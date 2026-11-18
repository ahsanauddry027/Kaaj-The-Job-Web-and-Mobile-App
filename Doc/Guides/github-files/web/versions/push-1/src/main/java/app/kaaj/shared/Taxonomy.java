package app.kaaj.shared;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Looks up occupations and skills. Job posts use it for the category and the required skills. */
public interface Taxonomy {

    Optional<Term> occupation(UUID id);

    /** The skills with these IDs, in the given order; unknown IDs are left out. */
    List<Term> skills(Collection<UUID> ids);

    /** Occupations whose names or synonyms match {@code query} (Bangla, Banglish or English), best first. */
    List<Term> searchOccupations(String query, int limit);

    /** Skills whose names or synonyms match {@code query}, best first. */
    List<Term> searchSkills(String query, int limit);
}
