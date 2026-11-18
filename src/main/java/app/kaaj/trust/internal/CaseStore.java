package app.kaaj.trust.internal;

import app.kaaj.job.Finding;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** The {@code trust_case} and {@code report} tables (V240), with plain SQL. */
@Component
class CaseStore {

    static final String JOB = "JOB";

    /** One row of {@code trust_case}. */
    record CaseRow(
            UUID id, String subjectType, UUID subjectId, String source, List<Finding> findings,
            ScamAssessment aiAssessment, String screening, int reportCount, String status, String decision,
            String note, UUID decidedBy, Instant decidedAt, Instant createdAt, Instant updatedAt) {
    }

    /** One row of {@code report}. */
    record ReportRow(UUID id, UUID reporterId, String reason, String details, Instant createdAt) {
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String COLUMNS = """
            id, subject_type, subject_id, source, findings::text AS findings, ai_assessment::text AS ai_assessment,
            screening, report_count, status, decision, note, decided_by, decided_at, created_at, updated_at""";

    private final JdbcClient jdbc;

    CaseStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // ---------------------------------------------------------------- cases

    Optional<CaseRow> find(UUID caseId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM trust_case WHERE id = :id")
                .param("id", caseId)
                .query(CaseStore::caseRow)
                .optional();
    }

    Optional<CaseRow> findOpen(String subjectType, UUID subjectId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM trust_case WHERE subject_type = :type AND subject_id = :subject AND status = 'OPEN'")
                .param("type", subjectType)
                .param("subject", subjectId)
                .query(CaseStore::caseRow)
                .optional();
    }

    /** The newest case about a subject, open or decided, for the employer's view of their post. */
    Optional<CaseRow> findLatest(String subjectType, UUID subjectId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM trust_case WHERE subject_type = :type AND subject_id = :subject"
                        + " ORDER BY (status = 'OPEN') DESC, created_at DESC LIMIT 1")
                .param("type", subjectType)
                .param("subject", subjectId)
                .query(CaseStore::caseRow)
                .optional();
    }

