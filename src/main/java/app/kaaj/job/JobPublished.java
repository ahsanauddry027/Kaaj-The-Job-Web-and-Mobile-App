package app.kaaj.job;

import java.time.Instant;
import java.util.UUID;

/** A post went live. Matching and notifications (Module 3) listen for it. */
public record JobPublished(UUID jobId, UUID employerId, Instant publishedAt, Instant expiresAt) {
}
