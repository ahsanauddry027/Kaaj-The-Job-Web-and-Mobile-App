package app.kaaj.job.internal;

import app.kaaj.employer.EmployerSummary;
import app.kaaj.job.JobStatus;
import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.Taxonomy;
import app.kaaj.shared.Term;
import app.kaaj.shared.WagePeriod;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** The JSON shapes of the job API, and the mapping from {@link Job} to them. */
@Component
class JobViews {

    /** The "Post a job" form. The full rules for submitting are in {@link JobRules}. */
    record JobInput(
            @Size(max = 120) String titleBn,
            @Size(max = 120) String titleEn,
            @Size(max = 4000) String descriptionBn,
            @Size(max = 4000) String descriptionEn,
            UUID occupationId,
            JobType jobType,
            Shift shift,
            @Size(max = 11) String hours,
            @Min(1) @Max(1000) Integer openings,
            @Min(1) @Max(JobRules.MAX_WAGE) Integer wageMin,
            @Min(1) @Max(JobRules.MAX_WAGE) Integer wageMax,
            WagePeriod wagePeriod,
            @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @Size(max = 120) String areaName,
            Boolean remote,
            @Min(0) @Max(50) Integer minExperienceYears,
            @Size(max = 30) List<UUID> skillIds,
            @Size(max = 10) List<String> requiredDocuments) {

        JobDetails toDetails() {
            return new JobDetails(titleBn, titleEn, descriptionBn, descriptionEn, occupationId, jobType, shift, hours,
                    openings, wageMin, wageMax, wagePeriod, latitude, longitude, areaName, remote, minExperienceYears,
                    skillIds, requiredDocuments);
        }
    }

    record ActionRequest(JobService.Action action) {
    }

    /** The employer shown on a post: name and the "Verified" badge. */
    record EmployerBadge(UUID id, String name, boolean verified) {
    }

    /** The menu items the web app shows for a post in its current status. */
    enum AllowedAction { EDIT, SUBMIT, PAUSE, RESUME, CLOSE, REPOST, DELETE }

    record JobView(
            UUID id, JobStatus status, String statusReason, boolean awaitingVerification,
            String titleBn, String titleEn, String descriptionBn, String descriptionEn,
            Term occupation, JobType jobType, Shift shift, String hours, int openings,
            Integer wageMin, Integer wageMax, WagePeriod wagePeriod, Integer wageMonthlyMax,
            Double latitude, Double longitude, String areaName, boolean remote,
            Integer minExperienceYears, List<Term> skills, List<String> requiredDocuments,
            Instant publishedAt, Instant expiresAt, Instant closedAt, Instant createdAt, Instant updatedAt,
            UUID repostedFrom, EmployerBadge employer, Set<AllowedAction> allowedActions) {
    }

    /** What anyone may see on the public job page. */
    record PublicJobView(
            UUID id, String titleBn, String titleEn, String descriptionBn, String descriptionEn,
            Term occupation, JobType jobType, Shift shift, String hours, int openings,
            Integer wageMin, Integer wageMax, WagePeriod wagePeriod,
            Double latitude, Double longitude, String areaName, boolean remote,
            Integer minExperienceYears, List<Term> skills, List<String> requiredDocuments,
            Instant publishedAt, Instant expiresAt, EmployerBadge employer) {
    }

    private final Taxonomy taxonomy;

    JobViews(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    JobView view(Job job, EmployerSummary employer) {
        return new JobView(job.getId(), job.getStatus(), job.getStatusReason(),
                Job.AWAITING_VERIFICATION.equals(job.getStatusReason()),
                job.getTitleBn(), job.getTitleEn(), job.getDescriptionBn(), job.getDescriptionEn(),
                taxonomy.occupation(job.getOccupationId()).orElse(null), job.getJobType(), job.getShift(),
                job.getHours(), job.getOpenings(), job.getWageMin(), job.getWageMax(), job.getWagePeriod(),
                job.getWageMonthlyMax(), job.getLatitude(), job.getLongitude(), job.getAreaName(), job.isRemote(),
                job.getMinExperienceYears(), taxonomy.skills(job.getSkillIds()), job.getRequiredDocuments(),
                job.getPublishedAt(), job.getExpiresAt(), job.getClosedAt(), job.getCreatedAt(), job.getUpdatedAt(),
                job.getRepostedFrom(), badge(employer), allowedActions(job.getStatus()));
    }

    PublicJobView publicView(Job job, EmployerSummary employer) {
        return new PublicJobView(job.getId(), job.getTitleBn(), job.getTitleEn(), job.getDescriptionBn(),
                job.getDescriptionEn(), taxonomy.occupation(job.getOccupationId()).orElse(null), job.getJobType(),
                job.getShift(), job.getHours(), job.getOpenings(), job.getWageMin(), job.getWageMax(),
                job.getWagePeriod(), job.getLatitude(), job.getLongitude(), job.getAreaName(), job.isRemote(),
                job.getMinExperienceYears(), taxonomy.skills(job.getSkillIds()), job.getRequiredDocuments(),
                job.getPublishedAt(), job.getExpiresAt(), badge(employer));
    }

    static Set<AllowedAction> allowedActions(JobStatus status) {
        return switch (status) {
            case DRAFT -> EnumSet.of(AllowedAction.EDIT, AllowedAction.SUBMIT, AllowedAction.DELETE);
            case PUBLISHED -> EnumSet.of(AllowedAction.EDIT, AllowedAction.PAUSE, AllowedAction.CLOSE);
            case PAUSED -> EnumSet.of(AllowedAction.EDIT, AllowedAction.RESUME, AllowedAction.CLOSE);
            case CLOSED -> EnumSet.of(AllowedAction.REPOST);
            case IN_REVIEW, BLOCKED -> EnumSet.noneOf(AllowedAction.class);
        };
    }

    private static EmployerBadge badge(EmployerSummary employer) {
        return employer == null ? null : new EmployerBadge(employer.id(), employer.name(), employer.verified());
    }
}
