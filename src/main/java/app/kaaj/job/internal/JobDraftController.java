package app.kaaj.job.internal;

import app.kaaj.identity.CurrentUser;
import java.util.Locale;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The AI job-post assistant (Member-3, Module 3). Employers only ({@code /api/v1/employer/**}). The answer
 * is a draft for the "Post a job" form; nothing is saved until the employer submits the form.
 */
@RestController
class JobDraftController {

    /** One sentence in Bangla, Banglish or English, up to 500 characters. */
    record DraftRequest(String text) {
    }

    private final JobDrafts drafts;
    private final CurrentUser currentUser;

    JobDraftController(JobDrafts drafts, CurrentUser currentUser) {
        this.drafts = drafts;
        this.currentUser = currentUser;
    }

    /**
     * {@code {"text": "2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta"}} →
     * the draft in both languages, questions for anything missing (in the request's language) and findings.
     * 429 {@code job.draft.limit} after the daily limit ({@code kaaj.jobs.drafts-per-day}).
     */
    @PostMapping("/api/v1/employer/jobs/drafts")
    JobDrafts.Draft draft(@RequestBody DraftRequest body, Locale locale) {
        return drafts.draft(currentUser.id(), body == null ? null : body.text(), locale);
    }
}
