package app.kaaj.job;

import java.util.UUID;

/** A post was submitted. Scam screening (M3-6) will decide: publish, review or block. */
public record JobSubmitted(UUID jobId, UUID employerId) {
}
