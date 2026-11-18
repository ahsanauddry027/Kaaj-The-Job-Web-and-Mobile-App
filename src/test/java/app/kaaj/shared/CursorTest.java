package app.kaaj.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CursorTest {

    @Test
    void roundTrips() {
        Cursor cursor = new Cursor(Instant.parse("2026-11-05T10:15:30.123456Z"), UUID.randomUUID());
        assertThat(Cursor.decode(cursor.encode())).isEqualTo(cursor);
    }

    @Test
    void refusesGarbage() {
        assertThatThrownBy(() -> Cursor.decode("not-a-cursor")).isInstanceOf(KaajException.class);
    }
}
