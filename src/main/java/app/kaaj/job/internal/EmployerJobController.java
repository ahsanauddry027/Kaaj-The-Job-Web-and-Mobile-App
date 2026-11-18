package app.kaaj.job.internal;

import app.kaaj.employer.EmployerSummary;
import app.kaaj.identity.CurrentUser;
import app.kaaj.job.JobStatus;
import app.kaaj.job.internal.JobViews.ActionRequest;
import app.kaaj.job.internal.JobViews.JobInput;
import app.kaaj.job.internal.JobViews.JobView;
import app.kaaj.shared.CursorPage;
import app.kaaj.shared.KaajException;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The employer's job posts. Only EMPLOYER users reach {@code /api/v1/employer/**}; the service makes sure each
 * employer sees and changes only their own posts.
 */
@RestController
@RequestMapping("/api/v1/employer/jobs")
class EmployerJobController {

    private final JobService service;
    private final JobViews views;
    private final CurrentUser currentUser;

    EmployerJobController(JobService service, JobViews views, CurrentUser currentUser) {
        this.service = service;
        this.views = views;
        this.currentUser = currentUser;
    }

    /** "My jobs": newest first, optionally one status, optionally a title search. */
    @GetMapping
    CursorPage<JobView> list(@RequestParam(required = false) JobStatus status,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) String cursor,
                             @RequestParam(defaultValue = "20") int size) {
        UUID user = currentUser.id();
        EmployerSummary employer = service.employerOf(user);
        CursorPage<Job> page = service.list(user, status, q, cursor, size);
        return new CursorPage<>(page.items().stream().map(job -> views.view(job, employer)).toList(), page.nextCursor());
    }

    /** Number of posts in each status, for the tabs and the dashboard. */
    @GetMapping("/summary")
    Map<JobStatus, Long> summary() {
        return service.counts(currentUser.id());
    }

    @GetMapping("/{id}")
    JobView one(@PathVariable UUID id) {
        UUID user = currentUser.id();
        return views.view(service.get(user, id), service.employerOf(user));
    }

    /**
     * Saves a new post. {@code ?submit=true} also submits it: it goes live if the employer is verified, and
     * otherwise waits as a draft until they are. Send an {@code Idempotency-Key} header to make retries safe.
     */
    @PostMapping
    ResponseEntity<JobView> create(@Valid @RequestBody JobInput body,
                                   @RequestParam(defaultValue = "false") boolean submit,
                                   @RequestHeader(name = "Idempotency-Key", required = false) UUID idempotencyKey) {
        UUID user = currentUser.id();
        Job job = service.create(user, body.toDetails(), submit, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(reloaded(user, job));
    }

    /** Edits a post. A published or paused post goes through review again and keeps its expiry date. */
    @PutMapping("/{id}")
    JobView update(@PathVariable UUID id, @Valid @RequestBody JobInput body,
                   @RequestParam(defaultValue = "false") boolean submit) {
        UUID user = currentUser.id();
        return reloaded(user, service.update(user, id, body.toDetails(), submit));
    }

    /** {@code {"action": "SUBMIT" | "PAUSE" | "RESUME" | "CLOSE" | "REPOST"}}. Repost returns the new draft. */
    @PatchMapping("/{id}")
    JobView act(@PathVariable UUID id, @RequestBody ActionRequest body) {
        if (body == null || body.action() == null) {
            throw KaajException.badRequest("job.action.required");
        }
        UUID user = currentUser.id();
        return reloaded(user, service.act(user, id, body.action()));
    }

    /** Deletes a draft. Posts that were ever submitted are closed instead. */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteDraft(currentUser.id(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reads the post again after the change, so the answer shows its status after the event listeners ran
     * (for example PUBLISHED straight after a submit, while auto-publish stands in for screening).
     */
    private JobView reloaded(UUID user, Job job) {
        return views.view(service.get(user, job.getId()), service.employerOf(user));
    }
}
