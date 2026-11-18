package app.kaaj.application.internal;

import app.kaaj.application.internal.ApplicationStatus.Actor;
import app.kaaj.employer.EmployerDirectory;
import app.kaaj.employer.EmployerSummary;
import app.kaaj.identity.AuditLog;
import app.kaaj.job.JobDirectory;
import app.kaaj.job.JobSummary;
import app.kaaj.seeker.SeekerDirectory;
import app.kaaj.seeker.SeekerSummary;
import app.kaaj.shared.KaajException;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applying, withdrawing, and the employer's decisions. Every status change also goes on the timeline. */
@Service
class ApplicationService {

    /** The statuses an employer may set by hand (VIEWED is set when they open an application). */
    static final Set<ApplicationStatus> EMPLOYER_STATUSES =
            EnumSet.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEW, ApplicationStatus.HIRED, ApplicationStatus.DECLINED);

    private final ApplicationRepository applications;
    private final ApplicationEvents events;
    private final JobDirectory jobs;
    private final SeekerDirectory seekers;
    private final EmployerDirectory employers;
    private final AuditLog auditLog;
    private final Clock clock;

    ApplicationService(ApplicationRepository applications, ApplicationEvents events, JobDirectory jobs,
                       SeekerDirectory seekers, EmployerDirectory employers, AuditLog auditLog, Clock clock) {
        this.applications = applications;
        this.events = events;
        this.jobs = jobs;
        this.seekers = seekers;
        this.employers = employers;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- job seeker

    /**
     * One-click apply with the Kaaj profile. The same {@code idempotencyKey} returns the same application;
     * applying again after withdrawing re-opens the old one (there is one per job and person).
     */
    @Transactional
    public Application apply(UUID seekerUserId, UUID jobId, String message, UUID idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<Application> earlier = applications.findByIdempotencyKey(idempotencyKey);
            if (earlier.isPresent()) {
                Application found = earlier.get();
                if (!found.getSeekerId().equals(seekerUserId) || !found.getJobId().equals(jobId)) {
                    throw KaajException.conflict("application.idempotency-key-used");
                }
                return found;
            }
        }
        SeekerSummary seeker = seekers.find(seekerUserId)
                .filter(SeekerSummary::readyToApply)
                .orElseThrow(() -> KaajException.conflict("application.profile-needed"));
        JobSummary job = jobs.findById(jobId).orElseThrow(() -> KaajException.notFound("job.not-found"));
        Instant now = clock.instant();
        if (!job.openForApplications(now)) {
            throw KaajException.conflict("application.job-closed");
        }
        double score = Match.of(job, seeker).score();

        Optional<Application> existing = applications.findByJobIdAndSeekerId(jobId, seekerUserId);
        if (existing.isPresent()) {
            Application application = existing.get();
            if (application.getStatus() != ApplicationStatus.WITHDRAWN) {
                throw KaajException.conflict("application.already-applied");
            }
            ApplicationStatus before = application.reapply(message, score, now);
            events.record(application.getId(), before, ApplicationStatus.APPLIED, Actor.SEEKER, message, now);
            return application;
        }
        Application application = applications.save(Application.apply(jobId, seekerUserId, message, score, idempotencyKey, now));
        events.record(application.getId(), null, ApplicationStatus.APPLIED, Actor.SEEKER, message, now);
        return application;
    }

    @Transactional(readOnly = true)
    public List<Application> mine(UUID seekerUserId) {
        return applications.findBySeekerIdOrderByUpdatedAtDesc(seekerUserId);
    }

    @Transactional(readOnly = true)
    public Application mineOne(UUID seekerUserId, UUID applicationId) {
        return applications.findById(applicationId)
                .filter(application -> application.getSeekerId().equals(seekerUserId))
                .orElseThrow(() -> KaajException.notFound("application.not-found"));
    }

    @Transactional
    public Application withdraw(UUID seekerUserId, UUID applicationId) {
        Application application = mineOne(seekerUserId, applicationId);
        Instant now = clock.instant();
        ApplicationStatus before = application.move(ApplicationStatus.WITHDRAWN, Actor.SEEKER, now);
        events.record(application.getId(), before, ApplicationStatus.WITHDRAWN, Actor.SEEKER, null, now);
        return application;
    }

    // ---------------------------------------------------------------- employer

    /** The applicants for one of the employer's posts. Withdrawn applications are no longer shared. */
    @Transactional(readOnly = true)
    public List<Application> applicants(UUID ownerUserId, UUID jobId) {
        ownJob(ownerUserId, jobId);
        return applications.findByJobIdAndStatusNotOrderByCreatedAtAsc(jobId, ApplicationStatus.WITHDRAWN);
    }

    /** Opens one applicant; the first time, the job seeker sees "Viewed". */
    @Transactional
    public Application open(UUID ownerUserId, UUID applicationId) {
        Application application = ownApplication(ownerUserId, applicationId);
        if (application.getStatus() == ApplicationStatus.APPLIED) {
            Instant now = clock.instant();
            application.move(ApplicationStatus.VIEWED, Actor.SYSTEM, now);
            events.record(application.getId(), ApplicationStatus.APPLIED, ApplicationStatus.VIEWED, Actor.SYSTEM, null, now);
        }
        return application;
    }

    /** Shortlist, interview, hire or "not selected", with an optional note the job seeker sees. */
    @Transactional
    public Application decide(UUID ownerUserId, UUID applicationId, ApplicationStatus next, String note,
                              Instant interviewAt, String interviewPlace) {
        if (next == null || !EMPLOYER_STATUSES.contains(next)) {
            throw KaajException.badRequest("application.status.not-allowed");
        }
        Application application = ownApplication(ownerUserId, applicationId);
        Instant now = clock.instant();
        ApplicationStatus before = application.move(next, Actor.EMPLOYER, now);
        if (next == ApplicationStatus.INTERVIEW) {
            application.scheduleInterview(interviewAt, interviewPlace);
        }
        events.record(application.getId(), before, next, Actor.EMPLOYER, note, now);
        auditLog.record(ownerUserId, "application.status", application.getId().toString(),
                Map.of("from", before.name(), "to", next.name()));
        return application;
    }

    // ---------------------------------------------------------------- helpers

    JobSummary ownJob(UUID ownerUserId, UUID jobId) {
        EmployerSummary employer = employers.findByOwner(ownerUserId)
                .orElseThrow(() -> KaajException.notFound("employer.profile.missing"));
        return jobs.findById(jobId)
                .filter(job -> job.employerId().equals(employer.id()))
                .orElseThrow(() -> KaajException.notFound("job.not-found"));
    }

    private Application ownApplication(UUID ownerUserId, UUID applicationId) {
        Application application = applications.findById(applicationId)
                .filter(found -> found.getStatus() != ApplicationStatus.WITHDRAWN)
                .orElseThrow(() -> KaajException.notFound("application.not-found"));
        ownJob(ownerUserId, application.getJobId());
        return application;
    }
}
