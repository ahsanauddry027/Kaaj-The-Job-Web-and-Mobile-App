package app.kaaj.identity.internal;

import app.kaaj.identity.Role;
import app.kaaj.shared.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/** A Kaaj account ({@code app_user}, context file §15). */
@Entity
@Table(name = "app_user")
public class AppUser implements Persistable<UUID> {

    public enum Status { ACTIVE, SUSPENDED }

    @Id
    private UUID id;

    private String email;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "google_sub")
    private String googleSub;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String locale;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Transient
    private boolean isNew = true;

    protected AppUser() {
    }

    public static AppUser withPassword(String email, String passwordHash, Role role, Instant now) {
        AppUser user = new AppUser();
        user.id = UuidV7.next();
        user.email = normalise(email);
        user.passwordHash = passwordHash;
        user.role = role;
        user.locale = "bn";
        user.status = Status.ACTIVE;
        // Stand-in sign-up has no email code yet (Member-2's S5 adds it), so the address counts as verified.
        user.emailVerified = true;
        user.createdAt = now;
        return user;
    }

    public static String normalise(String email) {
        return email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Status getStatus() {
        return status;
    }

    /** Suspends or reactivates the account (moderation). */
    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLocale() {
        return locale;
    }
}
