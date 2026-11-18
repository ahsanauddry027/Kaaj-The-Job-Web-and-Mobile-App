package app.kaaj.shared;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generates time-ordered UUIDs (version 7, RFC 9562), the same kind PostgreSQL 18's {@code uuidv7()} makes.
 * IDs are created in Java so an entity has its ID before it is saved.
 */
public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {
    }

    public static UUID next() {
        long millis = System.currentTimeMillis();
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);

        long msb = (millis & 0xFFFF_FFFF_FFFFL) << 16;            // 48-bit Unix time in milliseconds
        msb |= 0x7000L;                                            // version 7
        msb |= ((random[0] & 0x0FL) << 8) | (random[1] & 0xFFL);  // 12 random bits

        long lsb = 0;
        for (int i = 2; i < 10; i++) {
            lsb = (lsb << 8) | (random[i] & 0xFFL);
        }
        lsb &= 0x3FFF_FFFF_FFFF_FFFFL;                             // clear the two variant bits
        lsb |= 0x8000_0000_0000_0000L;                             // variant 10xx
        return new UUID(msb, lsb);
    }
}
