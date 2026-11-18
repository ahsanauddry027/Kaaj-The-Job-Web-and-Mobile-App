package app.kaaj.job.internal;

import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.WagePeriod;
import java.util.List;
import java.util.UUID;

/** The content of a job post, as the employer fills it in on the "Post a job" form. */
public record JobDetails(
        String titleBn,
        String titleEn,
        String descriptionBn,
        String descriptionEn,
        UUID occupationId,
        JobType jobType,
        Shift shift,
        String hours,
        Integer openings,
        Integer wageMin,
        Integer wageMax,
        WagePeriod wagePeriod,
        Double latitude,
        Double longitude,
        String areaName,
        Boolean remote,
        Integer minExperienceYears,
        List<UUID> skillIds,
        List<String> requiredDocuments) {
}
