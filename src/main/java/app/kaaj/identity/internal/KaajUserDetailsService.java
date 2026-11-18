package app.kaaj.identity.internal;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a user by email for the password check. The resulting principal's name is the user's ID
 * (not the email), so {@code CurrentUser.id()} works the same for every sign-in method.
 */
@Service
class KaajUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    KaajUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser user = users.findByEmail(AppUser.normalise(email))
                .orElseThrow(() -> new UsernameNotFoundException("No account for this email"));
        return User.withUsername(user.getId().toString())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .authorities(user.getRole().authority())
                .accountLocked(user.getStatus() == AppUser.Status.SUSPENDED)
                .build();
    }
}
