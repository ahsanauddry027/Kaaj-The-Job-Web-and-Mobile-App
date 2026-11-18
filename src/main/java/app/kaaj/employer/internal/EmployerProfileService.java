package app.kaaj.employer.internal;

import app.kaaj.employer.EmployerDirectory;
import app.kaaj.employer.EmployerSummary;
import app.kaaj.shared.FileKind;
import app.kaaj.shared.FileStore;
import app.kaaj.shared.FileStore.StoredFile;
import app.kaaj.shared.KaajException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** The employer's own business profile and document, and the public {@link EmployerDirectory}. */
@Service
@Transactional
public class EmployerProfileService implements EmployerDirectory {

    private final EmployerRepository employers;
    private final FileStore files;
    private final Clock clock;

    EmployerProfileService(EmployerRepository employers, FileStore files, Clock clock) {
        this.employers = employers;
        this.files = files;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<Employer> findOwn(UUID ownerId) {
        return employers.findByOwnerId(ownerId);
    }

    /** Creates the profile the first time, and updates it after that. */
    public Employer save(UUID ownerId, Employer.Details details) {
        Instant now = clock.instant();
        Optional<Employer> existing = employers.findByOwnerId(ownerId);
        if (existing.isPresent()) {
            existing.get().updateDetails(details, now);
            return existing.get();
        }
        return employers.save(Employer.create(ownerId, details, now));
    }

    /** Stores the trade licence or NID through the shared {@link FileStore} and sends the profile for review. */
    public Employer uploadDocument(UUID ownerId, byte[] content, String originalName, Employer.DocumentType type) {
        Employer employer = employers.findByOwnerId(ownerId)
                .orElseThrow(() -> KaajException.conflict("employer.profile.missing"));
        StoredFile stored = files.store(FileKind.DOCUMENT, content, originalName);
        // If this transaction fails, remove the new file; if it succeeds, remove the file it replaced.
        String previous;
        try {
            previous = employer.attachDocument(stored.key(), stored.originalName(), type, clock.instant());
        } catch (RuntimeException e) {
            files.delete(stored.key());
            throw e;
        }
        afterTransaction(stored.key(), previous);
        return employer;
    }

    public Employer submit(UUID ownerId) {
        Employer employer = employers.findByOwnerId(ownerId)
                .orElseThrow(() -> KaajException.notFound("employer.profile.missing"));
        employer.submit(clock.instant());
        return employer;
    }

    // ---- EmployerDirectory (public API for other modules) ----

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployerSummary> findById(UUID employerId) {
        return employers.findById(employerId).map(Employer::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployerSummary> findByOwner(UUID ownerUserId) {
        return employers.findByOwnerId(ownerUserId).map(Employer::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, EmployerSummary> findAllById(Collection<UUID> employerIds) {
        return employers.findAllById(employerIds).stream()
                .map(Employer::toSummary)
                .collect(Collectors.toMap(EmployerSummary::id, Function.identity()));
    }

    private void afterTransaction(String newKey, String replacedKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED && replacedKey != null) {
                    files.delete(replacedKey);
                } else if (status != STATUS_COMMITTED) {
                    files.delete(newKey);
                }
            }
        });
    }
}
