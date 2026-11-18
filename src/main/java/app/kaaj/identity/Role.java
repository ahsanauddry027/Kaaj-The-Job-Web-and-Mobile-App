package app.kaaj.identity;

/** A user chooses SEEKER or EMPLOYER at sign-up; only an admin can change a role or create an admin. */
public enum Role {
    SEEKER, EMPLOYER, ADMIN;

    /** The Spring Security authority for this role, for example {@code ROLE_EMPLOYER}. */
    public String authority() {
        return "ROLE_" + name();
    }
}
