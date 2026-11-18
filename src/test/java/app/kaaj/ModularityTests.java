package app.kaaj;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Fails the build if a module uses another module's internal classes, or if modules depend on each other
 * in a cycle (context file §11). Allowed today: job → employer → identity → shared.
 */
class ModularityTests {

    @Test
    void modulesKeepTheirBoundaries() {
        ApplicationModules.of(KaajApplication.class).verify();
    }
}
