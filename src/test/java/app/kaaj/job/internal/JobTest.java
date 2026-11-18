package app.kaaj.job.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.kaaj.job.JobStatus;
import app.kaaj.shared.KaajException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JobTest {

    static final Instant NOW = Instant.parse("2026-11-05T09:00:00Z");

    static Job published() {
        Job job = Job.draft(UUID.randomUUID(), JobRulesTest.complete(), null, NOW);
        job.submit(NOW);
        job.publish(NOW, 30);
        return job;
    }

    @Test
    void publishingStartsThe30DayWindow() {
        Job job = published();
        assertThat(job.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        assertThat(job.getPublishedAt()).isEqualTo(NOW);
        assertThat(job.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
    }

    @Test
    void aTitleOrDescriptionInOneLanguageFillsTheOther() {
        Job job = published();
        assertThat(job.getDescriptionEn()).isEqualTo(job.getDescriptionBn());
        assertThat(job.getWageMonthlyMax()).isEqualTo(20_000);
    }

    @Test
    void editingAndRepublishingKeepsTheOriginalExpiryDate() {
        Job job = published();
        Instant later = NOW.plus(Duration.ofDays(10));
        job.edit(JobRulesTest.complete(), later);
        job.submit(later);
        job.publish(later, 30);
        assertThat(job.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
    }

    @Test
    void pauseResumeAndClose() {
        Job job = published();
        job.pause(NOW);
        assertThat(job.getStatus()).isEqualTo(JobStatus.PAUSED);
        assertThatThrownBy(() -> job.pause(NOW)).isInstanceOf(KaajException.class);
        job.resume(NOW);
        assertThat(job.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        job.close(Job.CLOSED_BY_EMPLOYER, NOW);
        assertThat(job.getStatus()).isEqualTo(JobStatus.CLOSED);
        assertThatThrownBy(() -> job.edit(JobRulesTest.complete(), NOW)).isInstanceOf(KaajException.class);
    }

    @Test
    void resumingAfter30DaysClosesThePostInstead() {
        Job job = published();
        job.pause(NOW);
        job.resume(NOW.plus(Duration.ofDays(31)));
        assertThat(job.getStatus()).isEqualTo(JobStatus.CLOSED);
        assertThat(job.getStatusReason()).isEqualTo(Job.EXPIRED);
    }

    @Test
    void repostMakesANewDraftWithTheSameContent() {
        Job job = published();
        job.close(Job.EXPIRED, NOW);
        Job copy = job.repost(NOW);
        assertThat(copy.getId()).isNotEqualTo(job.getId());
        assertThat(copy.getStatus()).isEqualTo(JobStatus.DRAFT);
        assertThat(copy.getRepostedFrom()).isEqualTo(job.getId());
        assertThat(copy.getTitleEn()).isEqualTo(job.getTitleEn());
        assertThat(copy.getPublishedAt()).isNull();
    }

    @Test
    void onlyAClosedPostCanBeReposted() {
        assertThatThrownBy(() -> published().repost(NOW)).isInstanceOf(KaajException.class);
    }

    @Test
    void aDraftCanWaitForItsEmployersVerification() {
        Job job = Job.draft(UUID.randomUUID(), JobRulesTest.complete(), null, NOW);
        job.awaitVerification(NOW);
        assertThat(job.getStatus()).isEqualTo(JobStatus.DRAFT);
        assertThat(job.getStatusReason()).isEqualTo(Job.AWAITING_VERIFICATION);
        job.submit(NOW);
        assertThat(job.getStatusReason()).isNull();
    }
}
