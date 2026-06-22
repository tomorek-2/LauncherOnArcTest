package singlaunch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ModBrowserService {
    private static final String[] MOD_JSON_URLS = {
            "https://raw.githubusercontent.com/Anuken/MindustryMods/master/mods.json",
            "https://cdn.jsdelivr.net/gh/anuken/mindustrymods/mods.json"
    };
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<ModListing>>() {}.getType();

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private List<ModListing> cache;

    public synchronized List<ModListing> fetchList() throws IOException {
        if (cache != null) return cache;

        IOException lastError = null;
        for (String url : MOD_JSON_URLS) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .header("User-Agent", "SingularityLauncher")
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) continue;

                List<ModListing> list = GSON.fromJson(response.body(), LIST_TYPE);
                if (list == null) list = new ArrayList<>();
                list.sort(Comparator.comparing((ModListing m) -> m.lastUpdated != null ? m.lastUpdated : "").reversed());
                cache = list;
                return list;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Загрузка списка модов прервана", e);
            } catch (IOException e) {
                lastError = e;
            }
        }
        throw lastError != null ? lastError : new IOException("Не удалось загрузить mods.json");
    }

    public List<ModListing> search(String query) throws IOException {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<ModListing> all = fetchList();
        if (q.isEmpty()) return all;

        List<ModListing> filtered = new ArrayList<>();
        for (ModListing mod : all) {
            if (contains(mod.name, q) || contains(mod.repo, q) || contains(mod.author, q) || contains(mod.internalName, q)) {
                filtered.add(mod);
            }
        }
        return filtered;
    }

    public ModListing findByRepo(String repo) throws IOException {
        for (ModListing mod : fetchList()) {
            if (mod.repo != null && mod.repo.equalsIgnoreCase(repo)) return mod;
        }
        return null;
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
