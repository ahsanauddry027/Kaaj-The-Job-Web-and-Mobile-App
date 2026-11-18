package app.kaaj.shared;

import java.util.List;

/** A taxonomy term with every name it is known by: its Bangla and English names and all its synonyms. */
public record TermNames(Term term, List<String> names) {
}
