package app.kaaj.employer.internal;

import app.kaaj.employer.EmployerKind;
import app.kaaj.employer.EmployerSummary;
import app.kaaj.employer.VerificationStatus;
import app.kaaj.shared.KaajException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * An employer's business profile and its verification ({@code employer}, migration V130).
 * All status changes go through this class, so the rules in {@link VerificationStatus} always hold.
 */
@Entity
@Table(name = "employer")
public class Employer implements Persistable<UUID> {

    static final float TRUST_UNVERIFIED = 0.5f;
    static final float TRUST_VERIFIED = 0.8f;

    /** Which document the employer uploaded. */
    public enum DocumentType { TRADE_LICENCE, NATIONAL_ID }

    /** The business details the employer fills in. */
    public record Details(String name, EmployerKind kind, String address, Double latitude, Double longitude,
                          String contactPerson, String contactPhone, String contactEmail) {
    }

    @Id
    private UUID id;

    @Column(name = "owner_id")
    private UUID ownerId;

    private String name;

    @Enumerated(EnumType.STRING)
    private EmployerKind kind;

    private String address;
    private Double latitude;
    private Double longitude;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "document_key")
    private String documentKey;

    @Column(name = "document_name")
    private String documentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;

    @Column(name = "verification_note")
    private String verificationNote;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "trust_score")
    private float trustScore;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    private boolean isNew = true;

    protected Employer() {
    }

    public static Employer create(UUID ownerId, Details details, Instant now) {
        Employer employer = new Employer();
        employer.id = UuidV7.next();
        employer.ownerId = ownerId;
        employer.verificationStatus = VerificationStatus.UNVERIFIED;
        employer.trustScore = TRUST_UNVERIFIED;
        employer.createdAt = now;
        employer.apply(details, now);
        return employer;
    }

    /**
     * Saves new business details. A verified employer who changes the business name goes back to
     * {@code PENDING}; a rejected employer who has a document is sent for review again (they fixed something).
     */
    public void updateDetails(Details details, Instant now) {
        boolean nameChanged = !Objects.equals(clean(details.name()), name);
        apply(details, now);
        if (verificationStatus == VerificationStatus.VERIFIED && nameChanged) {
            sendForReview(now);
        } else if (verificationStatus == VerificationStatus.REJECTED && documentKey != null) {
            sendForReview(now);
        }
    }

    /**
     * Attaches the verification document and sends the profile for review.
     *
     * @return the key of the document it replaces, so the caller can delete that file, or {@code null}
     */
    public String attachDocument(String key, String originalName, DocumentType type, Instant now) {
        String previous = documentKey;
        documentKey = key;
        documentName = originalName;
        documentType = type;
        updatedAt = now;
        if (verificationStatus != VerificationStatus.PENDING) {
            sendForReview(now);
        }
        return previous;
    }

    /** Sends the profile for review again, for example after a rejection. */
    public void submit(Instant now) {
        if (verificationStatus == VerificationStatus.PENDING) {
            throw KaajException.conflict("employer.already-pending");
        }
        if (verificationStatus == VerificationStatus.VERIFIED) {
            throw KaajException.conflict("employer.already-verified");
        }
        sendForReview(now);
    }

    public void approve(UUID adminId, String note, Instant now) {
        moveTo(VerificationStatus.VERIFIED, now);
        verificationNote = clean(note);
        verifiedBy = adminId;
        verifiedAt = now;
        trustScore = TRUST_VERIFIED;
    }

    public void reject(UUID adminId, String reason, Instant now) {
        String cleanReason = clean(reason);
        if (cleanReason == null) {
            throw KaajException.invalid(java.util.Map.of("reason", "employer.reject.reason-required"));
        }
        moveTo(VerificationStatus.REJECTED, now);
        verificationNote = cleanReason;
        verifiedBy = adminId;
        verifiedAt = null;
        trustScore = TRUST_UNVERIFIED;
    }

    /** What is still missing before the profile can be reviewed (message codes for the web app). */
    public List<String> missingForReview() {
        List<String> missing = new ArrayList<>();
        if (name == null) missing.add("name");
        if (kind == null) missing.add("kind");
        if (address == null) missing.add("address");
        if (contactPerson == null) missing.add("contactPerson");
        if (contactPhone == null) missing.add("contactPhone");
        if (documentKey == null) missing.add("document");
        return missing;
    }

    public EmployerSummary toSummary() {
        return new EmployerSummary(id, ownerId, name, kind, address, verificationStatus, trustScore);
    }

    private void sendForReview(Instant now) {
        List<String> missing = missingForReview();
        if (!missing.isEmpty()) {
            throw KaajException.conflict("employer.incomplete", String.join(", ", missing));
        }
        moveTo(VerificationStatus.PENDING, now);
        submittedAt = now;
        verifiedAt = null;
        verifiedBy = null;
        verificationNote = null;
        trustScore = TRUST_UNVERIFIED;
    }

    private void moveTo(VerificationStatus next, Instant now) {
        if (!verificationStatus.canMoveTo(next)) {
            throw KaajException.conflict("employer.status.invalid-move", verificationStatus, next);
        }
        verificationStatus = next;
        updatedAt = now;
    }

    private void apply(Details details, Instant now) {
        name = clean(details.name());
        kind = details.kind();
        address = clean(details.address());
        latitude = details.latitude();
        longitude = details.longitude();
        contactPerson = clean(details.contactPerson());
        contactPhone = clean(details.contactPhone());
        contactEmail = clean(details.contactEmail());
        updatedAt = now;
    }

    private static String clean(String text) {
        if (text == null) {
            return null;
        }
        String stripped = text.strip();
        return stripped.isEmpty() ? null : stripped;
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
        isNew = false;
    }

    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public EmployerKind getKind() { return kind; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getContactPerson() { return contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public String getDocumentKey() { return documentKey; }
    public String getDocumentName() { return documentName; }
    public DocumentType getDocumentType() { return documentType; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public String getVerificationNote() { return verificationNote; }
    public Instant getSubmittedAt() { return submittedAt; }
    public UUID getVerifiedBy() { return verifiedBy; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public float getTrustScore() { return trustScore; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
