package app.kaaj.employer.internal;

import app.kaaj.employer.EmployerRejected;
import app.kaaj.employer.EmployerVerified;
import app.kaaj.employer.VerificationStatus;
import app.kaaj.identity.AuditLog;
import app.kaaj.shared.Cursor;
import app.kaaj.shared.CursorPage;
import app.kaaj.shared.KaajException;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The admin side of verification: the queue and the decisions. Every decision is written to the audit log
 * in the same transaction, and an {@link EmployerVerified} or {@link EmployerRejected} event is published
 * (the job module listens, so drafts waiting for verification go live on approval).
 */
@Service
@Transactional
public class EmployerVerificationService {

    private static final List<VerificationStatus> QUEUE_STATUSES =
            List.of(VerificationStatus.PENDING, VerificationStatus.VERIFIED, VerificationStatus.REJECTED);

    private final EmployerRepository employers;
    private final AuditLog auditLog;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    EmployerVerificationService(EmployerRepository employers, AuditLog auditLog,
                                ApplicationEventPublisher events, Clock clock) {
        this.employers = employers;
        this.auditLog = auditLog;
        this.events = events;
        this.clock = clock;
    }

    /** One page of employers in a status, oldest submission first. */
    @Transactional(readOnly = true)
    public CursorPage<Employer> queue(VerificationStatus status, String cursor, int size) {
        if (!QUEUE_STATUSES.contains(status)) {
            throw KaajException.badRequest("employer.queue.bad-status");
        }
        int pageSize = Math.clamp(size, 1, 100);
        PageRequest page = PageRequest.of(0, pageSize + 1); // one extra row tells us whether there is a next page
        List<Employer> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = employers.queueFirstPage(status, page);
        } else {
            Cursor after = Cursor.decode(cursor);
            rows = employers.queueAfter(status, after.at(), after.id(), page);
        }
        if (rows.size() <= pageSize) {
            return new CursorPage<>(rows, null);
        }
        List<Employer> items = rows.subList(0, pageSize);
        Employer last = items.getLast();
        return new CursorPage<>(List.copyOf(items), new Cursor(last.getSubmittedAt(), last.getId()).encode());
    }

    @Transactional(readOnly = true)
    public Employer get(UUID employerId) {
        return employers.findById(employerId).orElseThrow(() -> KaajException.notFound("employer.not-found"));
    }

    @Transactional(readOnly = true)
    public Map<VerificationStatus, Long> counts() {
        Map<VerificationStatus, Long> counts = new EnumMap<>(VerificationStatus.class);
        for (VerificationStatus status : VerificationStatus.values()) {
            counts.put(status, employers.countByVerificationStatus(status));
        }
        return counts;
    }

    public Employer approve(UUID adminId, UUID employerId, String note) {
        Employer employer = get(employerId);
        Instant now = clock.instant();
        employer.approve(adminId, note, now);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("name", employer.getName());
        details.put("note", employer.getVerificationNote());
        auditLog.record(adminId, "EMPLOYER_VERIFIED", "employer:" + employerId, details);
        events.publishEvent(new EmployerVerified(employerId, employer.getOwnerId(), now));
        return employer;
    }

    public Employer reject(UUID adminId, UUID employerId, String reason) {
        Employer employer = get(employerId);
        Instant now = clock.instant();
        employer.reject(adminId, reason, now);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("name", employer.getName());
        details.put("reason", employer.getVerificationNote());
        auditLog.record(adminId, "EMPLOYER_REJECTED", "employer:" + employerId, details);
        events.publishEvent(new EmployerRejected(employerId, employer.getOwnerId(), employer.getVerificationNote(), now));
        return employer;
    }
}
