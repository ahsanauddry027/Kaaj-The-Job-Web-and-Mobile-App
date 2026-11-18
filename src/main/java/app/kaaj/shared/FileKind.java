package app.kaaj.shared;

import java.util.Map;

/**
 * The kinds of file Kaaj stores, with the real types (checked by content, not by name) and size limits
 * from context file §7 "Shared file upload". The map value is the file extension used in the key.
 */
public enum FileKind {

    PHOTO("photo", 2 * 1024 * 1024, Map.of(
            "image/jpeg", "jpg",
            "image/png", "png")),

    DOCUMENT("document", 3 * 1024 * 1024, Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "application/pdf", "pdf")),

    VOICE("voice", 3 * 1024 * 1024, Map.of(
            "audio/webm", "webm",
            "video/webm", "webm",
            "audio/mp4", "m4a",
            "video/mp4", "m4a",
            "video/quicktime", "m4a"));

    private final String folder;
    private final long maxBytes;
    private final Map<String, String> extensionByType;

    FileKind(String folder, long maxBytes, Map<String, String> extensionByType) {
        this.folder = folder;
        this.maxBytes = maxBytes;
        this.extensionByType = extensionByType;
    }

    public String folder() {
        return folder;
    }

    public long maxBytes() {
        return maxBytes;
    }

    public boolean allows(String mediaType) {
        return extensionByType.containsKey(mediaType);
    }

    public String extensionFor(String mediaType) {
        return extensionByType.get(mediaType);
    }

    public static FileKind fromFolder(String folder) {
        for (FileKind kind : values()) {
            if (kind.folder.equals(folder)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown file kind: " + folder);
    }
}
