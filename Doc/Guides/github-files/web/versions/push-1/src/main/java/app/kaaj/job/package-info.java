/**
 * Job module · Feature 4 (Module 1): post and manage jobs · Member-3 (Ahsan Habib).
 * <p>
 * Verified employers post a job with a title, category, number of openings, wage range (required),
 * location, working hours, shift and requirements; they can edit, pause, close or repost it, and a post
 * closes automatically 30 days after publishing. Posts by employers who aren't verified yet wait as drafts
 * and are submitted automatically when an admin approves the business ({@code EmployerVerified}).
 * <p>
 * Submitting publishes {@link app.kaaj.job.JobSubmitted}. Until scam screening (M3-6) exists, this module
 * publishes the post itself ({@code kaaj.jobs.auto-publish=true}); M3-6 will use {@link app.kaaj.job.JobModeration}.
 */
package app.kaaj.job;
