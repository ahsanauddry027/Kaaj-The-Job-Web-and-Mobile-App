package app.kaaj.identity;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The signed-in user, for other modules. The session's principal name is the user's ID, whichever way
 * they signed in (email and password now; Google or an email code once Member-1 and Member-2 add them).
 */
@Component
public class CurrentUser {

    public UUID id() {
        return UUID.fromString(authentication().getName());
    }

    /** The user's ID on pages anyone may open (for example job search), or empty for a visitor. */
    public Optional<UUID> idIfSignedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(authentication.getName()));
    }

    public boolean has(Role role) {
        return authentication().getAuthorities().stream()
                .anyMatch(authority -> role.authority().equals(authority.getAuthority()));
    }

    private static Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Not signed in");
        }
        return authentication;
    }
}
