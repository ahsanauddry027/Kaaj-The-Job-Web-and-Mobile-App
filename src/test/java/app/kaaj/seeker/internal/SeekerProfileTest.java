package app.kaaj.seeker.internal;

import static org.assertj.core.api.Assertions.assertThat;

import app.kaaj.shared.WagePeriod;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The job seeker profile: privacy rounding, clean-up and the completeness meter (M1-2). */
class SeekerProfileTest {

    private final Instant now = Instant.parse("2026-11-18T06:00:00Z");

    private SeekerProfile.Details details(List<UUID> occupations, Double lat, Double lng, Integer wage, WagePeriod period) {
        return new SeekerProfile.Details("  রহিম মিয়া ", " ", occupations, List.of(), 0, null, List.of(), lat, lng,
                "Tongi", 10, wage, period, List.of());
    }

    @Test
    void theHomePointIsRoundedToAbout500Metres() {
        assertThat(SeekerProfile.round(23.8916)).isEqualTo(23.89);
        assertThat(SeekerProfile.round(90.4023)).isEqualTo(90.4);
        assertThat(SeekerProfile.round(90.4026)).isEqualTo(90.405);
        assertThat(SeekerProfile.round(null)).isNull();
    }

    @Test
    void blankTextIsStoredAsEmptyAndAHalfPointIsDropped() {
        SeekerProfile profile = SeekerProfile.create(UUID.randomUUID(),
                details(List.of(UUID.randomUUID()), 23.8916, null, 18000, WagePeriod.MONTH), now);
        assertThat(profile.getDisplayName()).isEqualTo("রহিম মিয়া");
        assertThat(profile.getPhone()).isNull();
        assertThat(profile.getHomeLatitude()).isNull();
        assertThat(profile.getHomeLongitude()).isNull();
        assertThat(profile.toSummary().readyToApply()).isTrue();
    }

    @Test
    void theMeterListsWhatIsMissing() {
        SeekerProfile empty = SeekerProfile.create(UUID.randomUUID(), details(List.of(), null, null, null, null), now);
        Completeness meter = Completeness.of(empty);
        assertThat(meter.missing()).containsExactly("occupations", "skills", "experience", "home", "expectedWage", "shifts");
        assertThat(meter.percent()).isEqualTo(14);
        assertThat(empty.toSummary().readyToApply()).isFalse();

        SeekerProfile full = SeekerProfile.create(UUID.randomUUID(), new SeekerProfile.Details("Rahim", "01811223344",
                List.of(UUID.randomUUID()), List.of(UUID.randomUUID()), 5, "SSC", List.of("DRIVING_PRO_LIGHT"), 23.89,
                90.405, "Tongi", 10, 18000, WagePeriod.MONTH, List.of("DAY")), now);
        assertThat(Completeness.of(full).percent()).isEqualTo(100);
        assertThat(Completeness.of(full).missing()).isEmpty();
    }
}
