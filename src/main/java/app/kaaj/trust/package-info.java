/**
 * Trust module · Scam screening and moderation (Module 2 in the final requirements) · Member-3 (Ahsan Habib).
 * <p>
 * Every submitted job post is checked before it goes live ({@code JobSubmitted}): the rule checks
 * ({@link app.kaaj.job.JobRuleChecks}, Bangla, Banglish and English patterns) decide first, and an optional
 * AI check looks at posts that pass the rules. A post that asks job seekers for money is blocked; contact
 * details, easy-money offers, overseas jobs without a licence and high AI risk send it to the admin queue;
 * anything else is published. Users can report a post; three reports take it out of search until an admin
 * decides. Admins review flagged and reported posts in a queue and remove the post, suspend the employer or
 * dismiss the case, each with an audit entry. Removing a post publishes {@link app.kaaj.trust.JobRemovedAsScam}
 * so Member-2's notifications can warn its applicants.
 */
package app.kaaj.trust;
