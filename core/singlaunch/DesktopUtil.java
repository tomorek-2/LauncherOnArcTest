package singlaunch;

import java.awt.Desktop;
import java.io.File;
import java.util.function.Consumer;

public final class DesktopUtil {
    private DesktopUtil() {}

    public static void openFolderAsync(File folder, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            folder.mkdirs();
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(folder);
                    return;
                }
            } catch (Exception ignored) {}

            try {
                new ProcessBuilder("xdg-open", folder.getAbsolutePath())
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start();
            } catch (Exception e) {
                if (onError != null) onError.accept(e.getMessage() != null ? e.getMessage() : "Не удалось открыть папку");
            }
        }, "open-folder");
        thread.setDaemon(true);
        thread.start();
    }
}
