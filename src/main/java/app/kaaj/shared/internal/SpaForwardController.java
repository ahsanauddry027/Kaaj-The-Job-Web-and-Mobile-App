package app.kaaj.shared.internal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * When the React app is built into the jar ({@code mvnw -Pweb package}), a browser may open a client-side
 * route such as {@code /employer/jobs/new} directly. These routes have no dot (so they aren't files) and
 * don't start with {@code api}, {@code v3} or {@code swagger-ui}, so they are sent to {@code index.html}
 * and React Router takes over. In development Vite does this instead.
 */
@Controller
class SpaForwardController {

    private static final String FIRST = "{first:(?!api|v3|swagger-ui)[^.]*}";

    @GetMapping({
            "/",
            "/" + FIRST,
            "/" + FIRST + "/{second:[^.]*}",
            "/" + FIRST + "/{second:[^.]*}/{third:[^.]*}",
            "/" + FIRST + "/{second:[^.]*}/{third:[^.]*}/{fourth:[^.]*}"
    })
    String forward() {
        return "forward:/index.html";
    }
}
