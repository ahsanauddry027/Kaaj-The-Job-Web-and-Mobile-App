package app.kaaj.application.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByJobIdAndSeekerId(UUID jobId, UUID seekerId);

    Optional<Application> findByIdempotencyKey(UUID idempotencyKey);

    List<Application> findBySeekerIdOrderByUpdatedAtDesc(UUID seekerId);

    List<Application> findByJobIdAndStatusNotOrderByCreatedAtAsc(UUID jobId, ApplicationStatus status);
}
