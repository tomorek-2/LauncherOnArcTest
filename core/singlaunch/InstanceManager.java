package singlaunch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstanceManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public List<InstanceInfo> list() {
        List<InstanceInfo> result = new ArrayList<>();
        Path dir = LauncherPaths.instancesDir();
        if (!Files.isDirectory(dir)) return result;

        try (var stream = Files.list(dir)) {
            stream.filter(Files::isDirectory).forEach(instanceDir -> {
                Path meta = instanceDir.resolve("instance.json");
                if (!Files.exists(meta)) return;
                try (Reader reader = Files.newBufferedReader(meta, StandardCharsets.UTF_8)) {
                    InstanceInfo info = GSON.fromJson(reader, InstanceInfo.class);
                    if (info != null) {
                        if (info.id == null || info.id.isBlank()) info.id = instanceDir.getFileName().toString();
                        result.add(info);
                    }
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}

        result.sort((a, b) -> Long.compare(a.createdAt, b.createdAt));
        if (result.isEmpty()) {
            result.add(create("default", "Основной", null));
        }
        return result;
    }

    public InstanceInfo create(String id, String name, String versionId) {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString().substring(0, 8);
        InstanceInfo info = new InstanceInfo(id, name, versionId);
        save(info);
        dataDir(info).toFile().mkdirs();
        dataDir(info).resolve("mods").toFile().mkdirs();
        dataDir(info).resolve("saves").toFile().mkdirs();
        return info;
    }

    public void delete(String id) {
        if ("default".equals(id)) return;
        Path dir = LauncherPaths.instancesDir().resolve(id);
        deleteRecursive(dir.toFile());
    }

    public InstanceInfo get(String id) {
        for (InstanceInfo info : list()) {
            if (info.id.equals(id)) return info;
        }
        return list().get(0);
    }

    public void save(InstanceInfo info) {
        Path dir = LauncherPaths.instancesDir().resolve(info.id);
        dir.toFile().mkdirs();
        Path meta = dir.resolve("instance.json");
        try (Writer writer = Files.newBufferedWriter(meta, StandardCharsets.UTF_8)) {
            GSON.toJson(info, writer);
        } catch (IOException ignored) {}
    }

    public static Path dataDir(InstanceInfo info) {
        return LauncherPaths.instancesDir().resolve(info.id).resolve("data");
    }

    private static void deleteRecursive(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] children = file.listFiles();
            if (children != null) {
                for (java.io.File child : children) deleteRecursive(child);
            }
        }
        file.delete();
    }
}
