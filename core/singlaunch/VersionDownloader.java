package singlaunch;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleConsumer;

public class VersionDownloader {
    private static final String RELEASES_API = "https://api.github.com/repos/Anuken/Mindustry/releases?per_page=30";
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public List<GameVersion> listAvailable() {
        List<GameVersion> versions = new ArrayList<>();
        versions.addAll(scanLegacyLocal());
        versions.addAll(fetchRemote());
        dedupe(versions);
        for (GameVersion version : versions) {
            version.cached = jarPath(version).toFile().exists();
            if (version.cached) version.sizeBytes = jarPath(version).toFile().length();
        }
        versions.sort(Comparator.comparing((GameVersion v) -> v.id).reversed());
        return versions;
    }

    public Path ensureDownloaded(GameVersion version, DoubleConsumer progress) throws IOException {
        Path target = jarPath(version);
        if (Files.exists(target) && Files.size(target) > 0) {
            if (progress != null) progress.accept(1.0);
            return target;
        }

        if (version.downloadUrl == null || version.downloadUrl.isBlank()) {
            throw new IOException("Нет URL для версии " + version.id);
        }

        Files.createDirectories(target.getParent());
        Path temp = target.resolveSibling(target.getFileName() + ".part");

        HttpRequest request = HttpRequest.newBuilder(URI.create(version.downloadUrl))
                .header("User-Agent", "SingularityLauncher")
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                throw new IOException("HTTP " + response.statusCode());
            }

            long total = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(temp)) {
                byte[] buffer = new byte[8192];
                long done = 0;
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    done += read;
                    if (progress != null && total > 0) progress.accept((double) done / total);
                }
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            if (progress != null) progress.accept(1.0);
            return target;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Загрузка прервана", e);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public Path jarPath(GameVersion version) {
        if ("local".equals(version.source)) {
            return Path.of(version.downloadUrl);
        }
        return LauncherPaths.versionsCacheDir().resolve(version.id).resolve("Mindustry.jar");
    }

    private List<GameVersion> scanLegacyLocal() {
        List<GameVersion> local = new ArrayList<>();
        Path legacy = LauncherPaths.legacyVersionsDir();
        if (!Files.isDirectory(legacy)) return local;

        try (var stream = Files.list(legacy)) {
            stream.filter(p -> p.toString().endsWith(".jar")).forEach(path -> {
                String fileName = path.getFileName().toString();
                String id = fileName.replaceFirst("(?i)mindustry", "").replaceFirst("\\.jar$", "");
                if (id.isBlank() || id.startsWith("V")) id = fileName.replace(".jar", "");
                local.add(new GameVersion("local-" + id, fileName.replace(".jar", ""), path.toAbsolutePath().toString(), "local"));
            });
        } catch (IOException ignored) {}
        return local;
    }

    private List<GameVersion> fetchRemote() {
        List<GameVersion> remote = new ArrayList<>();
        HttpRequest request = HttpRequest.newBuilder(URI.create(RELEASES_API))
                .header("User-Agent", "SingularityLauncher")
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) return remote;

            JsonArray releases = JsonParser.parseString(response.body()).getAsJsonArray();
            for (JsonElement element : releases) {
                JsonObject release = element.getAsJsonObject();
                String tag = release.get("tag_name").getAsString();
                String name = release.has("name") ? release.get("name").getAsString() : tag;
                JsonArray assets = release.getAsJsonArray("assets");
                for (JsonElement assetEl : assets) {
                    JsonObject asset = assetEl.getAsJsonObject();
                    String assetName = asset.get("name").getAsString();
                    if (!"Mindustry.jar".equals(assetName)) continue;
                    String url = asset.get("browser_download_url").getAsString();
                    GameVersion version = new GameVersion(tag, name, url, "remote");
                    version.sizeBytes = asset.get("size").getAsLong();
                    remote.add(version);
                    break;
                }
            }
        } catch (Exception ignored) {}
        return remote;
    }

    private void dedupe(List<GameVersion> versions) {
        List<GameVersion> filtered = new ArrayList<>();
        for (GameVersion version : versions) {
            boolean exists = false;
            for (GameVersion kept : filtered) {
                if (kept.id.equals(version.id) || kept.name.equals(version.name)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) filtered.add(version);
        }
        versions.clear();
        versions.addAll(filtered);
    }
}
