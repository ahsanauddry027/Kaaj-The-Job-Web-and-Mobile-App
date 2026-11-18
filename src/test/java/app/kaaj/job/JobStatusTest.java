package app.kaaj.job;

import static app.kaaj.job.JobStatus.BLOCKED;
import static app.kaaj.job.JobStatus.CLOSED;
import static app.kaaj.job.JobStatus.DRAFT;
import static app.kaaj.job.JobStatus.IN_REVIEW;
import static app.kaaj.job.JobStatus.PAUSED;
import static app.kaaj.job.JobStatus.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Every allowed and every refused move of a job post (context file §7, Feature 4). */
class JobStatusTest {

    static final Map<JobStatus, Set<JobStatus>> ALLOWED = Map.of(
            DRAFT, EnumSet.of(IN_REVIEW),
            IN_REVIEW, EnumSet.of(PUBLISHED, BLOCKED, CLOSED),
            PUBLISHED, EnumSet.of(PAUSED, CLOSED, IN_REVIEW, BLOCKED),
            PAUSED, EnumSet.of(PUBLISHED, CLOSED, IN_REVIEW, BLOCKED),
            CLOSED, EnumSet.noneOf(JobStatus.class),
            BLOCKED, EnumSet.of(PUBLISHED, CLOSED));

    @Test
    void onlyTheMovesInTheTableAreAllowed() {
        for (JobStatus from : JobStatus.values()) {
            for (JobStatus to : JobStatus.values()) {
                assertThat(from.canMoveTo(to))
                        .as("%s -> %s", from, to)
                        .isEqualTo(ALLOWED.get(from).contains(to));
            }
        }
    }
}
