package app.kaaj.job;

import app.kaaj.shared.WagePeriod;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** What other modules (applying, search, matching) may know about a job post. */
public record JobSummary(
        UUID id,
        UUID employerId,
        String employerName,
        boolean employerVerified,
        String titleBn,
        String titleEn,
        JobStatus status,
        Integer wageMin,
        Integer wageMax,
        WagePeriod wagePeriod,
        Instant expiresAt,
        UUID occupationId,
        String areaName,
        boolean remote,
        Double latitude,
        Double longitude,
        Integer minExperienceYears,
        List<UUID> skillIds,
        List<String> requiredDocuments) {

    /** Job seekers can apply only to published posts that haven't expired. */
    public boolean openForApplications(Instant now) {
        return status == JobStatus.PUBLISHED && (expiresAt == null || expiresAt.isAfter(now));
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}
