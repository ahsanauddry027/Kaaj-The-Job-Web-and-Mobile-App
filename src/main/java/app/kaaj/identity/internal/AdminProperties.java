package app.kaaj.identity.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code kaaj.admin.*}: the first admin, created at startup when no admin exists yet (S6). */
@ConfigurationProperties(prefix = "kaaj.admin")
public record AdminProperties(String email, String password) {
}
