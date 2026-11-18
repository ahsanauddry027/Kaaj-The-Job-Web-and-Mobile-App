package app.kaaj.job;

import app.kaaj.shared.WagePeriod;
import java.time.Instant;
import java.util.UUID;

/**
 * The text and the facts of a post that the rule checks and scam screening look at (and that a moderator
 * reads in the case queue).
 */
public record JobContent(
        UUID id,
        UUID employerId,
        String titleBn,
        String titleEn,
        String descriptionBn,
        String descriptionEn,
        Integer wageMin,
        Integer wageMax,
        WagePeriod wagePeriod,
        String hours,
        int openings,
        String areaName,
        JobStatus status,
        String statusReason,
        Instant publishedAt) {

    /** All the free text of the post in one string, for pattern checks. */
    public String allText() {
        StringBuilder text = new StringBuilder();
        for (String part : new String[] {titleBn, titleEn, descriptionBn, descriptionEn, areaName}) {
            if (part != null && !part.isBlank() && text.indexOf(part) < 0) {
                text.append(part).append('\n');
            }
        }
        return text.toString();
    }
}
