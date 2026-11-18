package app.kaaj.job.internal;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** A job seeker's saved jobs (M2-1), table {@code saved_job} (V150). */
@Component
class SavedJobs {

    private final JdbcClient jdbc;

    SavedJobs(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    void save(UUID userId, UUID jobId) {
        jdbc.sql("INSERT INTO saved_job (user_id, job_id) VALUES (:user, :job) ON CONFLICT DO NOTHING")
                .param("user", userId).param("job", jobId).update();
    }

    void remove(UUID userId, UUID jobId) {
        jdbc.sql("DELETE FROM saved_job WHERE user_id = :user AND job_id = :job")
                .param("user", userId).param("job", jobId).update();
    }

    /** The saved posts that are still open, newest saved first. */
    List<UUID> openJobIds(UUID userId, Instant now) {
        return jdbc.sql("""
                        SELECT s.job_id FROM saved_job s JOIN job j ON j.id = s.job_id
                        WHERE s.user_id = :user AND j.status = 'PUBLISHED' AND j.expires_at > :now
                        ORDER BY s.saved_at DESC LIMIT 100""")
                .param("user", userId)
                .param("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .query(UUID.class)
                .list();
    }

    /** Which of these posts the user has saved (to fill in the heart on search results). */
    Set<UUID> savedAmong(UUID userId, Collection<UUID> jobIds) {
        if (jobIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(jdbc.sql("SELECT job_id FROM saved_job WHERE user_id = :user AND job_id IN (:jobs)")
                .param("user", userId).param("jobs", jobIds)
                .query(UUID.class)
                .list());
    }
}
