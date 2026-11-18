package app.kaaj.identity.internal;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Session-based security for the single-page app (context file §17 "Security setup in words", decision 10):
 * an HttpOnly session cookie, CSRF protection with the token in the {@code XSRF-TOKEN} cookie and the
 * {@code X-XSRF-TOKEN} header, and role rules by path. Ownership checks ("this job is yours") are in the
 * services. Member-1 adds Google sign-in ({@code oauth2Login()}) to this same chain in S4.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contextRepository) throws Exception {
        http
                .csrf(csrf -> csrf.spa())
                .securityContext(context -> context.securityContextRepository(contextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/employer/**").hasRole("EMPLOYER")
                        .requestMatchers("/api/v1/me", "/api/v1/files/**").authenticated()
                        // The job seeker's own pages: profile, saved jobs, applications.
                        .requestMatchers("/api/v1/me/**").hasRole("SEEKER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/jobs/*/applications").hasRole("SEEKER")
                        .requestMatchers("/api/v1/auth/**", "/api/v1/taxonomy/**", "/api/v1/jobs/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) ->
                                writeProblem(response, HttpStatus.UNAUTHORIZED, "auth.required", "Please sign in."))
                        .accessDeniedHandler((request, response, ex) ->
                                writeProblem(response, HttpStatus.FORBIDDEN, "auth.forbidden", "You are not allowed to do this.")))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                // The admin page shows uploaded PDFs in a frame from the same site.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                // An API answers 401 instead of remembering the page for a redirect after sign-in.
                .requestCache(cache -> cache.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        return http.build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(KaajUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    private static void writeProblem(HttpServletResponse response, HttpStatus status, String code, String detail)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status.value()
                + ",\"title\":\"" + status.getReasonPhrase()
                + "\",\"code\":\"" + code
                + "\",\"detail\":\"" + detail + "\"}");
    }
}
