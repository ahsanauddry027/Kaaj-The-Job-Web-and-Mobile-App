package app.kaaj.job.internal;

import app.kaaj.employer.EmployerDirectory;
import app.kaaj.employer.EmployerSummary;
import app.kaaj.job.JobContent;
import app.kaaj.job.JobDirectory;
import app.kaaj.job.JobModeration;
import app.kaaj.job.JobPublished;
import app.kaaj.job.JobStatus;
import app.kaaj.job.JobSubmitted;
import app.kaaj.job.JobSummary;
import app.kaaj.shared.Cursor;
import app.kaaj.shared.CursorPage;
import app.kaaj.shared.KaajException;
import app.kaaj.shared.Taxonomy;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Posting and managing jobs. Every method that changes a post takes the signed-in user's ID and loads the
 * post through the employer that user owns, so an employer can never see or change another employer's
 * posts (they get 404, as if the post didn't exist).
 */
@Service
@Transactional
public class JobService implements JobDirectory, JobModeration {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    /** Actions on the "My jobs" page. EDIT is a PUT with the new content; the others are PATCH actions. */
    public enum Action { SUBMIT, PAUSE, RESUME, CLOSE, REPOST }

    private final JobRepository jobs;
    private final EmployerDirectory employers;
    private final Taxonomy taxonomy;
    private final JobProperties properties;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    JobService(JobRepository jobs, EmployerDirectory employers, Taxonomy taxonomy, JobProperties properties,
               ApplicationEventPublisher events, Clock clock) {
        this.jobs = jobs;
        this.employers = employers;
        this.taxonomy = taxonomy;
        this.properties = properties;
        this.events = events;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- the employer's own posts

    /** The signed-in user's employer record. Posting needs a business profile first (Feature 3). */
    @Transactional(readOnly = true)
    public EmployerSummary employerOf(UUID ownerUserId) {
        return employers.findByOwner(ownerUserId)
                .orElseThrow(() -> KaajException.conflict("job.employer-profile-missing"));
    }

    @Transactional(readOnly = true)
    public Job get(UUID ownerUserId, UUID jobId) {
        return own(employerOf(ownerUserId), jobId);
    }

    /**
     * Saves a new post as a draft, and submits it when {@code submit} is true. With an
     * {@code Idempotency-Key}, sending the same request twice creates only one post.
     */
    public Job create(UUID ownerUserId, JobDetails details, boolean submit, UUID idempotencyKey) {
        EmployerSummary employer = employerOf(ownerUserId);
        if (idempotencyKey != null) {
            Optional<Job> earlier = jobs.findByIdempotencyKey(idempotencyKey);
            if (earlier.isPresent()) {
                if (!earlier.get().getEmployerId().equals(employer.id())) {
                    throw KaajException.conflict("job.idempotency-key-used");
                }
                return earlier.get();
            }
        }
        check(submit ? JobRules.checkSubmit(details) : JobRules.checkDraft(details), details);
        Instant now = clock.instant();
        Job job = jobs.save(Job.draft(employer.id(), details, idempotencyKey, now));
        if (submit) {
            submit(job, employer, now);
        }
        return job;
    }

    /**
     * Saves new content. A draft stays a draft (unless {@code submit} is true); a published or paused post
     * goes through review again, keeping its original 30-day window.
     */
    public Job update(UUID ownerUserId, UUID jobId, JobDetails details, boolean submit) {
        EmployerSummary employer = employerOf(ownerUserId);
        Job job = own(employer, jobId);
        Instant now = clock.instant();
        if (job.getStatus() == JobStatus.DRAFT) {
            check(submit ? JobRules.checkSubmit(details) : JobRules.checkDraft(details), details);
            job.edit(details, now);
            if (submit) {
                submit(job, employer, now);
            }
            return job;
        }
        if (job.getStatus() != JobStatus.PUBLISHED && job.getStatus() != JobStatus.PAUSED) {
            throw KaajException.conflict("job.not-editable", job.getStatus());
        }
        check(JobRules.checkSubmit(details), details);
        requireVerified(employer);
        job.edit(details, now);
        job.submit(now);
        events.publishEvent(new JobSubmitted(job.getId(), employer.id()));
        return job;
    }

    /** Submit, pause, resume, close or repost. Repost returns the new draft. */
    public Job act(UUID ownerUserId, UUID jobId, Action action) {
        EmployerSummary employer = employerOf(ownerUserId);
        Job job = own(employer, jobId);
        Instant now = clock.instant();
        switch (action) {
            case SUBMIT -> {
                if (job.getStatus() != JobStatus.DRAFT) {
                    throw KaajException.conflict("job.not-a-draft");
                }
                submit(job, employer, now);
            }
            case PAUSE -> job.pause(now);
            case RESUME -> job.resume(now);
            case CLOSE -> job.close(Job.CLOSED_BY_EMPLOYER, now);
            case REPOST -> {
                return jobs.save(job.repost(now));
            }
        }
        return job;
    }

    /** Only drafts can be deleted; anything that was ever published is closed instead, to keep its history. */
    public void deleteDraft(UUID ownerUserId, UUID jobId) {
        Job job = own(employerOf(ownerUserId), jobId);
        if (job.getStatus() != JobStatus.DRAFT) {
            throw KaajException.conflict("job.delete-only-drafts");
        }
        jobs.delete(job);
    }

    @Transactional(readOnly = true)
    public CursorPage<Job> list(UUID ownerUserId, JobStatus status, String query, String cursor, int size) {
        EmployerSummary employer = employerOf(ownerUserId);
        Set<JobStatus> statuses = status == null ? EnumSet.allOf(JobStatus.class) : EnumSet.of(status);
        String cleaned = query == null ? "" : query.strip().toLowerCase(Locale.ROOT).replace("%", "").replace("_", "");
        String pattern = "%" + cleaned + "%";
        int pageSize = Math.clamp(size, 1, 50);
        PageRequest page = PageRequest.of(0, pageSize + 1);
        List<Job> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = jobs.listFirstPage(employer.id(), statuses, pattern, page);
        } else {
            Cursor after = Cursor.decode(cursor);
            rows = jobs.listAfter(employer.id(), statuses, pattern, after.at(), after.id(), page);
        }
        if (rows.size() <= pageSize) {
            return new CursorPage<>(rows, null);
        }
        List<Job> items = List.copyOf(rows.subList(0, pageSize));
        Job last = items.getLast();
        return new CursorPage<>(items, new Cursor(last.getCreatedAt(), last.getId()).encode());
    }

    /** Posts per status, for the tabs on "My jobs" and the dashboard. Statuses with no posts show 0. */
    @Transactional(readOnly = true)
    public Map<JobStatus, Long> counts(UUID ownerUserId) {
        EmployerSummary employer = employerOf(ownerUserId);
        Map<JobStatus, Long> counts = new EnumMap<>(JobStatus.class);
        for (JobStatus status : JobStatus.values()) {
            counts.put(status, 0L);
        }
        for (Object[] row : jobs.countByStatus(employer.id())) {
            counts.put((JobStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    // ---------------------------------------------------------------- events and scheduled work

    /** Called when an admin approves an employer: their posts waiting for verification are submitted. */
    public int submitWaitingDrafts(UUID employerId) {
        EmployerSummary employer = employers.findById(employerId).orElse(null);
        if (employer == null || !employer.verified()) {
            return 0;
        }
        Instant now = clock.instant();
        int submitted = 0;
        for (Job job : jobs.findByEmployerIdAndStatusAndStatusReason(employerId, JobStatus.DRAFT, Job.AWAITING_VERIFICATION)) {
            if (JobRules.checkSubmit(job.details()).isEmpty()) {
                job.submit(now);
                events.publishEvent(new JobSubmitted(job.getId(), employerId));
                submitted++;
            }
        }
        return submitted;
    }

    /** Closes published or paused posts whose 30 days are over. */
    public int closeExpired(Instant now) {
        List<Job> expired = jobs.findExpired(now);
        expired.forEach(job -> job.close(Job.EXPIRED, now));
        if (!expired.isEmpty()) {
            log.info("Closed {} job post(s) after {} days", expired.size(), properties.expiryDays());
        }
        return expired.size();
    }

    // ---------------------------------------------------------------- JobModeration (scam screening and moderation)

    @Override
    @Transactional(readOnly = true)
    public Optional<JobContent> content(UUID jobId) {
        return jobs.findById(jobId).map(job -> new JobContent(job.getId(), job.getEmployerId(), job.getTitleBn(),
                job.getTitleEn(), job.getDescriptionBn(), job.getDescriptionEn(), job.getWageMin(), job.getWageMax(),
                job.getWagePeriod(), job.getHours(), job.getOpenings(), job.getAreaName(), job.getStatus(),
                job.getStatusReason(), job.getPublishedAt()));
    }

    @Override
    public void publish(UUID jobId) {
        Job job = load(jobId);
        if (job.getStatus() != JobStatus.IN_REVIEW) {
            return;
        }
        publishNow(job);
    }

    @Override
    public void hold(UUID jobId, String reason) {
        load(jobId).hold(reason, clock.instant());
    }

    @Override
    public void block(UUID jobId, String reason) {
        Job job = load(jobId);
        if (job.getStatus() == JobStatus.BLOCKED) {
            return;
        }
        job.block(reason, clock.instant());
    }

    @Override
    public void restore(UUID jobId) {
        Job job = load(jobId);
        if (job.getStatus() == JobStatus.BLOCKED) {
            publishNow(job);
        }
    }

    private void publishNow(Job job) {
        job.publish(clock.instant(), properties.expiryDays());
        if (job.getStatus() == JobStatus.PUBLISHED) {
            events.publishEvent(new JobPublished(job.getId(), job.getEmployerId(), job.getPublishedAt(), job.getExpiresAt()));
        }
    }

    private Job load(UUID jobId) {
        return jobs.findById(jobId).orElseThrow(() -> KaajException.notFound("job.not-found"));
    }

    // ---------------------------------------------------------------- JobDirectory (for other modules)

    @Override
    @Transactional(readOnly = true)
    public Optional<JobSummary> findById(UUID jobId) {
        return jobs.findById(jobId).map(job -> summary(job, employers.findById(job.getEmployerId()).orElse(null)));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, JobSummary> findAllById(Collection<UUID> jobIds) {
        List<Job> found = jobs.findAllById(jobIds);
        Map<UUID, EmployerSummary> owners = employers.findAllById(found.stream().map(Job::getEmployerId).distinct().toList());
        Map<UUID, JobSummary> result = new LinkedHashMap<>();
        for (Job job : found) {
            result.put(job.getId(), summary(job, owners.get(job.getEmployerId())));
        }
        return result;
    }

    private static JobSummary summary(Job job, EmployerSummary employer) {
        return new JobSummary(job.getId(), job.getEmployerId(),
                employer == null ? null : employer.name(),
                employer != null && employer.verified(),
                job.getTitleBn(), job.getTitleEn(), job.getStatus(), job.getWageMin(), job.getWageMax(),
                job.getWagePeriod(), job.getExpiresAt(), job.getOccupationId(), job.getAreaName(), job.isRemote(),
                job.getLatitude(), job.getLongitude(), job.getMinExperienceYears(), job.getSkillIds(),
                job.getRequiredDocuments());
    }

    /** A published post, for the public job page. */
    @Transactional(readOnly = true)
    public Job getPublished(UUID jobId) {
        return jobs.findById(jobId)
                .filter(job -> job.getStatus() == JobStatus.PUBLISHED)
                .orElseThrow(() -> KaajException.notFound("job.not-found"));
    }

    // ---------------------------------------------------------------- helpers

    /**
     * Submits a complete post. A post by an employer who isn't verified yet stays a draft marked
     * "waiting for verification", and goes live once an admin approves the business.
     */
    private void submit(Job job, EmployerSummary employer, Instant now) {
        check(JobRules.checkSubmit(job.details()), job.details());
        if (properties.requireVerifiedEmployer() && !employer.verified()) {
            job.awaitVerification(now);
            return;
        }
        job.submit(now);
        events.publishEvent(new JobSubmitted(job.getId(), employer.id()));
    }

    private void requireVerified(EmployerSummary employer) {
        if (properties.requireVerifiedEmployer() && !employer.verified()) {
            throw KaajException.conflict("job.employer-not-verified");
        }
    }

    private Job own(EmployerSummary employer, UUID jobId) {
        return jobs.findByIdAndEmployerId(jobId, employer.id())
                .orElseThrow(() -> KaajException.notFound("job.not-found"));
    }

    /** Throws 400 with field errors; also checks that the category and skills exist in the taxonomy. */
    private void check(Map<String, String> errors, JobDetails details) {
        if (details.occupationId() != null && taxonomy.occupation(details.occupationId()).isEmpty()) {
            errors.putIfAbsent("occupationId", "job.category.unknown");
        }
        if (details.skillIds() != null && !details.skillIds().isEmpty()) {
            Set<UUID> wanted = new HashSet<>(details.skillIds());
            if (taxonomy.skills(wanted).size() != wanted.size()) {
                errors.putIfAbsent("skillIds", "job.skill.unknown");
            }
        }
        if (!errors.isEmpty()) {
            throw KaajException.invalid(errors);
        }
    }
}
