package app.kaaj.job.internal;

import app.kaaj.job.JobStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByIdAndEmployerId(UUID id, UUID employerId);

    Optional<Job> findByIdempotencyKey(UUID idempotencyKey);

    List<Job> findByEmployerIdAndStatusAndStatusReason(UUID employerId, JobStatus status, String statusReason);

    /** "My jobs", newest first (first page). {@code pattern} is a lower-case LIKE pattern such as {@code %driver%}. */
    @Query("""
            select j from Job j
            where j.employerId = :employerId and j.status in :statuses
              and (lower(j.titleBn) like :pattern or lower(j.titleEn) like :pattern)
            order by j.createdAt desc, j.id desc
            """)
    List<Job> listFirstPage(@Param("employerId") UUID employerId, @Param("statuses") Collection<JobStatus> statuses,
                            @Param("pattern") String pattern, Pageable page);

    /** "My jobs" after a cursor (keyset pagination on created_at, id). */
    @Query("""
            select j from Job j
            where j.employerId = :employerId and j.status in :statuses
              and (lower(j.titleBn) like :pattern or lower(j.titleEn) like :pattern)
              and (j.createdAt < :at or (j.createdAt = :at and j.id < :id))
            order by j.createdAt desc, j.id desc
            """)
    List<Job> listAfter(@Param("employerId") UUID employerId, @Param("statuses") Collection<JobStatus> statuses,
                        @Param("pattern") String pattern, @Param("at") Instant at, @Param("id") UUID id,
                        Pageable page);

    /** Number of posts per status, for the tabs on "My jobs" and the dashboard. */
    @Query("select j.status, count(j) from Job j where j.employerId = :employerId group by j.status")
    List<Object[]> countByStatus(@Param("employerId") UUID employerId);

    @Query("select j from Job j where j.status in :statuses and j.expiresAt <= :now")
    List<Job> findByStatusInAndExpiredBy(@Param("statuses") Collection<JobStatus> statuses, @Param("now") Instant now);

    /** Live or paused posts whose 30 days are over. */
    default List<Job> findExpired(Instant now) {
        return findByStatusInAndExpiredBy(List.of(JobStatus.PUBLISHED, JobStatus.PAUSED), now);
    }
}
