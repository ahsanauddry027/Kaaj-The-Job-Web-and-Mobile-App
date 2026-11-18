package app.kaaj.employer.internal;

import app.kaaj.shared.FileAccessPolicy;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Employer verification documents (trade licences and NIDs) are visible to admins only. The employer sees
 * the file name and the status, never the file itself (context file §7, §19).
 */
@Component
class EmployerDocumentAccessPolicy implements FileAccessPolicy {

    private final EmployerRepository employers;

    EmployerDocumentAccessPolicy(EmployerRepository employers) {
        this.employers = employers;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Boolean> canRead(String key, Viewer viewer) {
        if (key == null || !key.startsWith("document-") || !employers.existsByDocumentKey(key)) {
            return Optional.empty(); // not an employer document: another module may own it
        }
        return Optional.of(viewer.admin());
    }
}
