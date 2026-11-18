package app.kaaj.job;

import static org.assertj.core.api.Assertions.assertThat;

import app.kaaj.shared.WagePeriod;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The rule checks shared by the job-post assistant and scam screening (context file §10.4). */
class JobRuleChecksTest {

    static JobContent post(String description, Integer wageMin, Integer wageMax, String hours) {
        return new JobContent(UUID.randomUUID(), UUID.randomUUID(), "পিকআপ ড্রাইভার", "Pickup driver",
                description, description, wageMin, wageMax, wageMin == null ? null : WagePeriod.MONTH, hours, 2,
                "Tongi", JobStatus.IN_REVIEW, null, null);
    }

    @Test
    void anHonestPostHasNothingToFlag() {
        assertThat(JobRuleChecks.check(post("Drive a pickup around Tongi. One day off a week.", 16000, 20000, "08:00-16:00")))
                .isEmpty();
    }

    @Test
    void aRequestForMoneyBlocks() {
        List<Finding> findings = JobRuleChecks.check(post("Processing fee Tk 50,000 by bKash.", 45000, 60000, "08:00-16:00"));
        assertThat(JobRuleChecks.blocks(findings)).isTrue();
        assertThat(findings).extracting(Finding::code).contains(Finding.MONEY_REQUEST);
    }

    @Test
    void contactDetailsSendThePostToReview() {
        List<Finding> findings = JobRuleChecks.check(post("Call 01712345678 for details.", 16000, 20000, "08:00-16:00"));
        assertThat(JobRuleChecks.blocks(findings)).isFalse();
        assertThat(JobRuleChecks.needsReview(findings)).isTrue();
    }

    @Test
    void aLongDayAndAMissingWageAreQuestions() {
        List<Finding> findings = JobRuleChecks.check(post("Drive a pickup.", null, null, "08:00-18:00"));
        assertThat(findings).extracting(Finding::kind).containsOnly(Finding.Kind.QUESTION);
        assertThat(findings).extracting(Finding::code).containsExactly(Finding.WAGE_MISSING, Finding.LONG_DAY);
    }

    @Test
    void workingDaysOverMidnightAreCounted() {
        assertThat(JobRuleChecks.workingDay("20:00-06:00")).isEqualTo(Duration.ofHours(10));
        assertThat(JobRuleChecks.workingDay("08:00-16:00")).isEqualTo(Duration.ofHours(8));
        assertThat(JobRuleChecks.workingDay("8-6")).isNull();
    }
}
