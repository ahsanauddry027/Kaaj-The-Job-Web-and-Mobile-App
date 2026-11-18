package app.kaaj.identity.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ends the session of a user who was suspended after signing in: their next request gets 403
 * {@code auth.suspended} and they are signed out. Added to the security chain in {@link SecurityConfig}
 * (not a component, so it doesn't run twice).
 */
class SuspendedUserFilter extends OncePerRequestFilter {

    private final UserStatusCache statuses;

    SuspendedUserFilter(UserStatusCache statuses) {
        this.statuses = statuses;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && isUserId(authentication.getName())
                && !statuses.isActive(UUID.fromString(authentication.getName()))) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":403,\"title\":\"Forbidden\",\"code\":\"auth.suspended\","
                    + "\"detail\":\"This account is suspended.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isUserId(String name) {
        try {
            UUID.fromString(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
