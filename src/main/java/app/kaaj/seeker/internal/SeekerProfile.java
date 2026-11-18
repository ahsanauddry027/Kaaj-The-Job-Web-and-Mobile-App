package app.kaaj.seeker.internal;

import app.kaaj.seeker.SeekerSummary;
import app.kaaj.shared.WagePeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/** A job seeker's profile ({@code seeker_profile}, migration V150). The key is the user's ID. */
@Entity
@Table(name = "seeker_profile")
public class SeekerProfile implements Persistable<UUID> {

    /** About 500 m: the home point is rounded to this before it is stored (privacy, §19). */
    static final double ROUNDING_DEGREES = 0.005;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "display_name")
    private String displayName;

    private String phone;

    @Column(name = "occupation_ids", columnDefinition = "uuid[]")
    private UUID[] occupationIds = new UUID[0];

    @Column(name = "skill_ids", columnDefinition = "uuid[]")
    private UUID[] skillIds = new UUID[0];

    @Column(name = "years_experience")
    private short yearsExperience;

    private String education;

    @Column(columnDefinition = "text[]")
    private String[] documents = new String[0];

    @Column(name = "home_latitude")
    private Double homeLatitude;

    @Column(name = "home_longitude")
    private Double homeLongitude;

    @Column(name = "home_area_name")
    private String homeAreaName;

    @Column(name = "travel_radius_km")
    private float travelRadiusKm = 10;

    @Column(name = "expected_wage")
    private Integer expectedWage;

    @Enumerated(EnumType.STRING)
    @Column(name = "wage_period")
    private WagePeriod wagePeriod;

    @Column(columnDefinition = "text[]")
    private String[] shifts = new String[0];

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    private boolean isNew = true;

    protected SeekerProfile() {
    }

    static SeekerProfile create(UUID userId, Details details, Instant now) {
        SeekerProfile profile = new SeekerProfile();
        profile.userId = userId;
        profile.createdAt = now;
        profile.update(details, now);
        return profile;
    }

    /** Everything the job seeker can edit, already checked by the service. */
    record Details(String displayName, String phone, List<UUID> occupationIds, List<UUID> skillIds,
                   int yearsExperience, String education, List<String> documents, Double homeLatitude,
                   Double homeLongitude, String homeAreaName, double travelRadiusKm, Integer expectedWage,
                   WagePeriod wagePeriod, List<String> shifts) {
    }

    void update(Details details, Instant now) {
        displayName = details.displayName().trim();
        phone = blankToNull(details.phone());
        occupationIds = details.occupationIds().stream().distinct().toArray(UUID[]::new);
        skillIds = details.skillIds().stream().distinct().toArray(UUID[]::new);
        yearsExperience = (short) details.yearsExperience();
        education = blankToNull(details.education());
        documents = details.documents().stream().distinct().toArray(String[]::new);
        if (details.homeLatitude() == null || details.homeLongitude() == null) {
            homeLatitude = null;
            homeLongitude = null;
        } else {
            homeLatitude = round(details.homeLatitude());
            homeLongitude = round(details.homeLongitude());
        }
        homeAreaName = blankToNull(details.homeAreaName());
        travelRadiusKm = (float) details.travelRadiusKm();
        expectedWage = details.wagePeriod() == null ? null : details.expectedWage();
        wagePeriod = expectedWage == null ? null : details.wagePeriod();
        shifts = details.shifts().stream().distinct().toArray(String[]::new);
        updatedAt = now;
    }

    /** Rounds a coordinate to about 500 m, so the exact home is never stored. */
    static Double round(Double degrees) {
        if (degrees == null) {
            return null;
        }
        double rounded = Math.round(degrees / ROUNDING_DEGREES) * ROUNDING_DEGREES;
        return Math.round(rounded * 1000) / 1000.0; // drop floating-point noise: 23.89, not 23.890000000000001
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    SeekerSummary toSummary() {
        return new SeekerSummary(userId, displayName, phone, List.of(occupationIds), List.of(skillIds), yearsExperience,
                education, List.of(documents), homeLatitude, homeLongitude, homeAreaName, travelRadiusKm, expectedWage,
                wagePeriod, List.of(shifts));
    }

    @Override
    public UUID getId() {
        return userId;
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

    public UUID getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getPhone() { return phone; }
    public List<UUID> getOccupationIds() { return Arrays.asList(occupationIds); }
    public List<UUID> getSkillIds() { return Arrays.asList(skillIds); }
    public int getYearsExperience() { return yearsExperience; }
    public String getEducation() { return education; }
    public List<String> getDocuments() { return Arrays.asList(documents); }
    public Double getHomeLatitude() { return homeLatitude; }
    public Double getHomeLongitude() { return homeLongitude; }
    public String getHomeAreaName() { return homeAreaName; }
    public double getTravelRadiusKm() { return travelRadiusKm; }
    public Integer getExpectedWage() { return expectedWage; }
    public WagePeriod getWagePeriod() { return wagePeriod; }
    public List<String> getShifts() { return Arrays.asList(shifts); }
    public Instant getUpdatedAt() { return updatedAt; }
}
