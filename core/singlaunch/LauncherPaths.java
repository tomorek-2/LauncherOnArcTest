package singlaunch;

import java.io.File;
import java.nio.file.Path;

public final class LauncherPaths {
    private static final Path ROOT = Path.of(
            System.getProperty("singularity.launcher.dir",
                    System.getenv().getOrDefault("SINGULARITY_LAUNCHER_DIR",
                            Path.of(System.getProperty("user.home"), ".singularity-launcher").toString())));

    private LauncherPaths() {}

    public static Path root() {
        return ROOT;
    }

    public static Path configFile() {
        return ROOT.resolve("config.json");
    }

    public static Path instancesDir() {
        return ROOT.resolve("instances");
    }

    public static Path versionsCacheDir() {
        return ROOT.resolve("cache").resolve("versions");
    }

    public static Path legacyVersionsDir() {
        return Path.of("versions");
    }

    public static void ensureDirs() {
        ROOT.toFile().mkdirs();
        instancesDir().toFile().mkdirs();
        versionsCacheDir().toFile().mkdirs();
    }

    public static boolean isDevLayout() {
        return legacyVersionsDir().toFile().isDirectory()
                && legacyVersionsDir().resolve("MindustryV158.1.jar").toFile().exists()
                || new File("index.html").exists();
    }
}
