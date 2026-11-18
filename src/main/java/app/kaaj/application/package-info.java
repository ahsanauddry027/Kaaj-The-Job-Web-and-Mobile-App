/**
 * Application module · one-click apply and tracking (M2-2) and the employer's applicant review (M3-2,
 * rule-based instead of AI), built for the single-member Kotlin/Android version.
 * <p>
 * A job seeker with a profile applies to a published post with one tap (an {@code Idempotency-Key} makes
 * retries safe; one application per job and person), follows its status and can withdraw it. The employer
 * sees the applicants ranked by a transparent match score with reasons and gaps, and moves each one through
 * shortlist, interview, hire or "not selected". Every change is stored as an event for the timeline.
 * <p>
 * Reads other modules only through their public APIs: {@link app.kaaj.job.JobDirectory},
 * {@link app.kaaj.seeker.SeekerDirectory} and {@link app.kaaj.employer.EmployerDirectory}.
 */
package app.kaaj.application;
