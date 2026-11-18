package app.kaaj.shared.internal;

import app.kaaj.shared.KaajException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every error into an RFC 9457 {@code ProblemDetail} JSON response in the request's language
 * ({@code Accept-Language: bn} or {@code en}). Form errors come back as {@code errors: {field: message}}
 * so the web app can show them next to the right input.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MessageSource messages;

    ApiExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler(KaajException.class)
    ResponseEntity<ProblemDetail> handleKaaj(KaajException ex, Locale locale) {
        ProblemDetail body = problem(ex.status(), ex.code(), ex.args(), locale);
        if (!ex.fieldErrors().isEmpty()) {
            Map<String, String> errors = new LinkedHashMap<>();
            ex.fieldErrors().forEach((field, code) -> errors.put(field, text(code, null, locale)));
            body.setProperty("errors", errors);
        }
        return ResponseEntity.status(ex.status()).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem(HttpStatus.UNAUTHORIZED, "auth.bad-credentials", null, locale));
    }

    @ExceptionHandler({LockedException.class, DisabledException.class})
    ResponseEntity<ProblemDetail> handleSuspended(AuthenticationException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem(HttpStatus.FORBIDDEN, "auth.suspended", null, locale));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem(HttpStatus.UNAUTHORIZED, "auth.required", null, locale));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, Locale locale) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem(HttpStatus.FORBIDDEN, "auth.forbidden", null, locale));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleIntegrity(DataIntegrityViolationException ex, Locale locale) {
        log.warn("Database constraint refused a change: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem(HttpStatus.CONFLICT, "error.conflict", null, locale));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Locale locale = request.getLocale();
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "error.validation", null, locale);
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), messages.getMessage(error, locale));
        }
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    private ProblemDetail problem(HttpStatus status, String code, Object[] args, Locale locale) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, text(code, args, locale));
        body.setTitle(status.getReasonPhrase());
        body.setProperty("code", code);
        return body;
    }

    private String text(String code, Object[] args, Locale locale) {
        return messages.getMessage(code, args, code, locale);
    }
}
