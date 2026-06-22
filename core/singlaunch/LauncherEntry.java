package singlaunch;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LauncherEntry {
    private LauncherEntry() {}

    public static void main(String[] args) throws Exception {
        if (!hasJavaFx()) {
            relaunchWithJavaFx(args);
            return;
        }
        SingularityLauncher.main(args);
    }

    private static boolean hasJavaFx() {
        try {
            Class.forName("javafx.application.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void relaunchWithJavaFx(String[] args) throws Exception {
        Path jarDir = Path.of(LauncherEntry.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        Path javafxDir = jarDir.resolve("javafx");
        if (!javafxDir.toFile().isDirectory()) {
            System.err.println("JavaFX не найден. Запустите через scripts/run.sh или build/dist/run.sh");
            System.err.println("Ожидается папка: " + javafxDir.toAbsolutePath());
            System.exit(1);
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(PathUtil.defaultJava());
        cmd.add("--module-path");
        cmd.add(javafxDir.toAbsolutePath().toString());
        cmd.add("--add-modules");
        cmd.add("javafx.controls,javafx.web,javafx.graphics");
        String launcherDir = System.getProperty("singularity.launcher.dir", System.getenv("SINGULARITY_LAUNCHER_DIR"));
        if (launcherDir != null && !launcherDir.isBlank()) {
            cmd.add("-Dsingularity.launcher.dir=" + launcherDir);
        }
        cmd.add("-jar");
        cmd.add(jarDir.resolve("SingularityLauncher.jar").toAbsolutePath().toString());
        for (String arg : args) cmd.add(arg);

        new ProcessBuilder(cmd).inheritIO().start().waitFor();
    }
}
