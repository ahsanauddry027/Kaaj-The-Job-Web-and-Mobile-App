package app.kaaj.identity.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code kaaj.auth.simple-sign-up}: allows the stand-in email-and-password sign-up until Member-2's email
 * codes (S5) are merged; set it to {@code false} then.
 */
@ConfigurationProperties(prefix = "kaaj.auth")
public record AuthProperties(@DefaultValue("true") boolean simpleSignUp) {
}
