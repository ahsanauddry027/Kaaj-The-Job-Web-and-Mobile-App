package app.kaaj.employer;

import java.time.Instant;
import java.util.UUID;

/** Published when an admin approves an employer. Notifications (Member-2, M3-4) tell the employer. */
public record EmployerVerified(UUID employerId, UUID ownerId, Instant at) {
}
