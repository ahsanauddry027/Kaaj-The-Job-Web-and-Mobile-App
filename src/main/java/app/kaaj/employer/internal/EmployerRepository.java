package app.kaaj.employer.internal;

import app.kaaj.employer.VerificationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployerRepository extends JpaRepository<Employer, UUID> {

    Optional<Employer> findByOwnerId(UUID ownerId);

    boolean existsByDocumentKey(String documentKey);

    long countByVerificationStatus(VerificationStatus status);

    /** The admin queue: oldest submission first (first page). */
    @Query("""
            select e from Employer e
            where e.verificationStatus = :status
            order by e.submittedAt asc, e.id asc
            """)
    List<Employer> queueFirstPage(@Param("status") VerificationStatus status, Pageable page);

    /** The admin queue after a cursor (keyset pagination on submitted_at, id). */
    @Query("""
            select e from Employer e
            where e.verificationStatus = :status
              and (e.submittedAt > :at or (e.submittedAt = :at and e.id > :id))
            order by e.submittedAt asc, e.id asc
            """)
    List<Employer> queueAfter(@Param("status") VerificationStatus status, @Param("at") Instant at,
                              @Param("id") UUID id, Pageable page);
}
