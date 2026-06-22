package singlaunch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GameLauncher {
  private static final String MAIN_CLASS = "mindustry.desktop.DesktopLauncher";

  public Process launch(InstanceInfo instance, GameVersion version, LauncherSettings settings, Path gameJar) throws IOException {
    Path dataDir = InstanceManager.dataDir(instance);
    Files.createDirectories(dataDir);
    Files.createDirectories(dataDir.resolve("mods"));
    Files.createDirectories(dataDir.resolve("saves"));

    List<String> cmd = new ArrayList<>();
    cmd.add(settings.resolveJavaPath());
    cmd.add("-Xms" + settings.minMemoryMb + "m");
    cmd.add("-Xmx" + settings.maxMemoryMb + "m");
    cmd.add("-Dmindustry.data.dir=" + dataDir.toAbsolutePath());
    cmd.add("-Dfile.encoding=UTF-8");

    if (settings.extraJvmArgs != null && !settings.extraJvmArgs.isBlank()) {
      for (String arg : settings.extraJvmArgs.trim().split("\\s+")) {
        if (!arg.isBlank()) cmd.add(arg);
      }
    }

    cmd.add("-cp");
    cmd.add(gameJar.toAbsolutePath().toString());
    cmd.add(MAIN_CLASS);

    ProcessBuilder builder = new ProcessBuilder(cmd);
    builder.directory(gameJar.getParent().toFile());
    builder.inheritIO();
    return builder.start();
  }
}
