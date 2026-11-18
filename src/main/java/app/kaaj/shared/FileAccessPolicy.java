package app.kaaj.shared;

import java.util.Optional;
import java.util.UUID;

/**
 * Implemented by each module for the files it owns. {@code GET /api/v1/files/{key}} asks every policy;
 * a policy answers {@code Optional.empty()} for keys that aren't its files. Keys nobody owns, and
 * refused keys, get 404, so a stranger can't even learn that a file exists.
 */
public interface FileAccessPolicy {

    Optional<Boolean> canRead(String key, Viewer viewer);

    /** Who is asking for a file. */
    record Viewer(UUID userId, boolean admin) {
    }
}
