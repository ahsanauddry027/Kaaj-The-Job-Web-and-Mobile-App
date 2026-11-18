package app.kaaj.application.internal;

import java.util.EnumSet;
import java.util.Set;

/**
 * An application's status (M2-2). Who may change it, and to what:
 * <pre>
 *   APPLIED ──(employer opens it)──▶ VIEWED
 *   APPLIED / VIEWED ──▶ SHORTLISTED or DECLINED          (employer)
 *   SHORTLISTED ──▶ INTERVIEW, HIRED or DECLINED          (employer)
 *   INTERVIEW ──▶ HIRED or DECLINED                       (employer)
 *   any open status ──▶ WITHDRAWN                         (job seeker)
 *   WITHDRAWN ──▶ APPLIED                                 (job seeker applies again)
 * </pre>
 * HIRED and DECLINED are final.
 */
enum ApplicationStatus {
    APPLIED, VIEWED, SHORTLISTED, INTERVIEW, HIRED, DECLINED, WITHDRAWN;

    enum Actor { SEEKER, EMPLOYER, SYSTEM }

    boolean isOpen() {
        return this != HIRED && this != DECLINED && this != WITHDRAWN;
    }

    boolean canMoveTo(ApplicationStatus next, Actor actor) {
        return switch (actor) {
            case SEEKER -> (next == WITHDRAWN && isOpen()) || (next == APPLIED && this == WITHDRAWN);
            case SYSTEM -> this == APPLIED && next == VIEWED;
            case EMPLOYER -> employerChoices().contains(next);
        };
    }

    /** What the employer can choose next (the buttons on the applicant page). */
    Set<ApplicationStatus> employerChoices() {
        return switch (this) {
            case APPLIED, VIEWED -> EnumSet.of(SHORTLISTED, DECLINED);
            case SHORTLISTED -> EnumSet.of(INTERVIEW, HIRED, DECLINED);
            case INTERVIEW -> EnumSet.of(HIRED, DECLINED);
            case HIRED, DECLINED, WITHDRAWN -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    /** The employer sees the phone number only after shortlisting (the profile keeps it hidden until then). */
    boolean sharesPhone() {
        return this == SHORTLISTED || this == INTERVIEW || this == HIRED;
    }
}
