package app.kaaj.employer;

import java.time.Instant;
import java.util.UUID;

/** Published when an admin rejects an employer; the reason is shown to the employer. */
public record EmployerRejected(UUID employerId, UUID ownerId, String reason, Instant at) {
}
