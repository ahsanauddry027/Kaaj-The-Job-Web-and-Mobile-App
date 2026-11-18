package app.kaaj.application.internal;

import app.kaaj.application.internal.ApplicationStatus.Actor;
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
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/** One job seeker's application to one job ({@code application}, migration V160). */
@Entity
@Table(name = "application")
public class Application implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "seeker_id")
    private UUID seekerId;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String message;

    @Column(name = "match_score")
    private Float matchScore;

    @Column(name = "interview_at")
    private Instant interviewAt;

    @Column(name = "interview_place")
    private String interviewPlace;

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Transient
    private boolean isNew = true;

    protected Application() {
    }

    static Application apply(UUID jobId, UUID seekerId, String message, double score, UUID idempotencyKey, Instant now) {
        Application application = new Application();
        application.id = UuidV7.next();
        application.jobId = jobId;
        application.seekerId = seekerId;
        application.status = ApplicationStatus.APPLIED;
        application.message = blankToNull(message);
        application.matchScore = (float) score;
        application.idempotencyKey = idempotencyKey;
        application.createdAt = now;
        application.updatedAt = now;
        return application;
    }

    /** Changes the status if the rules allow it; returns the status before the change. */
    ApplicationStatus move(ApplicationStatus next, Actor actor, Instant now) {
        if (!status.canMoveTo(next, actor)) {
            throw KaajException.conflict("application.status.invalid-move", status);
        }
        ApplicationStatus before = status;
        status = next;
        updatedAt = now;
        return before;
    }

    /** Applying again after withdrawing: a fresh start with the new message and score. */
    ApplicationStatus reapply(String newMessage, double score, Instant now) {
        ApplicationStatus before = move(ApplicationStatus.APPLIED, Actor.SEEKER, now);
        message = blankToNull(newMessage);
        matchScore = (float) score;
        interviewAt = null;
        interviewPlace = null;
        return before;
    }

    void scheduleInterview(Instant at, String place) {
        interviewAt = at;
        interviewPlace = blankToNull(place);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
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

    public UUID getJobId() { return jobId; }
    public UUID getSeekerId() { return seekerId; }
    public ApplicationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Float getMatchScore() { return matchScore; }
    public Instant getInterviewAt() { return interviewAt; }
    public String getInterviewPlace() { return interviewPlace; }
    public UUID getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
