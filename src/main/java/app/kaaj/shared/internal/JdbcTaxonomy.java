package app.kaaj.shared.internal;

import app.kaaj.shared.Taxonomy;
import app.kaaj.shared.Term;
import app.kaaj.shared.TermNames;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Taxonomy search with PostgreSQL trigram similarity (pg_trgm), so "draiver", "ড্রাইভার" and "chalok"
 * all find the driver occupation. Names and synonyms in Bangla, Banglish and English are searched;
 * exact prefix matches come first, then the most similar.
 */
@Component
class JdbcTaxonomy implements Taxonomy {

    private static final RowMapper<Term> TERM = (rs, row) ->
            new Term(rs.getObject("id", UUID.class), rs.getString("name_bn"), rs.getString("name_en"));

    private static final double THRESHOLD = 0.25;

    private final JdbcClient jdbc;

    JdbcTaxonomy(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Term> occupation(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return jdbc.sql("SELECT id, name_bn, name_en FROM occupation WHERE id = :id")
                .param("id", id)
                .query(TERM)
                .optional();
    }

    @Override
    public List<Term> skills(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, Term> found = jdbc.sql("SELECT id, name_bn, name_en FROM skill WHERE id IN (:ids)")
                .param("ids", ids)
                .query(TERM)
                .list()
                .stream()
                .collect(Collectors.toMap(Term::id, Function.identity()));
        return ids.stream().map(found::get).filter(Objects::nonNull).toList();
    }

    @Override
    public List<Term> searchOccupations(String query, int limit) {
        return search("occupation", "occupation_synonym", "occupation_id", query, limit);
    }

    @Override
    public List<Term> searchSkills(String query, int limit) {
        return search("skill", "skill_synonym", "skill_id", query, limit);
    }

    /** Table names come from the two fixed callers above, never from user input. */
    private List<Term> search(String table, String synonymTable, String foreignKey, String query, int limit) {
        int size = Math.clamp(limit, 1, 50);
        String q = query == null ? "" : query.strip();
        if (q.isEmpty()) {
            return jdbc.sql("SELECT id, name_bn, name_en FROM " + table + " ORDER BY name_en LIMIT :size")
                    .param("size", size)
                    .query(TERM)
                    .list();
        }
        String sql = """
                SELECT t.id, t.name_bn, t.name_en
                FROM %1$s t
                WHERE t.name_en ILIKE :contains OR t.name_bn ILIKE :contains
                   OR similarity(t.name_en, CAST(:q AS text)) > :threshold
                   OR similarity(t.name_bn, CAST(:q AS text)) > :threshold
                   OR EXISTS (SELECT 1 FROM %2$s s WHERE s.%3$s = t.id
                              AND (s.term ILIKE :contains OR similarity(s.term, CAST(:q AS text)) > :threshold))
                ORDER BY (t.name_en ILIKE :prefix OR t.name_bn ILIKE :prefix) DESC,
                         GREATEST(similarity(t.name_en, CAST(:q AS text)), similarity(t.name_bn, CAST(:q AS text)),
                                  COALESCE((SELECT max(similarity(s.term, CAST(:q AS text))) FROM %2$s s
                                            WHERE s.%3$s = t.id), 0)) DESC,
                         t.name_en
                LIMIT :size
                """.formatted(table, synonymTable, foreignKey);
        String escaped = escapeLike(q);
        return jdbc.sql(sql)
                .param("q", q)
                .param("contains", "%" + escaped + "%")
                .param("prefix", escaped + "%")
                .param("threshold", THRESHOLD)
                .param("size", size)
                .query(TERM)
                .list();
    }

    @Override
    public List<TermNames> occupationNames() {
        return names("occupation", "occupation_synonym", "occupation_id");
    }

    @Override
    public List<TermNames> skillNames() {
        return names("skill", "skill_synonym", "skill_id");
    }

    /** Table names come from the two fixed callers above, never from user input. */
    private List<TermNames> names(String table, String synonymTable, String foreignKey) {
        String sql = """
                SELECT t.id, t.name_bn, t.name_en,
                       COALESCE(array_agg(s.term ORDER BY s.term) FILTER (WHERE s.term IS NOT NULL), '{}') AS synonyms
                FROM %1$s t LEFT JOIN %2$s s ON s.%3$s = t.id
                GROUP BY t.id, t.name_bn, t.name_en
                ORDER BY t.name_en
                """.formatted(table, synonymTable, foreignKey);
        return jdbc.sql(sql).query((rs, row) -> {
            Term term = TERM.mapRow(rs, row);
            List<String> names = new ArrayList<>();
            names.add(term.nameEn());
            names.add(term.nameBn());
            for (String synonym : (String[]) rs.getArray("synonyms").getArray()) {
                names.add(synonym);
            }
            return new TermNames(term, List.copyOf(names));
        }).list();
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
