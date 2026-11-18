package app.kaaj.shared;

import java.util.Optional;

/**
 * A small interface to a free text model (context file §10.1 rule 7: "free, swappable and able to fail").
 * The implementation calls OpenAI-compatible chat endpoints: the Gemini API free tier first, then Groq
 * (§13.2), using the API keys in {@code .env}. Every caller has a plain fallback for when this returns
 * nothing: no key, no internet, a quota error or a bad answer.
 */
public interface AiText {

    /** True when at least one provider has an API key, so a call is worth trying. */
    boolean available();

    /**
     * Asks for one JSON object. The user text must already be fenced as data (see {@link #fence}) and must
     * not contain real personal details: free tiers may use what is sent (§13).
     *
     * @return the JSON object as text, or empty when no provider answered with one
     */
    Optional<String> json(String systemPrompt, String userPrompt);

    /**
     * Puts user text inside {@code <tag>…</tag>} so the model treats it as data, after removing anything
     * that looks like that tag, so the text can't close the block early (§10.7, prompt injection).
     */
    static String fence(String tag, String text) {
        String clean = text == null ? "" : text.replaceAll("(?i)</?\\s*" + tag + "\\s*>", " ");
        return "<" + tag + ">\n" + clean.strip() + "\n</" + tag + ">";
    }
}
