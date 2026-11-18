package app.kaaj.job;

import app.kaaj.shared.ScamPatterns;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The rule checks for job posts (context file §10.4 and §10.6), in one place and in plain Java, used three
 * times: on a draft from the job-post assistant, by scam screening when a post is submitted, and in the
 * moderator's case view. "Rules that matter are code, not prompts" (§10.1).
 * <table>
 *   <caption>Checks</caption>
 *   <tr><th>Check</th><th>Finding</th></tr>
 *   <tr><td>Asks job seekers for money (registration, processing, training, visa or medical fees, deposits)</td><td>BLOCK</td></tr>
 *   <tr><td>Phone numbers, e-mail, "WhatsApp only" in the text</td><td>REVIEW (the assistant removes them: REMOVED)</td></tr>
 *   <tr><td>"Earn Tk 2,000 a day from home", multi-level marketing</td><td>REVIEW</td></tr>
 *   <tr><td>A job abroad without a recruiting licence number</td><td>REVIEW</td></tr>
 *   <tr><td>Gender, age, religion, marital status or appearance</td><td>NUDGE</td></tr>
 *   <tr><td>No wage, or no wage period</td><td>QUESTION</td></tr>
 *   <tr><td>More than 8 working hours a day (the Labour Act's normal day)</td><td>QUESTION: how is overtime paid?</td></tr>
 * </table>
 */
public final class JobRuleChecks {

    /** The Labour Act's normal working day. */
    public static final Duration NORMAL_DAY = Duration.ofHours(8);

    private JobRuleChecks() {
    }

    public static List<Finding> check(JobContent post) {
        Set<Finding> findings = new LinkedHashSet<>(checkText(post.allText()));
        if (post.wageMin() == null || post.wageMax() == null || post.wagePeriod() == null) {
            findings.add(new Finding(Finding.Kind.QUESTION, Finding.WAGE_MISSING, null));
        }
        Duration day = workingDay(post.hours());
        if (day != null && day.compareTo(NORMAL_DAY) > 0) {
            findings.add(new Finding(Finding.Kind.QUESTION, Finding.LONG_DAY, post.hours()));
        }
        return List.copyOf(findings);
    }

    /** The text checks alone, for any piece of text (a title, a description, a chat message). */
    public static List<Finding> checkText(String text) {
        List<Finding> findings = new ArrayList<>();
        for (ScamPatterns.Hit hit : ScamPatterns.find(text)) {
            findings.add(switch (hit.kind()) {
                case MONEY_REQUEST -> new Finding(Finding.Kind.BLOCK, Finding.MONEY_REQUEST, hit.quote());
                case CONTACT -> new Finding(Finding.Kind.REVIEW, Finding.CONTACT_DETAILS, hit.quote());
                case EASY_MONEY -> new Finding(Finding.Kind.REVIEW, Finding.EASY_MONEY, hit.quote());
                case OVERSEAS_NO_LICENCE -> new Finding(Finding.Kind.REVIEW, Finding.OVERSEAS_NO_LICENCE, hit.quote());
                case UNFAIR_REQUIREMENT -> new Finding(Finding.Kind.NUDGE, Finding.UNFAIR_REQUIREMENT, hit.quote());
            });
        }
        return findings;
    }

    public static boolean blocks(List<Finding> findings) {
        return findings.stream().anyMatch(f -> f.kind() == Finding.Kind.BLOCK);
    }

    public static boolean needsReview(List<Finding> findings) {
        return findings.stream().anyMatch(f -> f.kind() == Finding.Kind.REVIEW);
    }

    /**
     * The length of a working day from "HH:mm-HH:mm"; a night shift such as "22:00-06:00" ends the next
     * day. {@code null} when the hours are missing or not in that format.
     */
    public static Duration workingDay(String hours) {
        if (hours == null || !hours.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
            return null;
        }
        try {
            LocalTime start = LocalTime.parse(hours.substring(0, 5));
            LocalTime end = LocalTime.parse(hours.substring(6));
            Duration length = Duration.between(start, end);
            return length.isNegative() || length.isZero() ? length.plusHours(24) : length;
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
