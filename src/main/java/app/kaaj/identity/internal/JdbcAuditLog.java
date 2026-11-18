package app.kaaj.identity.internal;

import app.kaaj.identity.AuditLog;
import app.kaaj.shared.Json;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Writes {@code audit_log} rows in the caller's transaction, so an action and its log entry commit together. */
@Component
class JdbcAuditLog implements AuditLog {

    private final JdbcClient jdbc;

    JdbcAuditLog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void record(UUID actorId, String action, String target, Map<String, ?> details) {
        jdbc.sql("""
                        INSERT INTO audit_log (actor_id, action, target, details)
                        VALUES (:actor, :action, :target, CAST(:details AS jsonb))
                        """)
                .param("actor", actorId)
                .param("action", action)
                .param("target", target)
                .param("details", Json.object(details == null ? Map.of() : details))
                .update();
    }
}
