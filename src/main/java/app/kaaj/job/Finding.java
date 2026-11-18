package app.kaaj.job;

/**
 * One result of the rule checks ({@link JobRuleChecks}), shown to the employer on the draft and the post,
 * and to the moderator in the case queue.
 *
 * @param kind  what happens: {@code BLOCK} the post can't go live; {@code REVIEW} an admin looks first;
 *              {@code QUESTION} something the post should answer; {@code NUDGE} a suggestion;
 *              {@code REMOVED} the assistant took it out of the draft
 * @param code  a stable code that the apps translate, for example {@code money-request}
 * @param quote the words from the post it is about, or {@code null}
 */
public record Finding(Kind kind, String code, String quote) {

    public enum Kind { BLOCK, REVIEW, QUESTION, NUDGE, REMOVED }

    public static final String MONEY_REQUEST = "money-request";
    public static final String CONTACT_DETAILS = "contact-details";
    public static final String UNFAIR_REQUIREMENT = "unfair-requirement";
    public static final String EASY_MONEY = "easy-money";
    public static final String OVERSEAS_NO_LICENCE = "overseas-no-licence";
    public static final String WAGE_MISSING = "wage-missing";
    public static final String LONG_DAY = "long-day";
    public static final String AI_HIGH_RISK = "ai-high-risk";
}
