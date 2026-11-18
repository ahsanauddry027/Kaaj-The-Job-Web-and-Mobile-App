package app.kaaj.employer.internal;

import app.kaaj.employer.EmployerKind;
import app.kaaj.employer.VerificationStatus;
import app.kaaj.identity.CurrentUser;
import app.kaaj.identity.UserDirectory;
import app.kaaj.shared.CursorPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The admin verification queue and decisions. Only ADMIN users reach {@code /api/v1/admin/**}. */
@RestController
@RequestMapping("/api/v1/admin/employers")
class AdminEmployerController {

    private final EmployerVerificationService service;
    private final UserDirectory users;
    private final CurrentUser currentUser;

    AdminEmployerController(EmployerVerificationService service, UserDirectory users, CurrentUser currentUser) {
        this.service = service;
        this.users = users;
        this.currentUser = currentUser;
    }

    enum Decision { APPROVE, REJECT }

    record DecisionRequest(@NotNull Decision decision, @Size(max = 500) String reason) {
    }

    record AdminEmployerView(
            UUID id, String name, EmployerKind kind, String address, Double latitude, Double longitude,
            String contactPerson, String contactPhone, String contactEmail, String ownerEmail,
            String documentName, Employer.DocumentType documentType, String documentUrl,
            VerificationStatus status, String verificationNote, Instant submittedAt, Instant verifiedAt,
            Instant createdAt) {
    }

    @GetMapping
    CursorPage<AdminEmployerView> queue(@RequestParam(defaultValue = "PENDING") VerificationStatus status,
                                        @RequestParam(required = false) String cursor,
                                        @RequestParam(defaultValue = "20") int size) {
        CursorPage<Employer> page = service.queue(status, cursor, size);
        return new CursorPage<>(page.items().stream().map(this::view).toList(), page.nextCursor());
    }

    @GetMapping("/counts")
    Map<VerificationStatus, Long> counts() {
        return service.counts();
    }

    @GetMapping("/{id}")
    AdminEmployerView one(@PathVariable UUID id) {
        return view(service.get(id));
    }

    /** {@code APPROVE}, or {@code REJECT} with a reason the employer will see. */
    @PostMapping("/{id}/verification")
    AdminEmployerView decide(@PathVariable UUID id, @Valid @RequestBody DecisionRequest body) {
        Employer employer = switch (body.decision()) {
            case APPROVE -> service.approve(currentUser.id(), id, body.reason());
            case REJECT -> service.reject(currentUser.id(), id, body.reason());
        };
        return view(employer);
    }

    private AdminEmployerView view(Employer e) {
        String ownerEmail = users.find(e.getOwnerId()).map(UserDirectory.UserInfo::email).orElse(null);
        String documentUrl = e.getDocumentKey() == null ? null : "/api/v1/files/" + e.getDocumentKey();
        return new AdminEmployerView(e.getId(), e.getName(), e.getKind(), e.getAddress(), e.getLatitude(),
                e.getLongitude(), e.getContactPerson(), e.getContactPhone(), e.getContactEmail(), ownerEmail,
                e.getDocumentName(), e.getDocumentType(), documentUrl, e.getVerificationStatus(),
                e.getVerificationNote(), e.getSubmittedAt(), e.getVerifiedAt(), e.getCreatedAt());
    }
}
