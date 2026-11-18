package app.kaaj.trust;

import java.util.UUID;

/** A moderator removed a post as a scam. Notifications (Member-2) warn the people who applied to it. */
public record JobRemovedAsScam(UUID jobId, UUID employerId) {
}
