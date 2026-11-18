package app.kaaj.shared;

import java.io.InputStream;
import java.util.Optional;

/**
 * The one file upload service every feature uses (shared setup S7, Member-3). Files are kept private on
 * the server's disk under random keys; tables store only the key, never a path. Files are served only
 * through {@code GET /api/v1/files/{key}} after the owning module's {@link FileAccessPolicy} agrees.
 */
public interface FileStore {

    /**
     * Checks the real file type and size for {@code kind}, re-encodes images (which also removes hidden
     * metadata such as location), and saves the file.
     *
     * @throws KaajException with status 400 when the type or size isn't allowed
     */
    StoredFile store(FileKind kind, byte[] content, String originalName);

    Optional<FileContent> open(String key);

    void delete(String key);

    boolean exists(String key);

    /** A stored file's key, detected type, size and the name it was uploaded with. */
    record StoredFile(String key, String mediaType, long size, String originalName) {
    }

    /** A file read back from the store. */
    record FileContent(String key, String mediaType, long size, InputStream stream) {
    }
}
