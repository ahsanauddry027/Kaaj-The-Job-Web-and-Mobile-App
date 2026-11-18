package app.kaaj.trust.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code kaaj.trust.*}
 *
 * @param reportsToHold how many reports take a live post out of search until an admin decides
 * @param reportsPerDay how many reports one user may send in 24 hours
 * @param aiCheck       ask the AI about posts that pass the rules (only when an AI key is set)
 * @param whenAiFails   what happens to a post that passed the rules when the AI check was tried and failed:
 *                      {@code REVIEW} (the admin queue, the safe default) or {@code PUBLISH}
 */
@ConfigurationProperties(prefix = "kaaj.trust")
public record TrustProperties(
        @DefaultValue("3") int reportsToHold,
        @DefaultValue("10") int reportsPerDay,
        @DefaultValue("true") boolean aiCheck,
        @DefaultValue("REVIEW") Screening.Decision whenAiFails) {
}
