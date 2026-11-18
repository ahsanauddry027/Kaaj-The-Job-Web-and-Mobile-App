/**
 * Job module · Feature 4 (Module 1): post and manage jobs · Member-3 (Ahsan Habib).
 * <p>
 * Verified employers post a job with a title, category, number of openings, wage range (required),
 * location, working hours, shift and requirements; they can edit, pause, close or repost it, and a post
 * closes automatically 30 days after publishing. Posts by employers who aren't verified yet wait as drafts
 * and are submitted automatically when an admin approves the business ({@code EmployerVerified}).
 * <p>
 * Submitting publishes {@link app.kaaj.job.JobSubmitted}; scam screening (the {@code trust} module) then
 * checks the post with {@link app.kaaj.job.JobRuleChecks} and publishes, holds or blocks it through
 * {@link app.kaaj.job.JobModeration}.
 * <p>
 * Also here: the AI job-post assistant (Module 3, {@code POST /api/v1/employer/jobs/drafts}), which turns one
 * sentence into a draft for the "Post a job" form.
 */
package app.kaaj.job;
