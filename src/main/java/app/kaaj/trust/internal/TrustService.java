package app.kaaj.trust.internal;

import app.kaaj.employer.EmployerDirectory;
import app.kaaj.employer.EmployerSummary;
import app.kaaj.identity.AuditLog;
import app.kaaj.identity.UserAdmin;
import app.kaaj.identity.UserDirectory;
import app.kaaj.job.Finding;
import app.kaaj.job.JobContent;
import app.kaaj.job.JobModeration;
import app.kaaj.job.JobRuleChecks;
import app.kaaj.job.JobStatus;
import app.kaaj.shared.CursorPage;
import app.kaaj.shared.KaajException;
import app.kaaj.trust.JobRemovedAsScam;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reports from users, the admin queue and the admin's decisions (with the audit log). */
@Service
@Transactional
class TrustService {

    static final Set<String> REASONS = Set.of("ASKED_FOR_MONEY", "FAKE_JOB", "HARASSMENT", "OTHER");
    static final Set<String> STATUSES = Set.of("OPEN", "REMOVED", "DISMISSED");
    private static final int PAGE = 20;
    private static final int MAX_OFFSET = 10_000;

    private final CaseStore cases;
    private final JobModeration moderation;
    private final EmployerDirectory employers;
    private final UserDirectory users;
    private final UserAdmin userAdmin;
    private final AuditLog audit;
    private final ApplicationEventPublisher events;
    private final TrustProperties properties;

    TrustService(CaseStore cases, JobModeration moderation, EmployerDirectory employers, UserDirectory users,
                 UserAdmin userAdmin, AuditLog audit, ApplicationEventPublisher events, TrustProperties properties) {
        this.cases = cases;
        this.moderation = moderation;
        this.employers = employers;
        this.users = users;
        this.userAdmin = userAdmin;
        this.audit = audit;
        this.events = events;
        this.properties = properties;
    }

    // ---------------------------------------------------------------- reports

    /**
     * A signed-in user reports a live post. One report per person per post; at most
     * {@code kaaj.trust.reports-per-day} a day. The post joins the admin queue; after
     * {@code kaaj.trust.reports-to-hold} reports it leaves search until an admin decides.
     */
    void report(UUID reporterId, TrustViews.ReportRequest request) {
        if (request == null || request.subjectId() == null) {
            throw KaajException.badRequest("report.subject-required");
        }
        String type = request.subjectType() == null ? CaseStore.JOB : request.subjectType();
        if (!CaseStore.JOB.equals(type)) {
            throw KaajException.badRequest("report.subject-unsupported");   // messages and employers come later
        }
        if (request.reason() == null || !REASONS.contains(request.reason())) {
            throw KaajException.invalid(Map.of("reason", "report.reason.required"));
        }
        String details = request.details() == null || request.details().isBlank() ? null : request.details().strip();
        if (details != null && details.length() > 500) {
            throw KaajException.invalid(Map.of("details", "report.details.length"));
        }
        JobContent post = moderation.content(request.subjectId())
                .filter(p -> p.status() == JobStatus.PUBLISHED || p.status() == JobStatus.PAUSED)
                .orElseThrow(() -> KaajException.notFound("job.not-found"));
        boolean ownPost = employers.findByOwner(reporterId).map(EmployerSummary::id)
                .filter(id -> id.equals(post.employerId())).isPresent();
        if (ownPost) {
            throw KaajException.badRequest("report.own-post");
        }
        if (cases.hasReported(reporterId, type, post.id())) {
            throw KaajException.conflict("report.duplicate");
        }
        if (cases.reportsInLastDay(reporterId) >= properties.reportsPerDay()) {
            throw KaajException.tooManyRequests("report.limit", properties.reportsPerDay());
        }
        UUID caseId = cases.findOpen(type, post.id()).map(CaseStore.CaseRow::id)
                .orElseGet(() -> cases.openOrUpdate(type, post.id(), "REPORT", JobRuleChecks.check(post), null, null));
        cases.addReport(caseId, reporterId, type, post.id(), request.reason(), details);
        int count = cases.countReport(caseId);
        if (count >= properties.reportsToHold()) {
            moderation.hold(post.id(), JobModeration.REPORTED);
        }
    }

