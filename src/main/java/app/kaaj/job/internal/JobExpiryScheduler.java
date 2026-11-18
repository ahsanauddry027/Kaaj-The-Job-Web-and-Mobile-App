package app.kaaj.job.internal;

import java.time.Clock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Closes posts 30 days after they were first published. Runs every hour (at minute 5) by default, so a
 * post never stays open much longer than its 30 days; change it with {@code kaaj.jobs.expiry-cron}.
 */
@Component
class JobExpiryScheduler {

    private final JobService jobs;
    private final Clock clock;

    JobExpiryScheduler(JobService jobs, Clock clock) {
        this.jobs = jobs;
        this.clock = clock;
    }

    @Scheduled(cron = "${kaaj.jobs.expiry-cron:0 5 * * * *}")
    void closeExpiredPosts() {
        jobs.closeExpired(clock.instant());
    }
}