    /** Opens a case, or updates the subject's open case: screening runs again after every edit. */
    UUID openOrUpdate(String subjectType, UUID subjectId, String source, List<Finding> findings,
                      ScamAssessment ai, String screening) {
        Optional<CaseRow> open = findOpen(subjectType, subjectId);
        if (open.isPresent()) {
            jdbc.sql("""
                            UPDATE trust_case
                            SET findings = CAST(:findings AS jsonb),
                                ai_assessment = COALESCE(CAST(:ai AS jsonb), ai_assessment),
                                screening = COALESCE(:screening, screening),
                                updated_at = now()
                            WHERE id = :id""")
                    .param("findings", json(findings))
                    .param("ai", ai == null ? null : json(ai))
                    .param("screening", screening)
                    .param("id", open.get().id())
                    .update();
            return open.get().id();
        }
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO trust_case (id, subject_type, subject_id, source, findings, ai_assessment, screening)
                        VALUES (:id, :type, :subject, :source, CAST(:findings AS jsonb), CAST(:ai AS jsonb), :screening)""")
                .param("id", id)
                .param("type", subjectType)
                .param("subject", subjectId)
                .param("source", source)
                .param("findings", json(findings))
                .param("ai", ai == null ? null : json(ai))
                .param("screening", screening)
                .update();
        return id;
    }

    /** Adds one report to the case's count and returns the new count. */
    int countReport(UUID caseId) {
        return jdbc.sql("UPDATE trust_case SET report_count = report_count + 1, updated_at = now() WHERE id = :id RETURNING report_count")
                .param("id", caseId)
                .query(Integer.class)
                .single();
    }

    /** Records the admin's decision; returns false if the case was no longer open. */
    boolean decide(UUID caseId, String status, String decision, String note, UUID adminId) {
        return jdbc.sql("""
                        UPDATE trust_case
                        SET status = :status, decision = :decision, note = :note, decided_by = :admin,
                            decided_at = now(), updated_at = now()
                        WHERE id = :id AND status = 'OPEN'""")
                .param("status", status)
                .param("decision", decision)
                .param("note", note)
                .param("admin", adminId)
                .param("id", caseId)
                .update() == 1;
    }

    /**
     * The queue: open cases with the most reports and the oldest first; decided cases newest first.
     * Offset paging (the order isn't by one column, so a key cursor doesn't fit).
     */
    List<CaseRow> list(String status, int offset, int limit) {
        String order = "OPEN".equals(status) ? "report_count DESC, created_at ASC, id" : "decided_at DESC, id";
        return jdbc.sql("SELECT " + COLUMNS + " FROM trust_case WHERE status = :status ORDER BY " + order
                        + " LIMIT :limit OFFSET :offset")
                .param("status", status)
                .param("limit", limit)
                .param("offset", offset)
                .query(CaseStore::caseRow)
                .list();
    }

    long count(String status) {
        return jdbc.sql("SELECT count(*) FROM trust_case WHERE status = :status")
                .param("status", status)
                .query(Long.class)
                .single();
    }

    // ---------------------------------------------------------------- reports

    boolean hasReported(UUID reporterId, String subjectType, UUID subjectId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM report WHERE reporter_id = :reporter AND subject_type = :type AND subject_id = :subject)")
                .param("reporter", reporterId)
                .param("type", subjectType)
                .param("subject", subjectId)
                .query(Boolean.class)
                .single();
    }

    int reportsInLastDay(UUID reporterId) {
        return jdbc.sql("SELECT count(*) FROM report WHERE reporter_id = :reporter AND created_at > now() - interval '1 day'")
                .param("reporter", reporterId)
                .query(Integer.class)
                .single();
    }

    void addReport(UUID caseId, UUID reporterId, String subjectType, UUID subjectId, String reason, String details) {
        jdbc.sql("""
                        INSERT INTO report (id, case_id, reporter_id, subject_type, subject_id, reason, details)
                        VALUES (:id, :case, :reporter, :type, :subject, :reason, :details)""")
                .param("id", UUID.randomUUID())
                .param("case", caseId)
                .param("reporter", reporterId)
                .param("type", subjectType)
                .param("subject", subjectId)
                .param("reason", reason)
                .param("details", details)
                .update();
    }

    /** Reports about a subject (all its cases), newest first. */
    List<ReportRow> reports(String subjectType, UUID subjectId) {
        return jdbc.sql("""
                        SELECT id, reporter_id, reason, details, created_at FROM report
                        WHERE subject_type = :type AND subject_id = :subject ORDER BY created_at DESC""")
                .param("type", subjectType)
                .param("subject", subjectId)
                .query((rs, row) -> new ReportRow(rs.getObject("id", UUID.class), rs.getObject("reporter_id", UUID.class),
                        rs.getString("reason"), rs.getString("details"), instant(rs, "created_at")))
                .list();
    }

    // ---------------------------------------------------------------- mapping

    private static CaseRow caseRow(ResultSet rs, int row) throws SQLException {
        String ai = rs.getString("ai_assessment");
        return new CaseRow(
                rs.getObject("id", UUID.class),
                rs.getString("subject_type"),
                rs.getObject("subject_id", UUID.class),
                rs.getString("source"),
                List.of(JSON.readValue(rs.getString("findings"), Finding[].class)),
                ai == null ? null : JSON.readValue(ai, ScamAssessment.class),
                rs.getString("screening"),
                rs.getInt("report_count"),
                rs.getString("status"),
                rs.getString("decision"),
                rs.getString("note"),
                rs.getObject("decided_by", UUID.class),
                instant(rs, "decided_at"),
                instant(rs, "created_at"),
                instant(rs, "updated_at"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private static String json(Object value) {
        return JSON.writeValueAsString(value);
    }
}
