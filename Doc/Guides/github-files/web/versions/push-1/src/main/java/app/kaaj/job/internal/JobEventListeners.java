package app.kaaj.job.internal;

import app.kaaj.employer.EmployerVerified;
import app.kaaj.job.JobSubmitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to events after the transaction that published them has committed, each in its own new transaction.
 */
@Component
class JobEventListeners {

    private static final Logger log = LoggerFactory.getLogger(JobEventListeners.class);

    private final JobService jobs;
    private final JobProperties properties;

    JobEventListeners(JobService jobs, JobProperties properties) {
        this.jobs = jobs;
        this.properties = properties;
    }

    /**
     * Stand-in for scam screening (M3-6): until it exists, a submitted post is published straight away.
     * When M3-6 is merged, set {@code kaaj.jobs.auto-publish=false} and the trust module decides instead.
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void autoPublish(JobSubmitted event) {
        if (properties.autoPublish()) {
            jobs.publish(event.jobId());
        }
    }

    /** An admin approved an employer: their posts that were waiting for verification go live. */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void employerVerified(EmployerVerified event) {
        int submitted = jobs.submitWaitingDrafts(event.employerId());
        if (submitted > 0) {
            log.info("Employer {} was verified: submitted {} waiting post(s)", event.employerId(), submitted);
        }
    }
}
