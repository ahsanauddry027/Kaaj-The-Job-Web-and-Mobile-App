package app.kaaj.shared;

import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * A business error with an HTTP status and a message code. The code is looked up in
 * {@code messages.properties} / {@code messages_bn.properties}, so the API answers in the language
 * of the request's {@code Accept-Language} header (context file §16).
 */
public class KaajException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final transient Object[] args;
    private final Map<String, String> fieldErrors;

    public KaajException(HttpStatus status, String code, Object... args) {
        this(status, code, Map.of(), args);
    }

    public KaajException(HttpStatus status, String code, Map<String, String> fieldErrors, Object... args) {
        super(code);
        this.status = status;
        this.code = code;
        this.args = args;
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public Object[] args() {
        return args;
    }

    /** Field name → message code, for errors that belong next to a form field. */
    public Map<String, String> fieldErrors() {
        return fieldErrors;
    }

    public static KaajException notFound(String code) {
        return new KaajException(HttpStatus.NOT_FOUND, code);
    }

    public static KaajException conflict(String code, Object... args) {
        return new KaajException(HttpStatus.CONFLICT, code, args);
    }

    public static KaajException forbidden(String code, Object... args) {
        return new KaajException(HttpStatus.FORBIDDEN, code, args);
    }

    /** 429: a daily limit was reached (job-post drafts, reports). */
    public static KaajException tooManyRequests(String code, Object... args) {
        return new KaajException(HttpStatus.TOO_MANY_REQUESTS, code, args);
    }

    public static KaajException badRequest(String code, Object... args) {
        return new KaajException(HttpStatus.BAD_REQUEST, code, args);
    }

    public static KaajException invalid(Map<String, String> fieldErrors) {
        return new KaajException(HttpStatus.BAD_REQUEST, "error.validation", fieldErrors);
    }
}
