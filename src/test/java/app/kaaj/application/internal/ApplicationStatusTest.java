package app.kaaj.application.internal;

import static app.kaaj.application.internal.ApplicationStatus.APPLIED;
import static app.kaaj.application.internal.ApplicationStatus.DECLINED;
import static app.kaaj.application.internal.ApplicationStatus.HIRED;
import static app.kaaj.application.internal.ApplicationStatus.INTERVIEW;
import static app.kaaj.application.internal.ApplicationStatus.SHORTLISTED;
import static app.kaaj.application.internal.ApplicationStatus.VIEWED;
import static app.kaaj.application.internal.ApplicationStatus.WITHDRAWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.kaaj.application.internal.ApplicationStatus.Actor;
import app.kaaj.shared.KaajException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Who may move an application where (M2-2 statuses). */
class ApplicationStatusTest {

    private final Instant now = Instant.parse("2026-11-18T06:00:00Z");

    @Test
    void theEmployerMovesAnApplicationForward() {
        assertThat(APPLIED.employerChoices()).containsExactlyInAnyOrder(SHORTLISTED, DECLINED);
        assertThat(VIEWED.employerChoices()).containsExactlyInAnyOrder(SHORTLISTED, DECLINED);
        assertThat(SHORTLISTED.employerChoices()).containsExactlyInAnyOrder(INTERVIEW, HIRED, DECLINED);
        assertThat(INTERVIEW.employerChoices()).containsExactlyInAnyOrder(HIRED, DECLINED);
        assertThat(HIRED.employerChoices()).isEmpty();
        assertThat(DECLINED.employerChoices()).isEmpty();
        assertThat(WITHDRAWN.employerChoices()).isEmpty();
    }

    @Test
    void theJobSeekerCanWithdrawAnOpenApplicationAndApplyAgain() {
        for (ApplicationStatus open : new ApplicationStatus[] {APPLIED, VIEWED, SHORTLISTED, INTERVIEW}) {
            assertThat(open.canMoveTo(WITHDRAWN, Actor.SEEKER)).isTrue();
        }
        assertThat(HIRED.canMoveTo(WITHDRAWN, Actor.SEEKER)).isFalse();
        assertThat(DECLINED.canMoveTo(WITHDRAWN, Actor.SEEKER)).isFalse();
        assertThat(WITHDRAWN.canMoveTo(APPLIED, Actor.SEEKER)).isTrue();
        assertThat(APPLIED.canMoveTo(SHORTLISTED, Actor.SEEKER)).isFalse();
    }

    @Test
    void onlyOpeningANewApplicationMarksItViewed() {
        assertThat(APPLIED.canMoveTo(VIEWED, Actor.SYSTEM)).isTrue();
        assertThat(SHORTLISTED.canMoveTo(VIEWED, Actor.SYSTEM)).isFalse();
        assertThat(APPLIED.canMoveTo(VIEWED, Actor.EMPLOYER)).isFalse();
    }

    @Test
    void thePhoneNumberIsSharedOnlyAfterShortlisting() {
        assertThat(APPLIED.sharesPhone()).isFalse();
        assertThat(VIEWED.sharesPhone()).isFalse();
        assertThat(SHORTLISTED.sharesPhone()).isTrue();
        assertThat(HIRED.sharesPhone()).isTrue();
        assertThat(DECLINED.sharesPhone()).isFalse();
    }

    @Test
    void aRefusedMoveIsAConflictAndChangesNothing() {
        Application application = Application.apply(UUID.randomUUID(), UUID.randomUUID(), " ", 0.5, null, now);
        assertThat(application.getMessage()).isNull();
        assertThatThrownBy(() -> application.move(HIRED, Actor.EMPLOYER, now))
                .isInstanceOf(KaajException.class);
        assertThat(application.getStatus()).isEqualTo(APPLIED);

        assertThat(application.move(SHORTLISTED, Actor.EMPLOYER, now)).isEqualTo(APPLIED);
        assertThat(application.move(WITHDRAWN, Actor.SEEKER, now)).isEqualTo(SHORTLISTED);
        assertThat(application.reapply("again", 0.7, now)).isEqualTo(WITHDRAWN);
        assertThat(application.getStatus()).isEqualTo(APPLIED);
        assertThat(application.getMessage()).isEqualTo("again");
    }
}
