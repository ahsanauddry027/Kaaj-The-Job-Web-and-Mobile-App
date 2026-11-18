package app.kaaj.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UuidV7Test {

    @Test
    void makesVersion7UuidsThatSortByCreationTime() throws InterruptedException {
        UUID first = UuidV7.next();
        Thread.sleep(2);
        UUID second = UuidV7.next();
        assertThat(first.version()).isEqualTo(7);
        assertThat(first.variant()).isEqualTo(2);
        assertThat(first.toString()).isLessThan(second.toString());
    }
}
