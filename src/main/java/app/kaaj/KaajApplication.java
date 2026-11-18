package app.kaaj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Kaaj – The Job. One Spring Boot application split into modules (context file §11):
 * {@code identity}, {@code employer}, {@code job} and {@code shared} so far. Each module is a
 * package directly under {@code app.kaaj}; other modules may use only its top-level types, and
 * everything in its {@code internal} package stays private ({@code ModularityTests} checks this).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class KaajApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaajApplication.class, args);
    }
}
