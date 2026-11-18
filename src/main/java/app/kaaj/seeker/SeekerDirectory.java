package app.kaaj.seeker;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** The seeker module's read API for other modules (applying and the applicant list). */
public interface SeekerDirectory {

    Optional<SeekerSummary> find(UUID userId);

    Map<UUID, SeekerSummary> findAll(Collection<UUID> userIds);
}
