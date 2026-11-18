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
 * The employer's side (M3-2, rule-based): applicants ranked by match, one applicant (opening it marks it
 * viewed), and the decision. Everything under {@code /api/v1/employer/} needs an employer.
 */
@RestController
class EmployerApplicantController {

    record DecisionRequest(ApplicationStatus status, @Size(max = 500) String note, Instant interviewAt,
                           @Size(max = 200) String interviewPlace) {
    }

    private final ApplicationService service;
    private final ApplicationViews views;
    private final JobDirectory jobs;
    private final SeekerDirectory seekers;
    private final CurrentUser currentUser;

    EmployerApplicantController(ApplicationService service, ApplicationViews views, JobDirectory jobs,
                                SeekerDirectory seekers, CurrentUser currentUser) {
        this.service = service;
        this.views = views;
        this.jobs = jobs;
        this.seekers = seekers;
        this.currentUser = currentUser;
    }

    /** Best match first, then who applied first. */
    @GetMapping("/api/v1/employer/jobs/{jobId}/applicants")
    List<ApplicantView> applicants(@PathVariable UUID jobId) {
        UUID user = currentUser.id();
        JobSummary job = service.ownJob(user, jobId);
        List<Application> found = service.applicants(user, jobId);
        Map<UUID, SeekerSummary> people = seekers.findAll(found.stream().map(Application::getSeekerId).toList());
        return found.stream()
                .map(application -> views.applicant(application, job, people.get(application.getSeekerId()), false))
                .sorted(Comparator.comparingDouble(ApplicantView::score).reversed()
                        .thenComparing(ApplicantView::appliedAt))
                .toList();
    }

    @GetMapping("/api/v1/employer/applications/{id}")
    ApplicantView one(@PathVariable UUID id) {
        Application application = service.open(currentUser.id(), id);
        return view(application);
    }

    @PostMapping("/api/v1/employer/applications/{id}/status")
    ApplicantView decide(@PathVariable UUID id, @Valid @RequestBody DecisionRequest body) {
        Application application = service.decide(currentUser.id(), id, body.status(), body.note(),
                body.interviewAt(), body.interviewPlace());
        return view(application);
    }

    private ApplicantView view(Application application) {
        JobSummary job = jobs.findById(application.getJobId()).orElseThrow();
        SeekerSummary seeker = seekers.find(application.getSeekerId()).orElse(null);
        return views.applicant(application, job, seeker, true);
    }
}
