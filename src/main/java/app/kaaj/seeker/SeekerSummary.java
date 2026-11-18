package app.kaaj.seeker;

import app.kaaj.shared.WagePeriod;
import java.util.List;
import java.util.UUID;

/** What other modules (applying, the employer's applicant list) may know about a job seeker. */
public record SeekerSummary(
        UUID userId,
        String displayName,
        String phone,
        List<UUID> occupationIds,
        List<UUID> skillIds,
        int yearsExperience,
        String education,
        List<String> documents,
        Double homeLatitude,
        Double homeLongitude,
        String homeAreaName,
        double travelRadiusKm,
        Integer expectedWage,
        WagePeriod wagePeriod,
        List<String> shifts) {

    /** Applying needs a name and at least one occupation (M2-2). */
    public boolean readyToApply() {
        return displayName != null && !displayName.isBlank() && !occupationIds.isEmpty();
    }

    public boolean hasHome() {
        return homeLatitude != null && homeLongitude != null;
    }
}
