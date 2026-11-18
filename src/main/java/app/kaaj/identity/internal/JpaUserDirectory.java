package app.kaaj.identity.internal;

import app.kaaj.identity.UserDirectory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaUserDirectory implements UserDirectory {

    private final AppUserRepository users;

    JpaUserDirectory(AppUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfo> find(UUID userId) {
        return users.findById(userId).map(user ->
                new UserInfo(user.getId(), user.getEmail(), user.getRole(), user.getStatus() == AppUser.Status.ACTIVE));
    }
}
