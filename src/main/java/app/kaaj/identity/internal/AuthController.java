package app.kaaj.identity.internal;

import app.kaaj.identity.CurrentUser;
import app.kaaj.identity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sign-in endpoints. The controller handles the HTTP request and the session; the rules (who may sign up,
 * unique email, password hashing) are in {@link AuthService}.
 * <ul>
 *   <li>{@code GET /api/v1/auth/csrf} sets the {@code XSRF-TOKEN} cookie; the web app calls it first.</li>
 *   <li>{@code POST /api/v1/auth/login} checks email and password and starts a session.</li>
 *   <li>{@code POST /api/v1/auth/register}: <b>stand-in</b> sign-up without an email code, until Member-2's
 *       S5 replaces it (switch off with {@code kaaj.auth.simple-sign-up=false}).</li>
 *   <li>{@code POST /api/v1/auth/logout} is handled by Spring Security (see {@code SecurityConfig}).</li>
 *   <li>{@code GET /api/v1/me}: the signed-in user.</li>
 * </ul>
 */
@RestController
class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final AuthService auth;
    private final CurrentUser currentUser;

    AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contextRepository,
                   AuthService auth, CurrentUser currentUser) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.auth = auth;
        this.currentUser = currentUser;
    }

    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    record RegisterRequest(@NotBlank @Email @Size(max = 200) String email,
                           @NotBlank @Size(min = 8, max = 100) String password,
                           @NotNull Role role) {
    }

    record MeResponse(UUID id, String email, Role role, String locale) {
    }

    @GetMapping("/api/v1/auth/csrf")
    ResponseEntity<Void> csrf(CsrfToken token) {
        token.getToken(); // reading the deferred token makes Spring Security write the XSRF-TOKEN cookie
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/auth/login")
    MeResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(AppUser.normalise(body.email()), body.password()));
        startSession(authentication, request, response);
        return me(UUID.fromString(authentication.getName()));
    }

    @PostMapping("/api/v1/auth/register")
    MeResponse register(@Valid @RequestBody RegisterRequest body, HttpServletRequest request, HttpServletResponse response) {
        AppUser user = auth.register(body.email(), body.password(), body.role());
        return login(new LoginRequest(user.getEmail(), body.password()), request, response);
    }

    @GetMapping("/api/v1/me")
    MeResponse me() {
        return me(currentUser.id());
    }

    private MeResponse me(UUID id) {
        AppUser user = auth.get(id);
        return new MeResponse(user.getId(), user.getEmail(), user.getRole(), user.getLocale());
    }

    /** Stores the signed-in user in a new session (a fresh session ID prevents session fixation). */
    private void startSession(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }
}
