package app.kaaj.shared.internal;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class SharedConfiguration {

    /** All modules read the time from this clock (UTC), so tests can control it. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
