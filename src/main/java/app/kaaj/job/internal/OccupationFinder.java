package app.kaaj.job.internal;

import app.kaaj.shared.ScamPatterns;
import app.kaaj.shared.Taxonomy;
import app.kaaj.shared.Term;
import app.kaaj.shared.TermNames;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Finds taxonomy occupations and skills inside free text ("2 jon pickup driver lagbe, Tongi…"), by their
 * Bangla, Banglish and English names and synonyms. Matching runs in Java, so it works the same for Bangla
 * on any database locale. The longest matching name wins ("pickup driver" before "driver").
 */
@Component
class OccupationFinder {

    /** An occupation or skill, and the name that was found in the text. */
    record Match(Term term, String matchedName) {
    }

    private static final Duration CACHE_FOR = Duration.ofMinutes(10);

    private final Taxonomy taxonomy;
    private final Clock clock;
    private List<TermNames> occupations = List.of();
    private List<TermNames> skills = List.of();
    private Instant loadedAt = Instant.EPOCH;

    OccupationFinder(Taxonomy taxonomy, Clock clock) {
        this.taxonomy = taxonomy;
        this.clock = clock;
    }

    Optional<Match> occupationIn(String text) {
        refresh();
        return best(occupations, text);
    }

    Optional<Match> skillIn(String text) {
        refresh();
        return best(skills, text);
    }

    private synchronized void refresh() {
        Instant now = clock.instant();
        if (Duration.between(loadedAt, now).compareTo(CACHE_FOR) > 0) {
            occupations = taxonomy.occupationNames();
            skills = taxonomy.skillNames();
            loadedAt = now;
        }
    }

    /** Package-private for tests: the best match among the given names. */
    static Optional<Match> best(List<TermNames> terms, String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String haystack = ScamPatterns.normalise(text).toLowerCase(Locale.ROOT);
        Match best = null;
        for (TermNames term : terms) {
            for (String name : term.names()) {
                for (String candidate : variants(name)) {
                    if (candidate.length() >= 3 && contains(haystack, candidate)
                            && (best == null || candidate.length() > best.matchedName().length())) {
                        best = new Match(term.term(), candidate);
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /** "Driver (car, pickup, microbus)" is also found as "driver". */
    private static List<String> variants(String name) {
        String lower = ScamPatterns.normalise(name).toLowerCase(Locale.ROOT).strip();
        int bracket = lower.indexOf('(');
        if (bracket > 0) {
            return List.of(lower, lower.substring(0, bracket).strip());
        }
        return List.of(lower);
    }

    /** English and Banglish names must match whole words; Bangla names match as text. */
    private static boolean contains(String haystack, String name) {
        if (name.chars().allMatch(c -> c < 128)) {
            return Pattern.compile("(?<![a-z0-9])" + Pattern.quote(name) + "(?![a-z0-9])").matcher(haystack).find();
        }
        return haystack.contains(name);
    }
}
