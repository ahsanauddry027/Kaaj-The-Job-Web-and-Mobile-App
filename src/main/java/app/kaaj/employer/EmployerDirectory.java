package app.kaaj.employer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** The employer module's public API: job posts, search (Member-1) and matching read employers through it. */
public interface EmployerDirectory {

    Optional<EmployerSummary> findById(UUID employerId);

    /** The employer record of a user account with the EMPLOYER role, if they have created one. */
    Optional<EmployerSummary> findByOwner(UUID ownerUserId);

    Map<UUID, EmployerSummary> findAllById(Collection<UUID> employerIds);
}
