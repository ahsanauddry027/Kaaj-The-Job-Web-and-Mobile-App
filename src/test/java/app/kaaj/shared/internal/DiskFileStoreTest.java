package app.kaaj.shared.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.kaaj.shared.FileKind;
import app.kaaj.shared.FileStore.StoredFile;
import app.kaaj.shared.KaajException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DiskFileStoreTest {

    @TempDir
    Path dir;

    DiskFileStore store;

    @BeforeEach
    void setUp() {
        store = new DiskFileStore(new FileProperties(dir.toString()));
    }

    @Test
    void storesAPngDocumentUnderARandomKeyAndServesItBack() throws IOException {
        StoredFile stored = store.store(FileKind.DOCUMENT, image("png"), "C:\\Users\\me\\trade licence.png");
        assertThat(stored.key()).matches("document-[0-9a-f-]{36}\\.png");
        assertThat(stored.mediaType()).isEqualTo("image/png");
        assertThat(stored.originalName()).isEqualTo("trade licence.png");
        assertThat(store.exists(stored.key())).isTrue();
        assertThat(store.open(stored.key())).hasValueSatisfying(file -> assertThat(file.mediaType()).isEqualTo("image/png"));
        store.delete(stored.key());
        assertThat(store.exists(stored.key())).isFalse();
    }

    @Test
    void checksTheRealTypeNotTheFileName() {
        byte[] text = "this is not a PDF".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> store.store(FileKind.DOCUMENT, text, "licence.pdf"))
                .isInstanceOf(KaajException.class).hasMessage("file.wrong-type");
    }

    @Test
    void acceptsARealPdf() {
        byte[] pdf = "%PDF-1.4\n1 0 obj << /Type /Catalog >> endobj\ntrailer << /Root 1 0 R >>\n%%EOF\n"
                .getBytes(StandardCharsets.US_ASCII);
        assertThat(store.store(FileKind.DOCUMENT, pdf, "licence.pdf").key()).endsWith(".pdf");
    }

    @Test
    void refusesFilesOverTheLimit() {
        byte[] big = new byte[(int) FileKind.DOCUMENT.maxBytes() + 1];
        assertThatThrownBy(() -> store.store(FileKind.DOCUMENT, big, "big.pdf"))
                .isInstanceOf(KaajException.class).hasMessage("file.too-large");
    }

    @Test
    void reencodingRemovesExifDataSuchAsLocation() throws IOException {
        byte[] jpeg = image("jpg");
        byte[] exif = "Exif\0\0GPS-LATITUDE-23.89".getBytes(StandardCharsets.ISO_8859_1);
        // Insert an APP1 (Exif) segment right after the JPEG start marker.
        ByteArrayOutputStream withExif = new ByteArrayOutputStream();
        withExif.write(jpeg, 0, 2);
        withExif.write(new byte[] {(byte) 0xFF, (byte) 0xE1, 0, (byte) (exif.length + 2)});
        withExif.write(exif);
        withExif.write(jpeg, 2, jpeg.length - 2);

        StoredFile stored = store.store(FileKind.DOCUMENT, withExif.toByteArray(), "photo.jpg");

        byte[] saved = Files.readAllBytes(dir.resolve("document").resolve(stored.key()));
        assertThat(new String(saved, StandardCharsets.ISO_8859_1)).doesNotContain("GPS-LATITUDE");
    }

    @Test
    void keysThatLookLikePathsAreNeverOpened() {
        assertThat(store.open("../../etc/passwd")).isEmpty();
        assertThat(store.exists("document-../../x.pdf")).isFalse();
    }

    private static byte[] image(String format) throws IOException {
        BufferedImage image = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
        image.setRGB(5, 5, 0x1D4ED8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}
