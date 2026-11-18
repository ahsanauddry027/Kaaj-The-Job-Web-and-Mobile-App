package app.kaaj.identity;

import java.util.Optional;
import java.util.UUID;

/** Read-only facts about a user, for other modules (for example, the employer's email in the admin queue). */
public interface UserDirectory {

    Optional<UserInfo> find(UUID userId);

    record UserInfo(UUID id, String email, Role role, boolean active) {
    }
}
