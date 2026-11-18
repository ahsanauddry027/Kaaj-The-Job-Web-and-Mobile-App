package app.kaaj.application.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import app.kaaj.job.JobStatus;
import app.kaaj.job.JobSummary;
import app.kaaj.seeker.SeekerSummary;
import app.kaaj.shared.WagePeriod;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The rule-based match score, its reasons and its gaps. */
class MatchTest {

    private final UUID driver = UUID.randomUUID();
    private final UUID lightVehicle = UUID.randomUUID();
    private final UUID dhakaRoads = UUID.randomUUID();

    /** Karim's pickup-driver post in Tongi: driver, two skills, 1 year, a professional licence. */
    private JobSummary pickupDriver(boolean remote) {
        return new JobSummary(UUID.randomUUID(), UUID.randomUUID(), "Karim Transport", true, "পিকআপ ড্রাইভার",
                "Pickup driver", JobStatus.PUBLISHED, 16000, 20000, WagePeriod.MONTH, Instant.now(), driver,
                "Tongi", remote, remote ? null : 23.8916, remote ? null : 90.4023, 1, List.of(lightVehicle, dhakaRoads),
                List.of("DRIVING_PRO_LIGHT"));
    }

    private SeekerSummary seeker(List<UUID> occupations, List<UUID> skills, int years, List<String> documents,
                                 Double lat, Double lng) {
        return new SeekerSummary(UUID.randomUUID(), "Rahim", null, occupations, skills, years, null, documents, lat, lng,
                "Tongi", 10, 18000, WagePeriod.MONTH, List.of("DAY"));
    }

    @Test
    void aPerfectFitScoresOne() {
        Match match = Match.of(pickupDriver(false),
                seeker(List.of(driver), List.of(lightVehicle, dhakaRoads), 5, List.of("DRIVING_PRO_LIGHT"), 23.890, 90.405));
        assertThat(match.score()).isEqualTo(1.0);
        assertThat(match.reasons()).extracting(Match.Reason::code)
                .containsExactly("OCCUPATION_MATCH", "SKILLS_MATCH", "EXPERIENCE_OK", "DOCUMENTS_OK", "NEAR_HOME");
        assertThat(match.gaps()).isEmpty();
        assertThat(match.distanceKm()).isLessThan(1.0);
    }

    @Test
    void gapsExplainALowerScore() {
        Match match = Match.of(pickupDriver(false),
                seeker(List.of(driver), List.of(lightVehicle), 0, List.of(), 24.000, 90.420));
        // occupation 0.35 + half the skills 0.125; no experience, no licence, too far
        assertThat(match.score()).isCloseTo(0.475, within(0.01));
        assertThat(match.gaps()).extracting(Match.Reason::code)
                .containsExactly("SKILLS_MISSING", "EXPERIENCE_SHORT", "DOCUMENTS_MISSING", "FAR_FROM_HOME");
        assertThat(match.distanceKm()).isGreaterThan(10.0);
    }

    @Test
    void aRemoteJobCountsAsNearForEveryone() {
        Match match = Match.of(pickupDriver(true),
                seeker(List.of(driver), List.of(lightVehicle, dhakaRoads), 5, List.of("DRIVING_PRO_LIGHT"), null, null));
        assertThat(match.score()).isEqualTo(1.0);
        assertThat(match.reasons()).extracting(Match.Reason::code).contains("REMOTE");
        assertThat(match.distanceKm()).isNull();
    }

    @Test
    void haversineMatchesAKnownDistance() {
        // Tongi to Uttara is roughly 2 km.
        assertThat(Match.km(23.8916, 90.4023, 23.8740, 90.3980)).isCloseTo(2.0, within(0.3));
    }
}
