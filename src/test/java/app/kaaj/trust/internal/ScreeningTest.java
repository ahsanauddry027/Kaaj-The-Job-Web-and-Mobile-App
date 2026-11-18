package app.kaaj.trust.internal;

import static app.kaaj.trust.internal.ScamReviewer.Outcome.State.ANSWERED;
import static app.kaaj.trust.internal.ScamReviewer.Outcome.State.FAILED;
import static app.kaaj.trust.internal.ScamReviewer.Outcome.State.NOT_ASKED;
import static org.assertj.core.api.Assertions.assertThat;

import app.kaaj.job.Finding;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The screening decision table (context file §10.6). The model never decides on its own. */
class ScreeningTest {

    static final Finding MONEY = new Finding(Finding.Kind.BLOCK, Finding.MONEY_REQUEST, "processing fee");
    static final Finding PHONE = new Finding(Finding.Kind.REVIEW, Finding.CONTACT_DETAILS, "01712345678");
    static final Finding LONG_DAY = new Finding(Finding.Kind.QUESTION, Finding.LONG_DAY, "08:00-18:00");
    static final Finding NUDGE = new Finding(Finding.Kind.NUDGE, Finding.UNFAIR_REQUIREMENT, "male only");

    @Test
    void aRuleBlockAlwaysBlocks() {
        assertThat(Screening.decide(List.of(MONEY, PHONE), NOT_ASKED, Screening.Decision.PUBLISH)).isEqualTo(Screening.Decision.BLOCK);
    }

    @Test
    void aRuleFlagGoesToTheQueue() {
        assertThat(Screening.decide(List.of(PHONE), NOT_ASKED, Screening.Decision.PUBLISH)).isEqualTo(Screening.Decision.REVIEW);
    }

    @Test
    void questionsAndNudgesDoNotStopAPost() {
        assertThat(Screening.decide(List.of(LONG_DAY, NUDGE), NOT_ASKED, Screening.Decision.REVIEW)).isEqualTo(Screening.Decision.PUBLISH);
        assertThat(Screening.decide(List.of(), ANSWERED, Screening.Decision.REVIEW)).isEqualTo(Screening.Decision.PUBLISH);
    }

    @Test
    void whenTheAiCheckFailsTheSettingDecides() {
        assertThat(Screening.decide(List.of(), FAILED, Screening.Decision.REVIEW)).isEqualTo(Screening.Decision.REVIEW);
        assertThat(Screening.decide(List.of(), FAILED, Screening.Decision.PUBLISH)).isEqualTo(Screening.Decision.PUBLISH);
        assertThat(Screening.decide(List.of(), FAILED, Screening.Decision.BLOCK)).isEqualTo(Screening.Decision.REVIEW);
    }

    @Test
    void highAiRiskIsAReviewNeverABlock() {
        ScamAssessment high = new ScamAssessment(ScamAssessment.Risk.HIGH, false, false, false, null, List.of("easy money"), "Too good to be true.");
        assertThat(high.worthAReview()).isTrue();
        Finding flag = new Finding(Finding.Kind.REVIEW, Finding.AI_HIGH_RISK, "easy money");
        assertThat(Screening.decide(List.of(flag), ANSWERED, Screening.Decision.PUBLISH)).isEqualTo(Screening.Decision.REVIEW);
    }
}
