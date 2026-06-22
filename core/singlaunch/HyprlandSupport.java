package singlaunch;

import javafx.stage.Stage;

public final class HyprlandSupport {
    private HyprlandSupport() {}

    public static void floatOnOpen(Stage stage) {
        if (System.getenv("HYPRLAND_INSTANCE_SIGNATURE") == null) return;

        stage.setOnShown(event -> {
            Thread t = new Thread(() -> {
            try {
                Thread.sleep(250);
                String title = stage.getTitle();
                new ProcessBuilder("hyprctl", "dispatch", "togglefloating", "title:" + title)
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start()
                        .waitFor();
            } catch (Exception ignored) {}
            }, "hyprland-float");
            t.setDaemon(true);
            t.start();
        });
    }
}
