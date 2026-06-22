package singlaunch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.web.WebEngine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class JavaBridge {
    private static final Gson GSON = new GsonBuilder().create();

    private final ConfigManager configManager;
    private final InstanceManager instanceManager;
    private final VersionDownloader versionDownloader;
    private final GameLauncher gameLauncher;
    private final ModBrowserService modBrowserService;
    private final ModInstaller modInstaller;
    private final WebEngine webEngine;
    private final Consumer<String> status;

    public JavaBridge(ConfigManager configManager, InstanceManager instanceManager,
                      VersionDownloader versionDownloader, GameLauncher gameLauncher,
                      WebEngine webEngine, Consumer<String> status) {
        this.configManager = configManager;
        this.instanceManager = instanceManager;
        this.versionDownloader = versionDownloader;
        this.gameLauncher = gameLauncher;
        this.modBrowserService = new ModBrowserService();
        this.modInstaller = new ModInstaller();
        this.webEngine = webEngine;
        this.status = status;
    }

    public String getBootstrapData() {
        LauncherSettings settings = configManager.getSettings();
        List<InstanceInfo> instances = instanceManager.list();
        List<GameVersion> versions = versionDownloader.listAvailable();

        if (settings.selectedInstanceId == null || settings.selectedInstanceId.isBlank()) {
            settings.selectedInstanceId = instances.get(0).id;
        }
        if ((settings.selectedVersionId == null || settings.selectedVersionId.isBlank()) && !versions.isEmpty()) {
            settings.selectedVersionId = versions.get(0).id;
        }

        InstanceInfo instance = instanceManager.get(settings.selectedInstanceId);
        GameVersion version = findVersion(resolveVersionId(instance, settings));
        int[] parsed = version != null
                ? GameVersionUtil.parse(version.id, version.name)
                : new int[]{0, 0};

        Map<String, Object> payload = new HashMap<>();
        payload.put("settings", settings);
        payload.put("instances", instances);
        payload.put("versions", versions);
        payload.put("launcherDir", LauncherPaths.root().toAbsolutePath().toString());
        payload.put("systemRamMb", (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024)));
        payload.put("gameBuild", parsed[0]);
        payload.put("gameRevision", parsed[1]);
        payload.put("gameVersionLabel", GameVersionUtil.format(parsed[0], parsed[1]));
        payload.put("installedModKeys", listInstalledModKeys(instance));
        return GSON.toJson(payload);
    }

    public void saveSettings(String json) {
        LauncherSettings next = GSON.fromJson(json, LauncherSettings.class);
        if (next == null) return;
        configManager.update(next);
        runJs("onSettingsSaved()");
        status.accept("Настройки сохранены");
    }

    public void selectInstance(String id) {
        LauncherSettings settings = configManager.getSettings();
        settings.selectedInstanceId = id;
        configManager.save();
        InstanceInfo instance = instanceManager.get(id);
        runJs("onInstanceSelected(" + jsQuote(instance.name) + ")");
        refreshData();
    }

    public void selectVersion(String id) {
        LauncherSettings settings = configManager.getSettings();
        settings.selectedVersionId = id;
        configManager.save();
        runJs("onVersionSelected(" + jsQuote(id) + ")");
    }

    public void createInstance(String name, String versionId) {
        if (name == null || name.isBlank()) name = "Инстанс";
        InstanceInfo created = instanceManager.create(null, name.trim(), versionId);
        LauncherSettings settings = configManager.getSettings();
        settings.selectedInstanceId = created.id;
        configManager.save();
        refreshData();
        runJs("showToast('Создан инстанс: " + escapeJs(created.name) + "')");
    }

    public void deleteInstance(String id) {
        instanceManager.delete(id);
        LauncherSettings settings = configManager.getSettings();
        if (id.equals(settings.selectedInstanceId)) {
            settings.selectedInstanceId = instanceManager.list().get(0).id;
            configManager.save();
        }
        refreshData();
    }

    public void play() {
        LauncherSettings settings = configManager.getSettings();
        InstanceInfo instance = instanceManager.get(settings.selectedInstanceId);
        GameVersion version = findVersion(resolveVersionId(instance, settings));
        if (version == null) {
            status.accept("Версия не выбрана");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Загрузка " + version.name + "...");
                Platform.runLater(() -> runJs("setDownloadProgress(0, 'Загрузка...')"));

                versionDownloader.ensureDownloaded(version, progress ->
                        Platform.runLater(() -> runJs("setDownloadProgress(" + progress + ", 'Загрузка...')")));
                var gameJar = versionDownloader.jarPath(version);

                updateMessage("Запуск...");
                Platform.runLater(() -> runJs("setDownloadProgress(1, 'Запуск...')"));
                gameLauncher.launch(instance, version, settings, gameJar);
                return null;
            }

            @Override
            protected void succeeded() {
                runJs("setDownloadProgress(-1, '')");
                status.accept("Запущено: " + instance.name);
                if (!configManager.getSettings().keepLauncherOpen) {
                    Platform.exit();
                }
            }

            @Override
            protected void failed() {
                runJs("setDownloadProgress(-1, '')");
                Throwable error = getException();
                String message = error != null && error.getMessage() != null ? error.getMessage() : "Ошибка запуска";
                status.accept(message);
                runJs("showToast(" + jsQuote(message) + ")");
            }
        };

        task.setOnRunning(e -> status.accept(task.getMessage()));
        Thread thread = new Thread(task, "game-launch");
        thread.setDaemon(true);
        thread.start();
    }

    public void downloadVersion(String id) {
        GameVersion version = findVersion(id);
        if (version == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                versionDownloader.ensureDownloaded(version, progress ->
                        Platform.runLater(() -> runJs("setDownloadProgress(" + progress + ", 'Загрузка " + escapeJs(version.name) + "')")));
                return null;
            }

            @Override
            protected void succeeded() {
                runJs("setDownloadProgress(-1, '')");
                refreshData();
                runJs("showToast('Версия загружена')");
            }

            @Override
            protected void failed() {
                runJs("setDownloadProgress(-1, '')");
                Throwable error = getException();
                runJs("showToast(" + jsQuote(error != null ? error.getMessage() : "Ошибка загрузки") + ")");
            }
        };
        new Thread(task, "version-download").start();
    }

    public void openInstanceFolder() {
        InstanceInfo instance = instanceManager.get(configManager.getSettings().selectedInstanceId);
        openFolder(InstanceManager.dataDir(instance).toFile());
    }

    public void openModsFolder() {
        InstanceInfo instance = resolveModsInstance(null);
        File mods = InstanceManager.dataDir(instance).resolve("mods").toFile();
        openFolder(mods);
    }

    public void settings() {
        runJs("openPanel('settings')");
    }

    public void instances() {
        runJs("openPanel('instances')");
    }

    public void mods() {
        runJs("openPanel('mods')");
        loadMods("");
    }

    public void loadMods(String query) {
        loadMods(query, null);
    }

    public void loadMods(String query, String instanceId) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                List<ModListing> mods = modBrowserService.search(query);
                InstanceInfo instance = resolveModsInstance(instanceId);
                GameVersion version = findVersion(resolveVersionId(instance, configManager.getSettings()));
                int[] parsed = version != null
                        ? GameVersionUtil.parse(version.id, version.name)
                        : new int[]{0, 0};
                Set<String> installed = listInstalledModKeys(instance);

                List<Map<String, Object>> rows = new ArrayList<>();
                for (ModListing mod : mods) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("repo", mod.repo);
                    row.put("name", mod.name);
                    row.put("author", mod.author);
                    row.put("description", mod.description);
                    row.put("stars", mod.stars);
                    row.put("hasJava", mod.hasJava);
                    row.put("minGameVersion", mod.minGameVersion);
                    row.put("compatible", GameVersionUtil.isAtLeast(parsed[0], parsed[1], mod.minGameVersion));
                    row.put("installed", installed.contains(repoKey(mod.repo)));
                    rows.add(row);
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("mods", rows);
                payload.put("instanceId", instance.id);
                payload.put("instanceName", instance.name);
                payload.put("gameVersionLabel", GameVersionUtil.format(parsed[0], parsed[1]));
                payload.put("gameBuild", parsed[0]);
                payload.put("gameRevision", parsed[1]);
                return GSON.toJson(payload);
            }

            @Override
            protected void succeeded() {
                runJs("onModsLoaded(" + getValue() + ")");
            }

            @Override
            protected void failed() {
                Throwable error = getException();
                runJs("showToast(" + jsQuote(error != null ? error.getMessage() : "Ошибка загрузки модов") + ")");
            }
        };
        new Thread(task, "mod-list").start();
    }

    public void installMod(String repo, boolean hasJava, String instanceId) {
        InstanceInfo instance = resolveModsInstance(instanceId);
        ModListing resolvedListing = null;
        try {
            resolvedListing = modBrowserService.findByRepo(repo);
        } catch (Exception ignored) {}

        if (resolvedListing != null && !isCompatible(resolvedListing, instance)) {
            runJs("showToast('Мод несовместим с версией инстанса')");
            return;
        }

        final ModListing installListing = resolvedListing;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> runJs("setDownloadProgress(0, 'Установка мода...')"));
                if (installListing != null) {
                    modInstaller.installFromListing(installListing, instance, progress ->
                            Platform.runLater(() -> runJs("setDownloadProgress(" + progress + ", 'Установка мода...')")));
                } else {
                    modInstaller.installFromGithub(repo, hasJava, instance, progress ->
                            Platform.runLater(() -> runJs("setDownloadProgress(" + progress + ", 'Установка мода...')")));
                }
                return null;
            }

            @Override
            protected void succeeded() {
                runJs("setDownloadProgress(-1, '')");
                runJs("showToast('Мод установлен')");
                refreshData();
                loadMods("", instance.id);
            }

            @Override
            protected void failed() {
                runJs("setDownloadProgress(-1, '')");
                Throwable error = getException();
                runJs("showToast(" + jsQuote(error != null ? error.getMessage() : "Ошибка установки") + ")");
            }
        };
        new Thread(task, "mod-install").start();
    }

    public void importGithubMod(String input, String instanceId) {
        if (input == null || input.isBlank()) return;
        String repo = input.trim();
        if (repo.startsWith("https://github.com/")) repo = repo.substring("https://github.com/".length());
        if (repo.endsWith("/")) repo = repo.substring(0, repo.length() - 1);

        boolean hasJava = false;
        try {
            ModListing listing = modBrowserService.findByRepo(repo);
            if (listing != null) {
                if (!isCompatible(listing, resolveModsInstance(instanceId))) {
                    runJs("showToast('Мод несовместим с версией инстанса')");
                    return;
                }
                hasJava = listing.hasJava;
            }
        } catch (Exception ignored) {}

        installMod(repo, hasJava, instanceId);
    }

    public void exit() {
        Platform.runLater(() -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private boolean isCompatible(ModListing mod, InstanceInfo instance) {
        GameVersion version = findVersion(resolveVersionId(instance, configManager.getSettings()));
        int[] parsed = version != null
                ? GameVersionUtil.parse(version.id, version.name)
                : new int[]{0, 0};
        return GameVersionUtil.isAtLeast(parsed[0], parsed[1], mod.minGameVersion);
    }

    private InstanceInfo resolveModsInstance(String instanceId) {
        if (instanceId != null && !instanceId.isBlank()) {
            return instanceManager.get(instanceId);
        }
        return instanceManager.get(configManager.getSettings().selectedInstanceId);
    }

    private Set<String> listInstalledModKeys(InstanceInfo instance) {
        Set<String> keys = new HashSet<>();
        Path modsDir = InstanceManager.dataDir(instance).resolve("mods");
        if (!Files.isDirectory(modsDir)) return keys;
        try (var stream = Files.list(modsDir)) {
            stream.filter(path -> {
                String name = path.getFileName().toString().toLowerCase();
                return name.endsWith(".zip") || name.endsWith(".jar");
            }).forEach(path -> keys.add(path.getFileName().toString().replaceFirst("(?i)\\.(zip|jar)$", "")));
        } catch (Exception ignored) {}
        return keys;
    }

    private static String repoKey(String repo) {
        return repo.replace("/", "");
    }

    private String resolveVersionId(InstanceInfo instance, LauncherSettings settings) {
        if (instance.versionId != null && !instance.versionId.isBlank()) return instance.versionId;
        return settings.selectedVersionId;
    }

    private GameVersion findVersion(String id) {
        if (id == null) return null;
        for (GameVersion version : versionDownloader.listAvailable()) {
            if (version.id.equals(id)) return version;
        }
        return null;
    }

    private void refreshData() {
        runJs("applyBootstrap(" + getBootstrapData() + ")");
    }

    private void openFolder(File folder) {
        DesktopUtil.openFolderAsync(folder, message -> runJs("showToast(" + jsQuote(message) + ")"));
    }

    private void runJs(String script) {
        Platform.runLater(() -> webEngine.executeScript(script));
    }

    private static String jsQuote(String value) {
        if (value == null) return "''";
        return "'" + escapeJs(value) + "'";
    }

    private static String escapeJs(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
