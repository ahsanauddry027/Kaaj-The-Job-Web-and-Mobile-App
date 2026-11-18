package app.kaaj.job.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code kaaj.jobs.*}
 *
 * @param expiryDays              a post closes this many days after it is first published (30)
 * @param autoPublish             publish submitted posts straight away; only for running the job module without
 *                                scam screening (the {@code trust} module), which decides every post when this is false
 * @param requireVerifiedEmployer only verified employers' posts go live; others wait as drafts
 */
@ConfigurationProperties(prefix = "kaaj.jobs")
public record JobProperties(
        @DefaultValue("30") int expiryDays,
        @DefaultValue("false") boolean autoPublish,
        @DefaultValue("true") boolean requireVerifiedEmployer) {
}
