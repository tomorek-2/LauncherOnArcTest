package singlaunch;

import java.nio.file.Files;
import java.nio.file.Path;

final class PathUtil {
    private PathUtil() {}

    static String defaultJava() {
        String home = System.getProperty("java.home");
        Path unix = Path.of(home, "bin", "java");
        if (Files.isExecutable(unix)) return unix.toAbsolutePath().toString();
        Path win = Path.of(home, "bin", "java.exe");
        if (Files.isExecutable(win)) return win.toAbsolutePath().toString();
        return "java";
    }
}
