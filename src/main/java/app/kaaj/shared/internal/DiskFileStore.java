package app.kaaj.shared.internal;

import app.kaaj.shared.FileKind;
import app.kaaj.shared.FileStore;
import app.kaaj.shared.KaajException;
import app.kaaj.shared.UuidV7;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Saves files on the server's disk under {@code kaaj.files.dir}, one folder per {@link FileKind}.
 * The key is {@code <kind>-<uuid v7>.<ext>}, for example {@code document-0193c8b0-....pdf}.
 */
@Component
class DiskFileStore implements FileStore {

    private static final Pattern KEY = Pattern.compile(
            "^(photo|document|voice)-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|pdf|webm|m4a)$");

    private static final Map<String, String> TYPE_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "png", "image/png",
            "pdf", "application/pdf",
            "webm", "audio/webm",
            "m4a", "audio/mp4");

    /** Images larger than this many pixels are refused before decoding (protects memory). */
    private static final long MAX_PIXELS = 40_000_000L;

    private final Path root;
    private final Tika tika = new Tika();

    DiskFileStore(FileProperties properties) {
        this.root = Path.of(properties.dir()).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(FileKind kind, byte[] content, String originalName) {
        if (content == null || content.length == 0) {
            throw KaajException.badRequest("file.empty");
        }
        if (content.length > kind.maxBytes()) {
            throw new KaajException(HttpStatus.PAYLOAD_TOO_LARGE, "file.too-large", kind.maxBytes() / (1024 * 1024));
        }
        // The real type comes from the file's content (its "magic bytes"), never from its name.
        String mediaType = tika.detect(content);
        if (!kind.allows(mediaType)) {
            throw KaajException.badRequest("file.wrong-type");
        }
        byte[] toSave = mediaType.startsWith("image/") ? reencode(content, mediaType) : content;
        String key = kind.folder() + "-" + UuidV7.next() + "." + kind.extensionFor(mediaType);
        try {
            Path folder = Files.createDirectories(root.resolve(kind.folder()));
            Files.write(folder.resolve(key), toSave, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the file", e);
        }
        return new StoredFile(key, mediaType, toSave.length, cleanName(originalName));
    }

    @Override
    public Optional<FileContent> open(String key) {
        return pathOf(key).filter(Files::isRegularFile).map(path -> {
            try {
                String extension = key.substring(key.lastIndexOf('.') + 1);
                return new FileContent(key, TYPE_BY_EXTENSION.get(extension), Files.size(path), Files.newInputStream(path));
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read the file", e);
            }
        });
    }

    @Override
    public void delete(String key) {
        pathOf(key).ifPresent(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not delete the file", e);
            }
        });
    }

    @Override
    public boolean exists(String key) {
        return pathOf(key).map(Files::isRegularFile).orElse(false);
    }

    /** Only well-formed keys map to a path, so a key can never point outside the store (no "../"). */
    private Optional<Path> pathOf(String key) {
        if (key == null) {
            return Optional.empty();
        }
        Matcher matcher = KEY.matcher(key);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(root.resolve(matcher.group(1)).resolve(key));
    }

    /**
     * Decodes and re-encodes an image. This drops everything that isn't pixels, including EXIF data
     * such as the GPS location where a phone photo was taken (context file §19).
     */
    static byte[] reencode(byte[] content, String mediaType) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) {
                throw KaajException.badRequest("file.wrong-type");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw KaajException.badRequest("file.wrong-type");
            }
            ImageReader reader = readers.next();
            BufferedImage image;
            try {
                reader.setInput(input, true, true);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > MAX_PIXELS) {
                    throw KaajException.badRequest("file.image-too-large");
                }
                image = reader.read(0);
            } finally {
                reader.dispose();
            }
            String format = "image/png".equals(mediaType) ? "png" : "jpg";
            if ("jpg".equals(format) && image.getColorModel().hasAlpha()) {
                BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                rgb.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);
                image = rgb;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
            if (!ImageIO.write(image, format, out)) {
                throw KaajException.badRequest("file.wrong-type");
            }
            return out.toByteArray();
        } catch (IOException e) {
            // Broken or unusual images (for example CMYK JPEGs) are refused rather than stored as they are.
            throw KaajException.badRequest("file.unreadable-image");
        }
    }

    private static String cleanName(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"<>|*?:]", "_").strip();
        return base.length() > 120 ? base.substring(base.length() - 120) : base;
    }
}
