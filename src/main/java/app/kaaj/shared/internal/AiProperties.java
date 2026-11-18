package app.kaaj.shared.internal;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code kaaj.ai.*}: the free AI providers, tried in order. A provider without an API key is skipped.
 *
 * @param enabled   turn every AI call off (the features then use their plain fallbacks)
 * @param timeout   how long to wait for one provider
 * @param providers OpenAI-compatible chat endpoints, for example Gemini and Groq
 */
@ConfigurationProperties(prefix = "kaaj.ai")
public record AiProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("20s") Duration timeout,
        List<Provider> providers) {

    public AiProperties {
        providers = providers == null ? List.of() : List.copyOf(providers);
    }

    /**
     * @param name    for log lines, for example {@code gemini}
     * @param baseUrl for example {@code https://generativelanguage.googleapis.com/v1beta/openai}
     * @param apiKey  from {@code .env}; empty means "not set up"
     * @param model   for example {@code gemini-2.5-flash}
     */
    public record Provider(String name, String baseUrl, String apiKey, String model) {

        boolean configured() {
            return baseUrl != null && !baseUrl.isBlank() && apiKey != null && !apiKey.isBlank()
                    && model != null && !model.isBlank();
        }
    }
}
