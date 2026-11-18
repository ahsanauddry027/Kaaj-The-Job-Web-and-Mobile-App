package app.kaaj.shared.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** {@code kaaj.files.dir}: the folder where uploads are kept (git-ignored). */
@ConfigurationProperties(prefix = "kaaj.files")
public record FileProperties(@DefaultValue("./data/uploads") String dir) {
}
