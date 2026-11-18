package app.kaaj.seeker.internal;

import app.kaaj.identity.CurrentUser;
import app.kaaj.shared.KaajException;
import app.kaaj.shared.Taxonomy;
import app.kaaj.shared.Term;
import app.kaaj.shared.WagePeriod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The job seeker's own profile: {@code GET /api/v1/me/profile} (404 before it exists) and
 * {@code PUT /api/v1/me/profile}. Only job seekers reach it (SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/me/profile")
class SeekerProfileController {

    /** The same Bangladeshi mobile number rule as the employer's contact number. */
    static final String BD_MOBILE = "^(\\+?880[-\\s]?|0)1[3-9](?:[-\\s]?\\d){8}$";

    private final SeekerProfileService service;
    private final Taxonomy taxonomy;
    private final CurrentUser currentUser;

    SeekerProfileController(SeekerProfileService service, Taxonomy taxonomy, CurrentUser currentUser) {
        this.service = service;
        this.taxonomy = taxonomy;
        this.currentUser = currentUser;
    }

    record ProfileRequest(
            @NotBlank @Size(min = 2, max = 80) String displayName,
            @Pattern(regexp = BD_MOBILE) String phone,
            @Size(max = 5) List<UUID> occupationIds,
            @Size(max = 20) List<UUID> skillIds,
            @Min(0) @Max(60) Integer yearsExperience,
            @Size(max = 200) String education,
            @Size(max = 10) List<String> documents,
            @DecimalMin("-90") @DecimalMax("90") Double homeLatitude,
            @DecimalMin("-180") @DecimalMax("180") Double homeLongitude,
            @Size(max = 120) String homeAreaName,
            @DecimalMin("1") @DecimalMax("100") Double travelRadiusKm,
            @Min(1) @Max(10_000_000) Integer expectedWage,
            WagePeriod wagePeriod,
            @Size(max = 5) List<String> shifts) {

        SeekerProfile.Details details() {
            return new SeekerProfile.Details(displayName, blankPhone(phone), orEmpty(occupationIds), orEmpty(skillIds),
                    yearsExperience == null ? 0 : yearsExperience, education, orEmpty(documents), homeLatitude,
                    homeLongitude, homeAreaName, travelRadiusKm == null ? 10 : travelRadiusKm, expectedWage,
                    wagePeriod, orEmpty(shifts));
        }

        private static <T> List<T> orEmpty(List<T> list) {
            return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
        }

        private static String blankPhone(String phone) {
            return phone == null || phone.isBlank() ? null : phone.trim();
        }
    }

    record ProfileView(
            UUID userId, String displayName, String phone, List<Term> occupations, List<Term> skills,
            int yearsExperience, String education, List<String> documents, Double homeLatitude, Double homeLongitude,
            String homeAreaName, double travelRadiusKm, Integer expectedWage, WagePeriod wagePeriod,
            List<String> shifts, int completeness, List<String> missing, boolean readyToApply, Instant updatedAt) {
    }

    @GetMapping
    ProfileView get() {
        return service.get(currentUser.id()).map(this::view)
                .orElseThrow(() -> KaajException.notFound("seeker.profile.missing"));
    }

    @PutMapping
    ProfileView save(@Valid @RequestBody ProfileRequest body) {
        return view(service.save(currentUser.id(), body.details()));
    }

    private ProfileView view(SeekerProfile profile) {
        Completeness completeness = Completeness.of(profile);
        List<Term> occupations = profile.getOccupationIds().stream()
                .map(taxonomy::occupation).flatMap(java.util.Optional::stream).toList();
        return new ProfileView(profile.getUserId(), profile.getDisplayName(), profile.getPhone(), occupations,
                taxonomy.skills(profile.getSkillIds()), profile.getYearsExperience(), profile.getEducation(),
                profile.getDocuments(), profile.getHomeLatitude(), profile.getHomeLongitude(), profile.getHomeAreaName(),
                profile.getTravelRadiusKm(), profile.getExpectedWage(), profile.getWagePeriod(), profile.getShifts(),
                completeness.percent(), completeness.missing(), profile.toSummary().readyToApply(),
                profile.getUpdatedAt());
    }
}
