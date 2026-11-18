package app.kaaj.shared.internal;

import app.kaaj.shared.AiText;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link AiText} over OpenAI-compatible chat endpoints (Gemini's compatibility layer and Groq both offer
 * one), with plain HTTP and JSON so no extra library is needed. Providers are tried in order; the first
 * that answers with a JSON object wins. Prompts and answers are never logged, only failures.
 */
@Component
class OpenAiCompatibleText implements AiText {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleText.class);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** The parts of a chat-completions answer that are used. */
    record ChatResponse(List<Choice> choices) {
    }

    record Choice(Message message) {
    }

    record Message(String content) {
    }

    private final AiProperties properties;
    private final List<AiProperties.Provider> providers;
    private final RestClient http;

    OpenAiCompatibleText(AiProperties properties) {
        this.properties = properties;
        this.providers = properties.providers().stream().filter(AiProperties.Provider::configured).toList();
        SimpleClientHttpRequestFactory requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(properties.timeout());
        requests.setReadTimeout(properties.timeout());
        this.http = RestClient.builder().requestFactory(requests).build();
        if (properties.enabled()) {
            log.info("AI providers with an API key: {}", providers.isEmpty() ? "none (rules and templates only)"
                    : providers.stream().map(AiProperties.Provider::name).toList());
        }
    }

    @Override
    public boolean available() {
        return properties.enabled() && !providers.isEmpty();
    }

    @Override
    public Optional<String> json(String systemPrompt, String userPrompt) {
        if (!available()) {
            return Optional.empty();
        }
        for (AiProperties.Provider provider : providers) {
            try {
                String request = JSON.writeValueAsString(Map.of(
                        "model", provider.model(),
                        "temperature", 0.2,
                        "response_format", Map.of("type", "json_object"),
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt))));
                String answer = http.post()
                        .uri(URI.create(provider.baseUrl().replaceAll("/+$", "") + "/chat/completions"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + provider.apiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(String.class);
                Optional<String> object = firstObject(answer);
                if (object.isPresent()) {
                    return object;
                }
                log.warn("AI provider {} answered without a JSON object", provider.name());
            } catch (RuntimeException e) {
                // Quota errors, timeouts and network failures: try the next provider, then the caller's fallback.
                log.warn("AI provider {} failed: {}", provider.name(), e.getClass().getSimpleName());
            }
        }
        return Optional.empty();
    }

    /** The JSON object in the first choice's message, without any ```json fences around it. */
    static Optional<String> firstObject(String answer) {
        if (answer == null || answer.isBlank()) {
            return Optional.empty();
        }
        ChatResponse response = JSON.readValue(answer, ChatResponse.class);
        if (response.choices() == null || response.choices().isEmpty() || response.choices().getFirst().message() == null) {
            return Optional.empty();
        }
        String content = response.choices().getFirst().message().content();
        if (content == null) {
            return Optional.empty();
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        return start >= 0 && end > start ? Optional.of(content.substring(start, end + 1)) : Optional.empty();
    }
}
