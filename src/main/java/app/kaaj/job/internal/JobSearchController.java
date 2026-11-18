package app.kaaj.job.internal;

import app.kaaj.identity.CurrentUser;
import app.kaaj.identity.Role;
import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.job.internal.JobSearchService.JobHit;
import app.kaaj.shared.CursorPage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Job search and the saved-jobs list, for job seekers and visitors (the work is done by {@link JobSearchService}).
 * Search is public; saving needs a job seeker account (SecurityConfig).
 * One published post ({@code GET /api/v1/jobs/{id}}) is in {@link PublicJobController}.
 *
 * <p>In the CSE471 group plan, job search belongs to Member-1; it lives in its own controller so the
 * employer's job module (Member-3) builds without it.
 */
@RestController
class JobSearchController {

    private final JobSearchService searchService;
    private final CurrentUser currentUser;

    JobSearchController(JobSearchService searchService, CurrentUser currentUser) {
        this.searchService = searchService;
        this.currentUser = currentUser;
    }

    /**
     * {@code GET /api/v1/jobs?q=&occupation=&lat=&lng=&radiusKm=&wageMin=&type=&shift=&sort=&cursor=&size=}.
     * {@code wageMin} is a monthly amount, so day and month wages compare fairly.
     */
    @GetMapping("/api/v1/jobs")
    CursorPage<JobHit> search(@RequestParam(required = false) String q,
                              @RequestParam(required = false) UUID occupation,
                              @RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90") Double lat,
                              @RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180") Double lng,
                              @RequestParam(required = false) @DecimalMin("1") @DecimalMax("200") Double radiusKm,
                              @RequestParam(required = false) Integer wageMin,
                              @RequestParam(required = false) JobType type,
                              @RequestParam(required = false) Shift shift,
                              @RequestParam(defaultValue = "NEWEST") JobSearch.Sort sort,
                              @RequestParam(required = false) String cursor,
                              @RequestParam(defaultValue = "20") int size) {
        UUID seeker = currentUser.idIfSignedIn().filter(user -> currentUser.has(Role.SEEKER)).orElse(null);
        return searchService.search(new JobSearch.Query(q, occupation, lat, lng, radiusKm, wageMin, type, shift, sort),
                cursor, size, seeker);
    }

    // ---------------------------------------------------------------- saved jobs (job seekers)

    @GetMapping("/api/v1/me/saved-jobs")
    List<JobHit> savedJobs() {
        return searchService.savedJobs(currentUser.id());
    }

    @PutMapping("/api/v1/me/saved-jobs/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void save(@PathVariable UUID jobId) {
        searchService.save(currentUser.id(), jobId);
    }

    @DeleteMapping("/api/v1/me/saved-jobs/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unsave(@PathVariable UUID jobId) {
        searchService.unsave(currentUser.id(), jobId);
    }
}
