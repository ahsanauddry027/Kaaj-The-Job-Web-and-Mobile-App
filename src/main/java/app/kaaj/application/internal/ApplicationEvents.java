package app.kaaj.application.internal;

import app.kaaj.application.internal.ApplicationStatus.Actor;
import app.kaaj.shared.UuidV7;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** The timeline of an application ({@code application_event}): every status change, who made it and a note. */
@Component
class ApplicationEvents {

    record Event(ApplicationStatus fromStatus, ApplicationStatus toStatus, Actor actor, String note, Instant at) {
    }

    private final JdbcClient jdbc;

    ApplicationEvents(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    void record(UUID applicationId, ApplicationStatus from, ApplicationStatus to, Actor actor, String note, Instant now) {
        jdbc.sql("""
                        INSERT INTO application_event (id, application_id, from_status, to_status, actor, note, created_at)
                        VALUES (:id, :application, :from, :to, :actor, :note, :at)""")
                .param("id", UuidV7.next())
                .param("application", applicationId)
                .param("from", from == null ? null : from.name())
                .param("to", to.name())
                .param("actor", actor.name())
                .param("note", note == null || note.isBlank() ? null : note.trim())
                .param("at", OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .update();
    }

    List<Event> timeline(UUID applicationId) {
        return jdbc.sql("""
                        SELECT from_status, to_status, actor, note, created_at FROM application_event
                        WHERE application_id = :application ORDER BY created_at, id""")
                .param("application", applicationId)
                .query((rs, n) -> new Event(
                        rs.getString("from_status") == null ? null : ApplicationStatus.valueOf(rs.getString("from_status")),
                        ApplicationStatus.valueOf(rs.getString("to_status")),
                        Actor.valueOf(rs.getString("actor")),
                        rs.getString("note"),
                        rs.getObject("created_at", OffsetDateTime.class).toInstant()))
                .list();
    }
}
