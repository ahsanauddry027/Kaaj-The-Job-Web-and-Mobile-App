package app.kaaj.job;

import java.util.UUID;

/**
 * Decisions on submitted posts, for scam screening and moderation (M3-6). Until M3-6 is built the job
 * module calls {@link #publish} itself after every submission ({@code kaaj.jobs.auto-publish=true}).
 */
public interface JobModeration {

    /** Publishes a post that is in review. Does nothing for posts in any other status. */
    void publish(UUID jobId);

    /** Blocks a post that is in review, with a reason the employer sees. */
    void block(UUID jobId, String reason);
}
