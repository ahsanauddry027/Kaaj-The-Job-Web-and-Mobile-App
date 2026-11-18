package app.kaaj.job.internal;

import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.KaajException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Job search (M2-1) over published, unexpired posts: keyword in Bangla, Banglish or English (titles and the
 * occupation's names, with pg_trgm spelling tolerance), category, distance from a point, monthly wage, job
 * type and shift; sorted by newest, nearest or highest wage.
 * <p>
 * Distance uses the haversine formula in SQL, because this build has no PostGIS. The SQL is put together
 * from fixed pieces; every value the user sends is a bound parameter.
 */
@Component
class JobSearch {

    enum Sort { NEWEST, NEAREST, WAGE }

    /** What the user asked for. Any field may be null. */
    record Query(String text, UUID occupationId, Double latitude, Double longitude, Double radiusKm,
                 Integer monthlyWageMin, JobType type, Shift shift, Sort sort) {

        boolean hasPoint() {
            return latitude != null && longitude != null;
        }
    }

    /** One result: the post's ID and its distance in km from the point, if a point was given. */
    record Hit(UUID jobId, Double distanceKm) {
    }

    record Page(List<Hit> hits, String nextCursor) {
    }

    static final int MAX_SIZE = 50;
    private static final int MAX_OFFSET = 10_000;
    private static final String DISTANCE = "6371 * 2 * asin(sqrt(power(sin(radians(j.latitude - :lat) / 2), 2)"
            + " + cos(radians(:lat)) * cos(radians(j.latitude)) * power(sin(radians(j.longitude - :lng) / 2), 2)))";

    private final JdbcClient jdbc;

    JobSearch(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    Page search(Query query, String cursor, int size, Instant now) {
        int pageSize = Math.max(1, Math.min(size, MAX_SIZE));
        int offset = decode(cursor);
        Map<String, Object> params = new HashMap<>();
        params.put("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
        StringBuilder where = new StringBuilder("j.status = 'PUBLISHED' AND j.expires_at > :now");

        String text = query.text() == null ? "" : query.text().trim();
        if (!text.isEmpty()) {
            params.put("q", text);
            params.put("like", "%" + escapeLike(text) + "%");
            where.append("""
                     AND (j.title_bn ILIKE :like OR j.title_en ILIKE :like
                          OR word_similarity(:q, j.title_bn) > 0.4 OR word_similarity(:q, j.title_en) > 0.4
                          OR j.occupation_id IN (
                              SELECT o.id FROM occupation o WHERE o.name_bn ILIKE :like OR o.name_en ILIKE :like
                              UNION
                              SELECT s.occupation_id FROM occupation_synonym s
                              WHERE s.term ILIKE :like OR similarity(s.term, :q) > 0.3))""");
        }
        if (query.occupationId() != null) {
            params.put("occupation", query.occupationId());
            where.append(" AND j.occupation_id = :occupation");
        }
        if (query.type() != null) {
            params.put("type", query.type().name());
            where.append(" AND j.job_type = :type");
        }
        if (query.shift() != null) {
            params.put("shift", query.shift().name());
            where.append(" AND j.shift = :shift");
        }
        if (query.monthlyWageMin() != null) {
            params.put("wageMin", query.monthlyWageMin());
            where.append(" AND j.wage_monthly_max >= :wageMin");
        }

        String distance = "CAST(NULL AS double precision)";
        if (query.hasPoint()) {
            params.put("lat", query.latitude());
            params.put("lng", query.longitude());
            distance = "CASE WHEN j.latitude IS NULL THEN NULL ELSE " + DISTANCE + " END";
            if (query.radiusKm() != null) {
                params.put("radius", query.radiusKm());
                where.append(" AND (j.remote OR (j.latitude IS NOT NULL AND ").append(DISTANCE).append(" <= :radius))");
            }
        }

        Sort sort = query.sort() == null ? Sort.NEWEST : query.sort();
        String order = switch (sort) {
            case NEAREST -> query.hasPoint() ? "distance_km ASC NULLS LAST, j.published_at DESC, j.id DESC"
                    : "j.published_at DESC, j.id DESC";
            case WAGE -> "j.wage_monthly_max DESC NULLS LAST, j.published_at DESC, j.id DESC";
            case NEWEST -> "j.published_at DESC, j.id DESC";
        };

        params.put("limit", pageSize + 1);
        params.put("offset", offset);
        String sql = "SELECT j.id, " + distance + " AS distance_km FROM job j WHERE " + where
                + " ORDER BY " + order + " LIMIT :limit OFFSET :offset";

        List<Hit> rows = jdbc.sql(sql).params(params)
                .query((rs, n) -> new Hit(rs.getObject("id", UUID.class), (Double) rs.getObject("distance_km")))
                .list();
        boolean more = rows.size() > pageSize;
        List<Hit> hits = more ? rows.subList(0, pageSize) : rows;
        String next = more && offset + pageSize <= MAX_OFFSET ? encode(offset + pageSize) : null;
        return new Page(List.copyOf(hits), next);
    }

    /** "%" and "_" typed by the user are searched for literally. */
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    static String encode(int offset) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("o" + offset).getBytes(StandardCharsets.UTF_8));
    }

    static int decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String text = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int offset = Integer.parseInt(text.substring(1));
            if (!text.startsWith("o") || offset < 0 || offset > MAX_OFFSET) {
                throw new IllegalArgumentException(text);
            }
            return offset;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw KaajException.badRequest("error.bad-cursor");
        }
    }
}
