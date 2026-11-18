package app.kaaj.identity;

import java.util.Map;
import java.util.UUID;

/**
 * Writes {@code audit_log} rows for admin actions (shared setup S6, Member-3): employer approvals and
 * rejections now; suspensions, document reviews and post removals later.
 */
public interface AuditLog {

    /**
     * @param actorId the admin (or user) who acted
     * @param action  for example {@code EMPLOYER_VERIFIED}
     * @param target  what was acted on, for example {@code employer:0193...}
     * @param details a few extra facts, stored as JSON
     */
    void record(UUID actorId, String action, String target, Map<String, ?> details);
}
