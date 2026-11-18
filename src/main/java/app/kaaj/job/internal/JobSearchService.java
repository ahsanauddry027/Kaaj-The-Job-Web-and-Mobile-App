package app.kaaj.job.internal;

import app.kaaj.employer.EmployerDirectory;
import app.kaaj.employer.EmployerSummary;
import app.kaaj.job.internal.JobViews.PublicJobView;
import app.kaaj.shared.CursorPage;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Job search and saved jobs for job seekers (Member-1's search in the CSE471 plan; built for the Android app):
 * runs the search, loads the posts in order and adds distances and the "saved" flag. {@link JobSearchController}
 * only reads the request and the signed-in user.
 */
@Service
class JobSearchService {

    /** One search result: the post, its distance from the searcher's point, and whether they saved it. */
    record JobHit(PublicJobView job, Double distanceKm, boolean saved) {
    }

    private final JobService jobService;
    private final JobRepository jobs;
    private final JobViews views;
    private final JobSearch search;
    private final SavedJobs saved;
    private final EmployerDirectory employers;
    private final Clock clock;

    JobSearchService(JobService jobService, JobRepository jobs, JobViews views, JobSearch search, SavedJobs saved,
                     EmployerDirectory employers, Clock clock) {
        this.jobService = jobService;
        this.jobs = jobs;
        this.views = views;
        this.search = search;
        this.saved = saved;
        this.employers = employers;
        this.clock = clock;
    }

    /** {@code seekerId} is the signed-in job seeker (for the "saved" flag), or null for guests and other roles. */
    CursorPage<JobHit> search(JobSearch.Query query, String cursor, int size, UUID seekerId) {
        JobSearch.Page page = search.search(query, cursor, size, clock.instant());
        Map<UUID, Double> distances = page.hits().stream()
                .filter(hit -> hit.distanceKm() != null)
                .collect(Collectors.toMap(JobSearch.Hit::jobId, JobSearch.Hit::distanceKm));
        List<UUID> ids = page.hits().stream().map(JobSearch.Hit::jobId).toList();
        return new CursorPage<>(hits(ids, distances, seekerId), page.nextCursor());
    }

    /** The job seeker's saved posts that are still open. */
    List<JobHit> savedJobs(UUID seekerId) {
        return hits(saved.openJobIds(seekerId, clock.instant()), Map.of(), seekerId);
    }

    void save(UUID seekerId, UUID jobId) {
        jobService.getPublished(jobId); // 404 unless the post is live
        saved.save(seekerId, jobId);
    }

    void unsave(UUID seekerId, UUID jobId) {
        saved.remove(seekerId, jobId);
    }

    /** Loads the posts in the given order and adds distances and the saved flag. */
    private List<JobHit> hits(List<UUID> ids, Map<UUID, Double> distances, UUID seekerId) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, Job> found = jobs.findAllById(ids).stream().collect(Collectors.toMap(Job::getId, Function.identity()));
        Map<UUID, EmployerSummary> owners = employers.findAllById(
                found.values().stream().map(Job::getEmployerId).distinct().toList());
        Set<UUID> savedIds = seekerId == null ? Set.of() : saved.savedAmong(seekerId, ids);
        return ids.stream()
                .map(found::get)
                .filter(Objects::nonNull)
                .map(job -> new JobHit(views.publicView(job, owners.get(job.getEmployerId())),
                        round(distances.get(job.getId())), savedIds.contains(job.getId())))
                .toList();
    }

    private static Double round(Double km) {
        return km == null ? null : Math.round(km * 10) / 10.0;
    }
}
