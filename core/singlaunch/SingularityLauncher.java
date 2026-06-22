package singlaunch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

public class SingularityLauncher extends Application {

    private WebEngine webEngine;
    private JavaBridge bridge;

    @Override
    public void start(Stage stage) {
        LauncherPaths.ensureDirs();

        ConfigManager configManager = new ConfigManager();
        InstanceManager instanceManager = new InstanceManager();
        VersionDownloader versionDownloader = new VersionDownloader();
        GameLauncher gameLauncher = new GameLauncher();

        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        bridge = new JavaBridge(configManager, instanceManager, versionDownloader, gameLauncher, webEngine, this::setStatus);

        try {
            String html = ResourceLoader.loadWebPage();
            webEngine.loadContent(html, "text/html");
        } catch (Exception e) {
            webEngine.loadContent("<html><body style='background:#1a1a1a;color:#ffd379;font-family:monospace;padding:24px'>"
                    + "Не удалось загрузить интерфейс: " + e.getMessage() + "</body></html>");
        }

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", bridge);
                Platform.runLater(() -> webEngine.executeScript("applyBootstrap(" + bridge.getBootstrapData() + ")"));
            }
        });

        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 920, 580);
        scene.setFill(javafx.scene.paint.Color.web("#1a1a1a"));

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Singularity Launcher");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnHidden(e -> Platform.exit());
        HyprlandSupport.floatOnOpen(stage);
        stage.show();
    }

    private void setStatus(String text) {
        if (text == null) return;
        String escaped = text.replace("\\", "\\\\").replace("'", "\\'");
        Platform.runLater(() -> webEngine.executeScript("setStatus('" + escaped + "')"));
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        launch(args);
    }
}
