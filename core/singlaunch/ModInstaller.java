package singlaunch;

import com.google.gson.JsonArray;
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
import java.util.function.DoubleConsumer;

public class ModInstaller {
    private static final String GH_API = "https://api.github.com";

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public void installFromListing(ModListing listing, InstanceInfo instance, DoubleConsumer progress) throws IOException {
        if (listing == null || listing.repo == null || listing.repo.isBlank()) {
            throw new IOException("Некорректный мод");
        }
        installFromGithub(listing.repo, listing.hasJava, instance, progress);
    }

    public void installFromGithub(String repo, boolean preferJava, InstanceInfo instance, DoubleConsumer progress) throws IOException {
        repo = normalizeRepo(repo);
        if (preferJava) {
            try {
                installJavaRelease(repo, instance, progress);
                return;
            } catch (IOException ignored) {}
        }

        JsonObject repoInfo = apiGet("/repos/" + repo);
        String branch = repoInfo.get("default_branch").getAsString();
        String language = repoInfo.has("language") && !repoInfo.get("language").isJsonNull()
                ? repoInfo.get("language").getAsString() : "";

        if (isJvmLanguage(language)) {
            installJavaRelease(repo, instance, progress);
        } else {
            installBranchZipball(branch, repo, instance, progress);
        }
    }

    private void installJavaRelease(String repo, InstanceInfo instance, DoubleConsumer progress) throws IOException {
        JsonObject release = apiGet("/repos/" + repo + "/releases/latest");
        JsonArray assets = release.getAsJsonArray("assets");
        if (assets == null || assets.isEmpty()) {
            throw new IOException("У мода нет релизов с JAR");
        }

        JsonObject asset = null;
        for (var element : assets) {
            JsonObject item = element.getAsJsonObject();
            String name = item.get("name").getAsString();
            if (name.startsWith("dexed") && name.endsWith(".jar")) {
                asset = item;
                break;
            }
        }
        if (asset == null) {
            for (var element : assets) {
                JsonObject item = element.getAsJsonObject();
                String name = item.get("name").getAsString();
                if (name.endsWith(".jar")) {
                    asset = item;
                    break;
                }
            }
        }
        if (asset == null) throw new IOException("JAR не найден в релизе");

        String url = asset.get("browser_download_url").getAsString();
        Path temp = downloadToTemp(url, progress);
        installFile(repo, temp, instance);
        Files.deleteIfExists(temp);
    }

    private void installBranchZipball(String branch, String repo, InstanceInfo instance, DoubleConsumer progress) throws IOException {
        String url = GH_API + "/repos/" + repo + "/zipball/" + branch;
        Path temp = downloadToTemp(url, progress);
        installFile(repo, temp, instance);
        Files.deleteIfExists(temp);
    }

    private void installFile(String repo, Path source, InstanceInfo instance) throws IOException {
        Path modsDir = InstanceManager.dataDir(instance).resolve("mods");
        Files.createDirectories(modsDir);

        String baseName = repo.replace("/", "").replace(':', '_').replace(' ', '_');
        String finalName = baseName;
        int count = 1;
        while (Files.exists(modsDir.resolve(finalName + ".zip"))) {
            finalName = baseName + count++;
        }

        Path dest = modsDir.resolve(finalName + ".zip");
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path downloadToTemp(String url, DoubleConsumer progress) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "SingularityLauncher")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                throw new IOException("HTTP " + response.statusCode() + " при загрузке мода");
            }

            Path temp = Files.createTempFile("singularity-mod-", ".download");
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
            if (progress != null) progress.accept(1.0);
            return temp;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Загрузка мода прервана", e);
        }
    }

    private JsonObject apiGet(String path) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GH_API + path))
                .header("User-Agent", "SingularityLauncher")
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IOException("GitHub API: HTTP " + response.statusCode());
            }
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("GitHub API прерван", e);
        }
    }

    private static String normalizeRepo(String repo) {
        String text = repo.trim();
        if (text.startsWith("https://github.com/")) text = text.substring("https://github.com/".length());
        if (text.endsWith("/")) text = text.substring(0, text.length() - 1);
        if (text.endsWith(".git")) text = text.substring(0, text.length() - 4);
        return text;
    }

    private static boolean isJvmLanguage(String language) {
        return "Java".equals(language) || "Kotlin".equals(language)
                || "Groovy".equals(language) || "Scala".equals(language);
    }
}
