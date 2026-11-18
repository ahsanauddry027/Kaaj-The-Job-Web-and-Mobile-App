package app.kaaj.identity.internal;

import app.kaaj.identity.Role;
import app.kaaj.shared.KaajException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sign-up and the signed-in user's details: the rules behind {@link AuthController}, which only deals with the
 * HTTP request and the session.
 */
@Service
class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final Clock clock;

    AuthService(AppUserRepository users, PasswordEncoder passwordEncoder, AuthProperties authProperties, Clock clock) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
        this.clock = clock;
    }

    /**
     * <b>Stand-in</b> sign-up without an email code, until Member-2's S5 replaces it
     * (switched off with {@code kaaj.auth.simple-sign-up=false}). Nobody can sign up as an admin.
     */
    @Transactional
    AppUser register(String email, String password, Role role) {
        if (!authProperties.simpleSignUp()) {
            throw KaajException.notFound("auth.sign-up-closed");
        }
        if (role == Role.ADMIN) {
            throw KaajException.badRequest("auth.role-not-allowed");
        }
        String normalised = AppUser.normalise(email);
        if (users.existsByEmail(normalised)) {
            throw KaajException.conflict("auth.email-taken");
        }
        return users.saveAndFlush(AppUser.withPassword(normalised, passwordEncoder.encode(password), role, clock.instant()));
    }

    @Transactional(readOnly = true)
    AppUser get(UUID id) {
        return users.findById(id).orElseThrow(() -> KaajException.notFound("auth.user-not-found"));
    }
}
