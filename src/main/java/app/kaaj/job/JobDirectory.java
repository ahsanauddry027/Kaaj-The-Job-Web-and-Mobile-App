package app.kaaj.job;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** The job module's read API for other modules, for example applying (M2-2) and search. */
public interface JobDirectory {

    Optional<JobSummary> findById(UUID jobId);

    Map<UUID, JobSummary> findAllById(Collection<UUID> jobIds);
}
