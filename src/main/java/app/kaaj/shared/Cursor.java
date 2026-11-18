package app.kaaj.shared;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * A position in a list sorted by (time, id), for cursor pagination (context file §16). The client gets
 * it as an opaque string ({@code nextCursor}) and sends it back as {@code ?cursor=} for the next page.
 */
public record Cursor(Instant at, UUID id) {

    public String encode() {
        String raw = at.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String value) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
            int bar = raw.indexOf('|');
            return new Cursor(Instant.parse(raw.substring(0, bar)), UUID.fromString(raw.substring(bar + 1)));
        } catch (RuntimeException e) {
            throw KaajException.badRequest("error.bad-cursor");
        }
    }
}
