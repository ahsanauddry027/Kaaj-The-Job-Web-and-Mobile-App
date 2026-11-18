package app.kaaj.shared;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Codes for verified documents (context file §15 {@code documents}): driving licence classes, and
 * {@code CERT:<skill id>} for a certificate in a skill. Job posts list the codes they require;
 * skill certification (Member-1) produces them for job seekers.
 */
public record DocumentCode(String value) {

    public static final List<String> DRIVING_CODES = List.of(
            "DRIVING_MOTORCYCLE", "DRIVING_LIGHT", "DRIVING_PRO_LIGHT", "DRIVING_PRO_MEDIUM", "DRIVING_PRO_HEAVY");

    private static final String CERT_PREFIX = "CERT:";

    public DocumentCode {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Unknown document code: " + value);
        }
    }

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        if (DRIVING_CODES.contains(value)) {
            return true;
        }
        if (value.startsWith(CERT_PREFIX)) {
            try {
                UUID.fromString(value.substring(CERT_PREFIX.length()));
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    public static Optional<DocumentCode> parse(String value) {
        return isValid(value) ? Optional.of(new DocumentCode(value)) : Optional.empty();
    }

    public static DocumentCode certificate(UUID skillId) {
        return new DocumentCode(CERT_PREFIX + skillId);
    }

    /** The skill a certificate is for, if this is a certificate code. */
    public Optional<UUID> certificateSkill() {
        return value.startsWith(CERT_PREFIX)
                ? Optional.of(UUID.fromString(value.substring(CERT_PREFIX.length())))
                : Optional.empty();
    }
}
