package singlaunch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private LauncherSettings settings;

    public ConfigManager() {
        LauncherPaths.ensureDirs();
        load();
    }

    public LauncherSettings getSettings() {
        return settings;
    }

    public void load() {
        if (!Files.exists(LauncherPaths.configFile())) {
            settings = new LauncherSettings();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(LauncherPaths.configFile(), StandardCharsets.UTF_8)) {
            LauncherSettings loaded = GSON.fromJson(reader, LauncherSettings.class);
            settings = loaded != null ? loaded : new LauncherSettings();
        } catch (IOException e) {
            settings = new LauncherSettings();
        }
    }

    public void save() {
        try {
            Files.createDirectories(LauncherPaths.configFile().getParent());
            try (Writer writer = Files.newBufferedWriter(LauncherPaths.configFile(), StandardCharsets.UTF_8)) {
                GSON.toJson(settings, writer);
            }
        } catch (IOException ignored) {}
    }

    public void update(LauncherSettings next) {
        settings = next;
        save();
    }
}
