package app.kaaj.application.internal;

import app.kaaj.application.internal.ApplicationViews.ApplicantView;
import app.kaaj.application.internal.ApplicationViews.MyApplicationView;
import app.kaaj.identity.CurrentUser;
import app.kaaj.job.JobDirectory;
import app.kaaj.job.JobSummary;
import app.kaaj.seeker.SeekerDirectory;
import app.kaaj.seeker.SeekerSummary;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The job seeker's side (M2-2): apply, "My applications" with the timeline, withdraw.
 * {@code POST /api/v1/jobs/{id}/applications} and everything under {@code /api/v1/me/} need a job seeker.
 */
@RestController
class SeekerApplicationController {

    record ApplyRequest(@Size(max = 500) String message) {
    }

    private final ApplicationService service;
    private final ApplicationViews views;
    private final JobDirectory jobs;
    private final CurrentUser currentUser;
    private final Clock clock;

    SeekerApplicationController(ApplicationService service, ApplicationViews views, JobDirectory jobs,
                                CurrentUser currentUser, Clock clock) {
        this.service = service;
        this.views = views;
        this.jobs = jobs;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @PostMapping("/api/v1/jobs/{jobId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    MyApplicationView apply(@PathVariable UUID jobId,
                            @RequestHeader(name = "Idempotency-Key", required = false) UUID idempotencyKey,
                            @Valid @RequestBody(required = false) ApplyRequest body) {
        String message = body == null ? null : body.message();
        Application application = service.apply(currentUser.id(), jobId, message, idempotencyKey);
        return views.mine(application, true, clock.instant());
    }

    /** Newest change first. {@code ?jobId=} narrows it to one post (to show "Applied" on the job page). */
    @GetMapping("/api/v1/me/applications")
    List<MyApplicationView> mine(@RequestParam(required = false) UUID jobId) {
        List<Application> mine = service.mine(currentUser.id()).stream()
                .filter(application -> jobId == null || application.getJobId().equals(jobId))
                .toList();
        Map<UUID, JobSummary> posts = jobs.findAllById(mine.stream().map(Application::getJobId).distinct().toList());
        Instant now = clock.instant();
        return mine.stream().map(application -> views.mine(application, posts.get(application.getJobId()), false, now)).toList();
    }

    @GetMapping("/api/v1/me/applications/{id}")
    MyApplicationView one(@PathVariable UUID id) {
        return views.mine(service.mineOne(currentUser.id(), id), true, clock.instant());
    }

    @PostMapping("/api/v1/me/applications/{id}/withdraw")
    MyApplicationView withdraw(@PathVariable UUID id) {
        return views.mine(service.withdraw(currentUser.id(), id), true, clock.instant());
    }
}
