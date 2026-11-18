package app.kaaj.trust.internal;

import app.kaaj.job.Finding;
import app.kaaj.job.JobContent;
import app.kaaj.job.JobModeration;
import app.kaaj.job.JobRuleChecks;
import app.kaaj.job.JobStatus;
import app.kaaj.job.JobSubmitted;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Scam screening: every submitted post is checked before it goes live (context file §10.6). Runs after the
 * submit has been saved, in its own transaction, so a failure here never loses the employer's post (it
 * stays in review for an admin).
 * <pre>
 *   any rule BLOCK (asks for money)                          → BLOCKED, case opened
 *   any rule REVIEW, or the AI check says HIGH risk          → IN_REVIEW (admin queue), case opened
 *   the AI check was tried and failed                        → kaaj.trust.when-ai-fails (REVIEW by default)
 *   otherwise                                                → PUBLISHED
 * </pre>
 * The model never publishes or blocks on its own: Java decides from the rules plus the assessment.
 */
@Component
class Screening {

    enum Decision { PUBLISH, REVIEW, BLOCK }

    private static final Logger log = LoggerFactory.getLogger(Screening.class);

    private final JobModeration moderation;
    private final ScamReviewer reviewer;
    private final CaseStore cases;
    private final TrustProperties properties;

    Screening(JobModeration moderation, ScamReviewer reviewer, CaseStore cases, TrustProperties properties) {
        this.moderation = moderation;
        this.reviewer = reviewer;
        this.cases = cases;
        this.properties = properties;
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void onSubmitted(JobSubmitted event) {
        JobContent post = moderation.content(event.jobId()).orElse(null);
        if (post == null || post.status() != JobStatus.IN_REVIEW) {
            return;
        }
        List<Finding> findings = new ArrayList<>(JobRuleChecks.check(post));
        ScamReviewer.Outcome ai = JobRuleChecks.blocks(findings) || JobRuleChecks.needsReview(findings)
                ? ScamReviewer.Outcome.notAsked()       // the rules already decided: save the free quota
                : reviewer.review(post);
        if (ai.state() == ScamReviewer.Outcome.State.ANSWERED && ai.assessment().worthAReview()) {
            findings.add(new Finding(Finding.Kind.REVIEW, Finding.AI_HIGH_RISK,
                    ai.assessment().redFlags() == null || ai.assessment().redFlags().isEmpty()
                            ? null : String.join(" · ", ai.assessment().redFlags())));
        }
        Decision decision = decide(findings, ai.state(), properties.whenAiFails());
        switch (decision) {
            case PUBLISH -> moderation.publish(post.id());
            case REVIEW -> {
                moderation.hold(post.id(), JobModeration.SCREENING_REVIEW);
                cases.openOrUpdate(CaseStore.JOB, post.id(), "SCREENING", findings, ai.assessment(), "REVIEW");
            }
            case BLOCK -> {
                moderation.block(post.id(), JobModeration.SCREENING_BLOCKED);
                cases.openOrUpdate(CaseStore.JOB, post.id(), "SCREENING", findings, ai.assessment(), "BLOCK");
            }
        }
        log.info("Screened job {}: {} ({})", post.id(), decision,
                findings.stream().map(Finding::code).distinct().toList());
    }

    /** The decision table, as a pure function for the tests. */
    static Decision decide(List<Finding> findings, ScamReviewer.Outcome.State ai, Decision whenAiFails) {
        if (JobRuleChecks.blocks(findings)) {
            return Decision.BLOCK;
        }
        if (JobRuleChecks.needsReview(findings)) {
            return Decision.REVIEW;
        }
        if (ai == ScamReviewer.Outcome.State.FAILED) {
            return whenAiFails == Decision.BLOCK ? Decision.REVIEW : whenAiFails;
        }
        return Decision.PUBLISH;
    }
}