    // ---------------------------------------------------------------- the admin queue

    @Transactional(readOnly = true)
    CursorPage<TrustViews.CaseSummary> list(String status, String cursor) {
        String wanted = status == null ? "OPEN" : status;
        if (!STATUSES.contains(wanted)) {
            throw KaajException.badRequest("case.status.unknown");
        }
        int offset = decode(cursor);
        List<CaseStore.CaseRow> rows = cases.list(wanted, offset, PAGE + 1);
        boolean more = rows.size() > PAGE;
        List<TrustViews.CaseSummary> items = rows.stream().limit(PAGE).map(this::summary).toList();
        return new CursorPage<>(items, more && offset + PAGE <= MAX_OFFSET ? encode(offset + PAGE) : null);
    }

    @Transactional(readOnly = true)
    Map<String, Long> counts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String status : List.of("OPEN", "REMOVED", "DISMISSED")) {
            counts.put(status, cases.count(status));
        }
        return counts;
    }

    @Transactional(readOnly = true)
    TrustViews.CaseDetail detail(UUID caseId) {
        CaseStore.CaseRow row = cases.find(caseId).orElseThrow(() -> KaajException.notFound("case.not-found"));
        JobContent post = moderation.content(row.subjectId()).orElse(null);
        EmployerSummary employer = post == null ? null : employers.findById(post.employerId()).orElse(null);
        UserDirectory.UserInfo owner = employer == null ? null : users.find(employer.ownerId()).orElse(null);
        List<TrustViews.ReportView> reports = cases.reports(row.subjectType(), row.subjectId()).stream()
                .map(r -> new TrustViews.ReportView(r.reason(), r.details(), r.createdAt()))
                .toList();
        return new TrustViews.CaseDetail(row.id(), row.subjectType(), row.subjectId(), row.source(), row.screening(),
                row.reportCount(), row.status(), row.decision(), row.note(), row.createdAt(), row.decidedAt(),
                row.findings(), row.aiAssessment(),
                post == null ? null : new TrustViews.JobBrief(post.id(), post.titleBn(), post.titleEn(),
                        post.descriptionBn(), post.descriptionEn(), post.wageMin(), post.wageMax(), post.wagePeriod(),
                        post.hours(), post.areaName(), post.status(), post.statusReason()),
                employer == null ? null : new TrustViews.EmployerBrief(employer.id(), employer.name(), employer.verified(),
                        owner == null ? null : owner.email(), owner != null && owner.active()),
                reports);
    }

    /**
     * {@code REMOVE_POST} blocks the post and warns its applicants ({@link JobRemovedAsScam});
     * {@code SUSPEND_EMPLOYER} also suspends the employer's account; {@code DISMISS} puts a post that was
     * held or blocked by screening back online. A note is required to remove or suspend.
     */
    TrustViews.CaseDetail decide(UUID caseId, UUID adminId, TrustViews.DecisionRequest request) {
        CaseStore.CaseRow row = cases.find(caseId).orElseThrow(() -> KaajException.notFound("case.not-found"));
        if (!"OPEN".equals(row.status())) {
            throw KaajException.conflict("case.closed");
        }
        String decision = request == null ? null : request.decision();
        String note = request == null || request.note() == null || request.note().isBlank() ? null : request.note().strip();
        if (decision == null || !Set.of("REMOVE_POST", "SUSPEND_EMPLOYER", "DISMISS").contains(decision)) {
            throw KaajException.invalid(Map.of("decision", "case.decision.required"));
        }
        if (!"DISMISS".equals(decision) && (note == null || note.length() < 3)) {
            throw KaajException.invalid(Map.of("note", "case.note.required"));
        }
        if (note != null && note.length() > 500) {
            throw KaajException.invalid(Map.of("note", "case.note.length"));
        }
        JobContent post = moderation.content(row.subjectId()).orElseThrow(() -> KaajException.notFound("job.not-found"));
        String target = "job:" + post.id();
        switch (decision) {
            case "REMOVE_POST", "SUSPEND_EMPLOYER" -> {
                moderation.block(post.id(), JobModeration.REMOVED);
                audit.record(adminId, "JOB_REMOVED", target, Map.of("case", caseId.toString(), "note", note));
                if ("SUSPEND_EMPLOYER".equals(decision)) {
                    EmployerSummary employer = employers.findById(post.employerId())
                            .orElseThrow(() -> KaajException.notFound("employer.not-found"));
                    userAdmin.suspend(employer.ownerId(), adminId, note);
                }
                events.publishEvent(new JobRemovedAsScam(post.id(), post.employerId()));
            }
            default -> {
                if (post.status() == JobStatus.IN_REVIEW) {
                    moderation.publish(post.id());
                } else if (post.status() == JobStatus.BLOCKED && JobModeration.SCREENING_BLOCKED.equals(post.statusReason())) {
                    moderation.restore(post.id());
                }
                audit.record(adminId, "CASE_DISMISSED", target, Map.of("case", caseId.toString(), "note", note == null ? "" : note));
            }
        }
        if (!cases.decide(caseId, "DISMISS".equals(decision) ? "DISMISSED" : "REMOVED", decision, note, adminId)) {
            throw KaajException.conflict("case.closed");
        }
        return detail(caseId);
    }

    // ---------------------------------------------------------------- the employer's own post

    /** Why the employer's post is in review or blocked: the newest case's findings and, once decided, the outcome. */
    @Transactional(readOnly = true)
    TrustViews.ScreeningView screening(UUID ownerUserId, UUID jobId) {
        EmployerSummary employer = employers.findByOwner(ownerUserId)
                .orElseThrow(() -> KaajException.notFound("job.not-found"));
        JobContent post = moderation.content(jobId)
                .filter(p -> p.employerId().equals(employer.id()))
                .orElseThrow(() -> KaajException.notFound("job.not-found"));
        return cases.findLatest(CaseStore.JOB, post.id())
                .map(row -> new TrustViews.ScreeningView(row.status(), row.screening(),
                        row.findings(), row.decision(), "REMOVED".equals(row.status()) ? row.note() : null, row.reportCount() > 0))
                .orElseGet(() -> new TrustViews.ScreeningView(null, null, JobRuleChecks.check(post), null, null, false));
    }

    // ---------------------------------------------------------------- helpers

    private TrustViews.CaseSummary summary(CaseStore.CaseRow row) {
        JobContent post = moderation.content(row.subjectId()).orElse(null);
        String employerName = post == null ? null : employers.findById(post.employerId()).map(EmployerSummary::name).orElse(null);
        return new TrustViews.CaseSummary(row.id(), row.subjectType(), row.subjectId(), row.source(), row.screening(),
                row.reportCount(), row.status(), row.decision(),
                post == null ? null : post.titleBn(), post == null ? null : post.titleEn(), employerName,
                post == null ? null : post.status(),
                row.findings().stream().filter(f -> f.kind() == Finding.Kind.BLOCK || f.kind() == Finding.Kind.REVIEW)
                        .map(Finding::code).distinct().toList(),
                row.createdAt(), row.decidedAt());
    }

    static String encode(int offset) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("o" + offset).getBytes(StandardCharsets.UTF_8));
    }

    static int decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int offset = Integer.parseInt(text.substring(1));
            if (!text.startsWith("o") || offset < 0 || offset > MAX_OFFSET) {
                throw KaajException.badRequest("error.bad-cursor");
            }
            return offset;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw KaajException.badRequest("error.bad-cursor");
        }
    }
}
