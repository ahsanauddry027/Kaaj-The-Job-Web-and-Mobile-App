package app.kaaj.identity.internal;

import app.kaaj.identity.Role;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first admin at startup from {@code KAAJ_ADMIN_EMAIL} and {@code KAAJ_ADMIN_PASSWORD} when no
 * admin exists yet (S6). Admins are never created by a migration, so no password hash sits in Git.
 */
@Component
class FirstAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FirstAdminInitializer.class);

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties admin;
    private final Clock clock;

    FirstAdminInitializer(AppUserRepository users, PasswordEncoder passwordEncoder,
                          AdminProperties admin, Clock clock) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.admin = admin;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.ADMIN)) {
            return;
        }
        if (admin.email() == null || admin.email().isBlank() || admin.password() == null || admin.password().length() < 8) {
            log.warn("No admin account yet. Set KAAJ_ADMIN_EMAIL and KAAJ_ADMIN_PASSWORD (8+ characters) in .env and restart.");
            return;
        }
        if (users.existsByEmail(AppUser.normalise(admin.email()))) {
            log.warn("KAAJ_ADMIN_EMAIL {} already belongs to a non-admin account; choose another email.", admin.email());
            return;
        }
        users.save(AppUser.withPassword(admin.email(), passwordEncoder.encode(admin.password()), Role.ADMIN, clock.instant()));
        log.info("Created the first admin account: {}", admin.email());
    }
}
