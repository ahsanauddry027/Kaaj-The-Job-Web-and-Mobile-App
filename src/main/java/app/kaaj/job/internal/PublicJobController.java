package app.kaaj.job.internal;

import app.kaaj.employer.EmployerDirectory;
import app.kaaj.job.internal.JobViews.PublicJobView;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * One published post, as job seekers and visitors see it ({@code GET /api/v1/jobs/{id}}, public).
 * Job search and saved jobs are in {@code JobSearchController} (Member-1's feature in the CSE471 plan).
 */
@RestController
class PublicJobController {

    private final JobService service;
    private final JobViews views;
    private final EmployerDirectory employers;

    PublicJobController(JobService service, JobViews views, EmployerDirectory employers) {
        this.service = service;
        this.views = views;
        this.employers = employers;
    }

    @GetMapping("/api/v1/jobs/{id}")
    PublicJobView one(@PathVariable UUID id) {
        Job job = service.getPublished(id);
        return views.publicView(job, employers.findById(job.getEmployerId()).orElse(null));
    }
}
