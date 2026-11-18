package app.kaaj.job.internal;

import app.kaaj.job.JobStatus;
import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.KaajException;
import app.kaaj.shared.UuidV7;
import app.kaaj.shared.WagePeriod;
import app.kaaj.shared.Wages;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

/**
 * A job post ({@code job}, migration V140). Every status change goes through this class, so the rules in
 * {@link JobStatus} always hold.
 */
@Entity
@Table(name = "job")
public class Job implements Persistable<UUID> {

    /** Shown while a post waits for its employer's verification (it stays a DRAFT until then). */
    public static final String AWAITING_VERIFICATION = "AWAITING_VERIFICATION";
    public static final String EXPIRED = "EXPIRED";
    public static final String CLOSED_BY_EMPLOYER = "CLOSED_BY_EMPLOYER";

    @Id
    private UUID id;

    @Column(name = "employer_id")
    private UUID employerId;

    @Column(name = "occupation_id")
    private UUID occupationId;

    @Column(name = "title_bn")
    private String titleBn;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "description_bn")
    private String descriptionBn;

    @Column(name = "description_en")
    private String descriptionEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    private Shift shift;

    private String hours;

    private int openings;

    @Column(name = "wage_min")
    private Integer wageMin;

    @Column(name = "wage_max")
    private Integer wageMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "wage_period")
    private WagePeriod wagePeriod;

    @Column(name = "wage_monthly_max")
    private Integer wageMonthlyMax;

    private Double latitude;
    private Double longitude;

    @Column(name = "area_name")
    private String areaName;

    private boolean remote;

    @Column(name = "min_experience_years")
    private Integer minExperienceYears;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skill_ids", columnDefinition = "uuid[]")
    private UUID[] skillIds = new UUID[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "required_documents", columnDefinition = "text[]")
    private String[] requiredDocuments = new String[0];

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "status_reason")
    private String statusReason;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "reposted_from")
    private UUID repostedFrom;

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    private boolean isNew = true;

    protected Job() {
    }

    public static Job draft(UUID employerId, JobDetails details, UUID idempotencyKey, Instant now) {
        Job job = new Job();
        job.id = UuidV7.next();
        job.employerId = employerId;
        job.status = JobStatus.DRAFT;
        job.idempotencyKey = idempotencyKey;
        job.createdAt = now;
        job.apply(details, now);
        return job;
    }

    /** Replaces the post's content. The service decides whether the post must be submitted again. */
    public void edit(JobDetails details, Instant now) {
        if (status != JobStatus.DRAFT && status != JobStatus.PUBLISHED && status != JobStatus.PAUSED) {
            throw KaajException.conflict("job.not-editable", status);
        }
        apply(details, now);
    }

    public void submit(Instant now) {
        moveTo(JobStatus.IN_REVIEW, now);
        statusReason = null;
    }

    /** The post is complete, but its employer isn't verified yet: it waits as a draft. */
    public void awaitVerification(Instant now) {
        if (status != JobStatus.DRAFT) {
            throw KaajException.conflict("job.not-a-draft");
        }
        statusReason = AWAITING_VERIFICATION;
        updatedAt = now;
    }

    /**
     * Publishes a post in review. The 30-day window starts at the first publication and is kept when an
     * edited post is published again, so editing never extends a post's life.
     */
    public void publish(Instant now, int expiryDays) {
        if (publishedAt == null) {
            publishedAt = now;
            expiresAt = now.plus(Duration.ofDays(expiryDays));
        }
        if (!expiresAt.isAfter(now)) {
            close(EXPIRED, now);
            return;
        }
        moveTo(JobStatus.PUBLISHED, now);
        statusReason = null;
    }

    /** Blocks a post in review, or removes a live one (scam screening and moderation). */
    public void block(String reason, Instant now) {
        moveTo(JobStatus.BLOCKED, now);
        statusReason = reason;
    }

    /**
     * Keeps the post out of search until a moderator decides: a post in review gets the reason; a published
     * or paused post goes back to review. Its 30-day window keeps running.
     */
    public void hold(String reason, Instant now) {
        if (status == JobStatus.PUBLISHED || status == JobStatus.PAUSED) {
            moveTo(JobStatus.IN_REVIEW, now);
        } else if (status != JobStatus.IN_REVIEW) {
            return;
        }
        statusReason = reason;
        updatedAt = now;
    }

    public void pause(Instant now) {
        requireStatus(JobStatus.PUBLISHED);
        moveTo(JobStatus.PAUSED, now);
    }

    /** Resumes a paused post, or closes it if its 30 days have already passed. */
    public void resume(Instant now) {
        requireStatus(JobStatus.PAUSED);
        if (isExpired(now)) {
            close(EXPIRED, now);
            return;
        }
        moveTo(JobStatus.PUBLISHED, now);
    }

    public void close(String reason, Instant now) {
        moveTo(JobStatus.CLOSED, now);
        statusReason = reason;
        closedAt = now;
    }

    /** A new draft with the same content, for a closed post. */
    public Job repost(Instant now) {
        requireStatus(JobStatus.CLOSED);
        Job copy = draft(employerId, details(), null, now);
        copy.repostedFrom = id;
        return copy;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public JobDetails details() {
        return new JobDetails(titleBn, titleEn, descriptionBn, descriptionEn, occupationId, jobType, shift, hours,
                openings, wageMin, wageMax, wagePeriod, latitude, longitude, areaName, remote, minExperienceYears,
                List.of(skillIds), List.of(requiredDocuments));
    }

    private void requireStatus(JobStatus expected) {
        if (status != expected) {
            throw KaajException.conflict("job.status.invalid-move", status, expected);
        }
    }

    private void moveTo(JobStatus next, Instant now) {
        if (!status.canMoveTo(next)) {
            throw KaajException.conflict("job.status.invalid-move", status, next);
        }
        status = next;
        updatedAt = now;
    }

    /** Copies the content. A title or description given in one language fills the other until AI drafting (M3-5). */
    private void apply(JobDetails d, Instant now) {
        String bnTitle = clean(d.titleBn());
        String enTitle = clean(d.titleEn());
        titleBn = bnTitle != null ? bnTitle : enTitle;
        titleEn = enTitle != null ? enTitle : bnTitle;
        String bnText = clean(d.descriptionBn());
        String enText = clean(d.descriptionEn());
        descriptionBn = bnText != null ? bnText : enText;
        descriptionEn = enText != null ? enText : bnText;
        occupationId = d.occupationId();
        jobType = d.jobType() == null ? JobType.FULL_TIME : d.jobType();
        shift = d.shift() == null ? Shift.DAY : d.shift();
        hours = clean(d.hours());
        openings = d.openings() == null ? 1 : d.openings();
        wageMin = d.wageMin();
        wageMax = d.wageMax();
        wagePeriod = d.wagePeriod();
        wageMonthlyMax = Wages.monthly(wageMax, wagePeriod);
        remote = Boolean.TRUE.equals(d.remote());
        latitude = d.latitude();
        longitude = d.longitude();
        areaName = clean(d.areaName());
        minExperienceYears = d.minExperienceYears();
        skillIds = d.skillIds() == null ? new UUID[0] : d.skillIds().stream().distinct().toArray(UUID[]::new);
        requiredDocuments = d.requiredDocuments() == null ? new String[0]
                : d.requiredDocuments().stream().distinct().toArray(String[]::new);
        updatedAt = now;
    }

    private static String clean(String text) {
        if (text == null) {
            return null;
        }
        String stripped = text.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        isNew = false;
    }

    public UUID getEmployerId() { return employerId; }
    public UUID getOccupationId() { return occupationId; }
    public String getTitleBn() { return titleBn; }
    public String getTitleEn() { return titleEn; }
    public String getDescriptionBn() { return descriptionBn; }
    public String getDescriptionEn() { return descriptionEn; }
    public JobType getJobType() { return jobType; }
    public Shift getShift() { return shift; }
    public String getHours() { return hours; }
    public int getOpenings() { return openings; }
    public Integer getWageMin() { return wageMin; }
    public Integer getWageMax() { return wageMax; }
    public WagePeriod getWagePeriod() { return wagePeriod; }
    public Integer getWageMonthlyMax() { return wageMonthlyMax; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getAreaName() { return areaName; }
    public boolean isRemote() { return remote; }
    public Integer getMinExperienceYears() { return minExperienceYears; }
    public List<UUID> getSkillIds() { return Arrays.asList(skillIds); }
    public List<String> getRequiredDocuments() { return Arrays.asList(requiredDocuments); }
    public JobStatus getStatus() { return status; }
    public String getStatusReason() { return statusReason; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getClosedAt() { return closedAt; }
    public UUID getRepostedFrom() { return repostedFrom; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
