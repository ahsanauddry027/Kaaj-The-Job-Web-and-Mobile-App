package app.kaaj.identity.internal;

import app.kaaj.identity.AuditLog;
import app.kaaj.identity.Role;
import app.kaaj.identity.UserAdmin;
import app.kaaj.shared.KaajException;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link UserAdmin}: suspend and reactivate, with the audit log. */
@Service
@Transactional
class UserAdminService implements UserAdmin {

    private final AppUserRepository users;
    private final UserStatusCache statuses;
    private final AuditLog audit;

    UserAdminService(AppUserRepository users, UserStatusCache statuses, AuditLog audit) {
        this.users = users;
        this.statuses = statuses;
        this.audit = audit;
    }

    @Override
    public void suspend(UUID userId, UUID actorId, String reason) {
        if (userId.equals(actorId)) {
            throw KaajException.badRequest("user.suspend-self");
        }
        AppUser user = users.findById(userId).orElseThrow(() -> KaajException.notFound("auth.user-not-found"));
        if (user.getRole() == Role.ADMIN) {
            throw KaajException.badRequest("user.suspend-admin");
        }
        if (user.getStatus() != AppUser.Status.SUSPENDED) {
            user.setStatus(AppUser.Status.SUSPENDED);
            audit.record(actorId, "USER_SUSPENDED", "user:" + userId, Map.of("reason", reason == null ? "" : reason));
        }
        statuses.forget(userId);
    }

    @Override
    public void reactivate(UUID userId, UUID actorId, String reason) {
        AppUser user = users.findById(userId).orElseThrow(() -> KaajException.notFound("auth.user-not-found"));
        if (user.getStatus() != AppUser.Status.ACTIVE) {
            user.setStatus(AppUser.Status.ACTIVE);
            audit.record(actorId, "USER_REACTIVATED", "user:" + userId, Map.of("reason", reason == null ? "" : reason));
        }
        statuses.forget(userId);
    }
}
