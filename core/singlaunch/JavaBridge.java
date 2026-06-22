package singlaunch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.web.WebEngine;

import java.awt.Desktop;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JavaBridge {
    private static final Gson GSON = new GsonBuilder().create();

    private final ConfigManager configManager;
    private final InstanceManager instanceManager;
    private final VersionDownloader versionDownloader;
    private final GameLauncher gameLauncher;
    private final WebEngine webEngine;
    private final Consumer<String> status;

    public JavaBridge(ConfigManager configManager, InstanceManager instanceManager,
                      VersionDownloader versionDownloader, GameLauncher gameLauncher,
                      WebEngine webEngine, Consumer<String> status) {
        this.configManager = configManager;
        this.instanceManager = instanceManager;
        this.versionDownloader = versionDownloader;
        this.gameLauncher = gameLauncher;
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

        Map<String, Object> payload = new HashMap<>();
        payload.put("settings", settings);
        payload.put("instances", instances);
        payload.put("versions", versions);
        payload.put("launcherDir", LauncherPaths.root().toAbsolutePath().toString());
        payload.put("systemRamMb", (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024)));
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
        LauncherSettings settings = configManager.getSettings();
        InstanceInfo instance = instanceManager.get(settings.selectedInstanceId);
        openFolder(InstanceManager.dataDir(instance).toFile());
    }

    public void openModsFolder() {
        LauncherSettings settings = configManager.getSettings();
        InstanceInfo instance = instanceManager.get(settings.selectedInstanceId);
        File mods = InstanceManager.dataDir(instance).resolve("mods").toFile();
        mods.mkdirs();
        openFolder(mods);
    }

    public void settings() {
        runJs("openPanel('settings')");
    }

    public void instances() {
        runJs("openPanel('instances')");
    }

    public void mods() {
        openModsFolder();
    }

    public void exit() {
        Platform.runLater(() -> {
            Platform.exit();
            System.exit(0);
        });
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
        folder.mkdirs();
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(folder);
            }
        } catch (Exception e) {
            runJs("showToast(" + jsQuote("Не удалось открыть папку") + ")");
        }
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
