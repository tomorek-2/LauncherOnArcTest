package singlaunch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceLoader {
    private ResourceLoader() {}

    public static String loadWebPage() throws IOException {
        try (InputStream in = ResourceLoader.class.getResourceAsStream("/web/index.html")) {
            if (in == null) {
                java.nio.file.Path dev = java.nio.file.Path.of("index.html");
                if (java.nio.file.Files.exists(dev)) {
                    return java.nio.file.Files.readString(dev, StandardCharsets.UTF_8);
                }
                throw new IOException("index.html not found");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
