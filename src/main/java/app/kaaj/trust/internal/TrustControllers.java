package app.kaaj.trust.internal;

import app.kaaj.identity.CurrentUser;
import app.kaaj.shared.CursorPage;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The trust API. Paths and roles: {@code /api/v1/reports} any signed-in user; {@code /api/v1/admin/cases/**}
 * admins ({@code /api/v1/admin/**} in SecurityConfig); {@code /api/v1/employer/jobs/{id}/screening} employers.
 */
@RestController
class TrustControllers {

    private final TrustService trust;
    private final CurrentUser currentUser;

    TrustControllers(TrustService trust, CurrentUser currentUser) {
        this.trust = trust;
        this.currentUser = currentUser;
    }

    /** Report a live post: {@code {"subjectType": "JOB", "subjectId": "…", "reason": "ASKED_FOR_MONEY", "details": "…"}}. */
    @PostMapping("/api/v1/reports")
    ResponseEntity<Map<String, Boolean>> report(@RequestBody TrustViews.ReportRequest body) {
        trust.report(currentUser.id(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("received", true));
    }

    /** The moderation queue: {@code OPEN} (most reported and oldest first), {@code REMOVED} or {@code DISMISSED}. */
    @GetMapping("/api/v1/admin/cases")
    CursorPage<TrustViews.CaseSummary> cases(@RequestParam(defaultValue = "OPEN") String status,
                                             @RequestParam(required = false) String cursor) {
        return trust.list(status, cursor);
    }

    @GetMapping("/api/v1/admin/cases/counts")
    Map<String, Long> counts() {
        return trust.counts();
    }

    @GetMapping("/api/v1/admin/cases/{id}")
    TrustViews.CaseDetail one(@PathVariable UUID id) {
        return trust.detail(id);
    }

    /** {@code {"decision": "REMOVE_POST" | "SUSPEND_EMPLOYER" | "DISMISS", "note": "…"}}. */
    @PostMapping("/api/v1/admin/cases/{id}/decision")
    TrustViews.CaseDetail decide(@PathVariable UUID id, @RequestBody TrustViews.DecisionRequest body) {
        return trust.decide(id, currentUser.id(), body);
    }

    /** For the employer: why their post is in review or blocked. */
    @GetMapping("/api/v1/employer/jobs/{id}/screening")
    TrustViews.ScreeningView screening(@PathVariable UUID id) {
        return trust.screening(currentUser.id(), id);
    }
}
