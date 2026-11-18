package app.kaaj.employer.internal;

import app.kaaj.employer.EmployerKind;
import app.kaaj.employer.VerificationStatus;
import app.kaaj.identity.CurrentUser;
import app.kaaj.shared.KaajException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * The employer's own business profile. Only users with the EMPLOYER role reach {@code /api/v1/employer/**}
 * (see {@code SecurityConfig}), and every call works on the signed-in user's own profile.
 */
@RestController
@RequestMapping("/api/v1/employer/profile")
class EmployerProfileController {

    private final EmployerProfileService service;
    private final CurrentUser currentUser;

    EmployerProfileController(EmployerProfileService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    /** Bangladeshi mobile numbers: 01XXXXXXXXX or +8801XXXXXXXXX, spaces and dashes allowed. */
    static final String BD_MOBILE = "^(\\+?880[-\\s]?|0)1[3-9](?:[-\\s]?\\d){8}$";

    record ProfileRequest(
            @NotBlank @Size(min = 2, max = 120) String name,
            @NotNull EmployerKind kind,
            @NotBlank @Size(max = 300) String address,
            @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @NotBlank @Size(max = 120) String contactPerson,
            @NotBlank @Pattern(regexp = BD_MOBILE) String contactPhone,
            @Email @Size(max = 200) String contactEmail) {

        Employer.Details toDetails() {
            return new Employer.Details(name, kind, address, latitude, longitude, contactPerson, contactPhone, contactEmail);
        }
    }

    /** What the employer sees. The document itself is never sent back, only its name. */
    record ProfileView(
            UUID id, String name, EmployerKind kind, String address, Double latitude, Double longitude,
            String contactPerson, String contactPhone, String contactEmail,
            boolean hasDocument, String documentName, Employer.DocumentType documentType,
            VerificationStatus status, String verificationNote, Instant submittedAt, Instant verifiedAt,
            Instant createdAt, boolean canPublishJobs, List<String> missing) {

        static ProfileView of(Employer e) {
            return new ProfileView(e.getId(), e.getName(), e.getKind(), e.getAddress(), e.getLatitude(), e.getLongitude(),
                    e.getContactPerson(), e.getContactPhone(), e.getContactEmail(),
                    e.getDocumentKey() != null, e.getDocumentName(), e.getDocumentType(),
                    e.getVerificationStatus(), e.getVerificationNote(), e.getSubmittedAt(), e.getVerifiedAt(),
                    e.getCreatedAt(), e.getVerificationStatus() == VerificationStatus.VERIFIED, e.missingForReview());
        }
    }

    @GetMapping
    ProfileView get() {
        return service.findOwn(currentUser.id())
                .map(ProfileView::of)
                .orElseThrow(() -> KaajException.notFound("employer.profile.missing"));
    }

    /** Creates the profile the first time and updates it after that. */
    @PutMapping
    ProfileView save(@Valid @RequestBody ProfileRequest body) {
        return ProfileView.of(service.save(currentUser.id(), body.toDetails()));
    }

    /** Uploads a trade licence or NID (JPEG, PNG or PDF up to 3 MB) and sends the profile for review. */
    @PostMapping(path = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ProfileView uploadDocument(@RequestParam("file") MultipartFile file,
                               @RequestParam(name = "documentType", defaultValue = "TRADE_LICENCE")
                               Employer.DocumentType documentType) {
        try {
            return ProfileView.of(service.uploadDocument(
                    currentUser.id(), file.getBytes(), file.getOriginalFilename(), documentType));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the upload", e);
        }
    }

    /** Sends the profile for review again, for example after a rejection. */
    @PostMapping("/submit")
    ProfileView submit() {
        return ProfileView.of(service.submit(currentUser.id()));
    }
}
