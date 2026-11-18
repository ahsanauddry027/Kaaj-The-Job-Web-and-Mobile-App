package app.kaaj.job;

import java.util.Optional;
import java.util.UUID;

/**
 * Decisions on posts, for scam screening and moderation (the {@code trust} module). Screening reads a
 * submitted post with {@link #content}, runs {@link JobRuleChecks}, and then publishes, holds or blocks it.
 * Moderators use the same methods from the case queue. While {@code kaaj.jobs.auto-publish=true} (only
 * without the trust module), the job module publishes every submitted post itself.
 */
public interface JobModeration {

    /** Why a post is in review or blocked; shown to the employer. */
    String SCREENING_REVIEW = "SCREENING_REVIEW";
    String SCREENING_BLOCKED = "SCREENING_BLOCKED";
    String REPORTED = "REPORTED";
    String REMOVED = "REMOVED";

    Optional<JobContent> content(UUID jobId);

    /** Publishes a post that is in review. Does nothing for posts in any other status. */
    void publish(UUID jobId);

    /**
     * Keeps a post out of search until a moderator decides: a post in review stays there with this reason;
     * a published or paused post goes back to review. Does nothing for other statuses.
     */
    void hold(UUID jobId, String reason);

    /** Blocks a post that is in review, published or paused, with a reason the employer sees. */
    void block(UUID jobId, String reason);

    /**
     * Undoes a block after a moderator dismissed the case: the post goes live again (or closes, if its
     * 30 days are over). Does nothing unless the post is blocked.
     */
    void restore(UUID jobId);
}
