package app.kaaj.shared.internal;

import app.kaaj.shared.FileAccessPolicy;
import app.kaaj.shared.FileAccessPolicy.Viewer;
import app.kaaj.shared.FileStore;
import app.kaaj.shared.FileStore.FileContent;
import app.kaaj.shared.KaajException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code GET /api/v1/files/{key}}: serves a stored file only when the module that owns it agrees. */
@RestController
@RequestMapping("/api/v1/files")
class FileController {

    private final FileStore store;
    private final List<FileAccessPolicy> policies;

    FileController(FileStore store, List<FileAccessPolicy> policies) {
        this.store = store;
        this.policies = policies;
    }

    @GetMapping("/{key}")
    ResponseEntity<InputStreamResource> get(@PathVariable String key, Authentication authentication) {
        Viewer viewer = new Viewer(
                UUID.fromString(authentication.getName()),
                authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));

        boolean allowed = policies.stream()
                .map(policy -> policy.canRead(key, viewer))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(false);
        if (!allowed) {
            throw KaajException.notFound("file.not-found");
        }
        FileContent file = store.open(key).orElseThrow(() -> KaajException.notFound("file.not-found"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mediaType()))
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(key).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(new InputStreamResource(file.stream()));
    }
}
