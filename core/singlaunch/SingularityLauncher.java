
package singlaunch;

import arc.ApplicationCore;
import arc.Core;
import arc.Files;
import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.scene.Scene;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Timer;
import arc.util.pooling.Pool;
import arc.util.viewport.ScreenViewport;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;

public class SingularityLauncher extends ApplicationCore {
    private static final String VERSIONS_DIR = "versions";
    Color ColorC = Color.valueOf("1e1e24");
    private HashMap<String, Fi> jarFiles = new HashMap<>();
    private Fi selectedJar;
    private Font titleFont;
    public static Font regularFont;
    private Scene scene;
    private Label selectedVersionLabel;
    Table main = new Table();
    Table mainButtons = new Table();
    String pathVersions;
    String pathVersionsInput;
    Table listTable = new Table();
    private Fi versionsDir;
    String[] urlsVersions;
    int w;
    int h;
    ArrayList<String> arguments = new ArrayList<String>();
    private Pool<Fi> fiPool = new Pool<Fi>() { @Override protected Fi newObject() { return new Fi("");  } };
    TextButton launchBtn;
Process process;
    String argument =
            "-Xmx512M";
boolean isLinux;
    String urlDownoadList = "https://raw.githubusercontent.com/tomorek-2/LauncherOnArcTest/refs/heads/main/urlVersions.json";
String urlDownloadLatest ="https://github.com/anuken/mindustry/releases/latest/download/Mindustry.jar";
    public SingularityLauncher() {
    }

    public void setup() {
        Log.info("Launcher started!");
       pathVersions = Core.files.local("").absolutePath().replace(System.getProperty("user.home"),  "");

        pathVersionsInput = System.getProperty("user.home") + pathVersions + "/" + VERSIONS_DIR;
        this.versionsDir = Core.files.absolute(pathVersionsInput);
        Core.batch = new SpriteBatch();
        Draw.batch(Core.batch);
        this.scene = new Scene(new ScreenViewport());
        Core.scene = this.scene;

        FontsAWT.load();
        this.regularFont = FontsAWT.regular;
        Style.load();

        this.registerDefaultStyles();

        this.createUI();


        Core.input.addProcessor(this.scene);
        String osName = System.getProperty("os.name").toLowerCase();
         isLinux = osName.contains("nix") || osName.contains("nux") || osName.contains("aix");
        this.scanVersions();
    }

    public void update() {
         w =Core.graphics.getWidth();
         h =Core.graphics.getHeight();
       if (w != 0 && h != 0) {
            Core.graphics.clear(ColorC);
            if (this.scene != null) {
                this.scene.getViewport().update(w, h, true);
                this.scene.act();
                this.scene.draw();
              launchBtn.setDisabled(this.jarFiles.isEmpty());
                if(Core.input.keyTap(KeyCode.enter)) {
                    if (this.selectedJar != null) {
                        this.launchMindustry(this.selectedJar.absolutePath());
                    }
                }
           }

//scanVersions();


           if (isLinux) {
               if(process != null) {
               Fi file = arc.files.Fi.get("/dev/shm/.mindustry_restart_flag");
               if (file.exists()) {
                   byte[] bytes = file.readBytes();
                   if (bytes.length > 0 && bytes[0] == 1) {
                       byte[] bytesw = new byte[]{0};

                       process.destroy();
                       if(process.isAlive()) process.destroyForcibly();
                       this.launchMindustry(this.selectedJar.absolutePath());
                       file.writeBytes(bytesw);
                   }
               }

               }
           }
       }

    }
public void getListUrl(String url) {
        try {
            arc.util.Http.get(url, response -> {

                byte[] data = response.getResult();

                String jsonTextRaw = new String(data, java.nio.charset.StandardCharsets.UTF_8);
                String jsonText = jsonTextRaw.substring(0).trim();

                arc.util.serialization.JsonValue json = new arc.util.serialization.JsonReader().parse(jsonText);


        urlsVersions = json.asStringArray();
Core.app.post(()->this.scanVersions());

            }, error -> Log.err("Ошибка сети", error));
        } catch (Exception e) {
            Log.err("Ошибка сети: "+e.toString());
        }
}
    private void registerDefaultStyles() {
        Drawable panel = this.solidDrawable(Color.valueOf("2b2b36"));
        Drawable hover = this.solidDrawable(Color.valueOf("3b3b46"));
        Button.ButtonStyle defBtn = new Button.ButtonStyle();
        defBtn.up = panel;
        defBtn.over = hover;
        this.scene.addStyle(Button.ButtonStyle.class, defBtn);
        Label.LabelStyle defLabel = new Label.LabelStyle();
        defLabel.font = this.regularFont;
        defLabel.fontColor = Color.white;
        this.scene.addStyle(Label.LabelStyle.class, defLabel);
    }

    private void scanVersions() {
        this.jarFiles.clear();
if(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 50 *1024*1024)System.gc();
        if (!versionsDir.exists()) {

     Log.info("versions directory wasn't created");
        }

        Log.info("Scanning: " + versionsDir.absolutePath());

        for(Fi file: versionsDir.list()) {
            if (file.extEquals("jar")) {
                this.jarFiles.put(file.name(), file);
                Log.info("Found: " + file.name());

            }
        }


        if (this.jarFiles.isEmpty()) {
            Log.warn("No JAR files found in 'versions/'", new Object[0]);
        }


        TextButton.TextButtonStyle versionStyle = Style.versionStyle;
        TextButton.TextButtonStyle remoteStyle = Style.remoteStyle;
        Label.LabelStyle regularLabelStyle = Style.regularLabelStyle;
        listTable.clear();
listTable.getCells().clear();
       if(urlsVersions == null) this.getListUrl(urlDownoadList);
        if(urlsVersions != null) {
            for (String line : urlsVersions) {

                if (line.length() > 32) {

                    String name = line.substring(0, 32).trim();

                    if (!this.jarFiles.containsKey(name + ".jar")) {
                        String url = line.substring(32).trim();

                        TextButton btn = new TextButton("[WEB] " + name, remoteStyle);
                        btn.clicked(() ->{ this.httpDownloadInFile(url, name + ".jar");

                        if(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 25 *1024*1024)System.gc();});
                        listTable.add(btn).width(360.0F).height(45.0F).fillX().pad(0.0F, 0.0F, 1.0F, 0.0F).row();
                    }
                }
            }
        }
        if (this.jarFiles.isEmpty()) {

            listTable.add(new Label("No versions found", regularLabelStyle)).pad(20.0F).row();
            listTable.add(new Label("Place .jar files in 'versions/' folder", regularLabelStyle)).row();
        } else {

            for(Fi jar : this.jarFiles.values()) {
                TextButton btn = new TextButton(jar.nameWithoutExtension(), versionStyle);
                btn.clicked(() -> this.selectVersion(jar));
                listTable.add(btn).width(360.0F).height(45.0F).fillX().pad(0.0F, 0.0F, 1.0F, 0.0F).row();
            }
        }

    }



   public Drawable loadTexture(String path) {
        Texture tex = new Texture(Core.files.internal(path));
        return new TextureRegionDrawable(new TextureRegion(tex));
    }
public void httpDownloadInFile(String url, String nameFile) {
        try {
            arc.util.Http.get(url, (response) -> {
                if(response.getStatus().code != 200) {
                    Log.warn("Download latest version was occured, code error: "+response.getStatus().code+"");
                }
                byte[] data = response.getResult();
             //   Log.info("httpDownloadInListVersions: скачивание идёт. ");
Fi file = Core.files.absolute(pathVersionsInput + "/"+nameFile);
                Log.info("httpDownloadInListVersions: скачивание идёт. "+file.absolutePath());
if(file.exists()) {
                if(Arrays.equals(data, file.readBytes())) {
    Log.info("LatestVersions.jar is already downloaded");
    return;
}
}
file.writeBytes(data, false);
Core.app.post(()->this.scanVersions());

            }, (error)->{
                arc.util.Log.err("error " + error.getMessage(), error.toString());
            });
        } catch (Exception e) {
            Log.err("Error in httpDownloadInListVersions", e);
        }
}
    private void createUI() {
main.clear();
        this.scene.clear();

       Color bg = Color.valueOf("1e1e24");
        Drawable bgDrawable = this.solidDrawable(bg);

     var regularLabelStyle= Style.regularLabelStyle;


var textFieldStyle = Style.textFieldStyle;

       TextButton.TextButtonStyle launchStyle = Style.launchStyle;
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = this.solidDrawable(Color.valueOf("15151a"));
        scrollStyle.background = this.loadTexture("cat.jpg");
        this.main.setFillParent(true);
       this.main.setBackground(bgDrawable);
       // this.mainButtons.setFillParent(true);
        this.mainButtons.setBackground(bgDrawable);
        ScrollPane scroll = new ScrollPane(listTable, scrollStyle);
        scroll.setScrollingDisabled(true, false);
        this.main.add(scroll).width(400.0F).height(220.0F).pad(10.0F).right().row();
       this.selectedVersionLabel = new Label("Selected: None", regularLabelStyle);
        this.selectedVersionLabel.setColor(Color.lightGray);

        launchBtn = new TextButton("LAUNCH", Style.launchStyle);

        TextButton downloadBtn = new TextButton("download", launchStyle);
        TextButton wd = new TextButton(" ", launchStyle);

        TextButton visibleBtn = new TextButton(" ", launchStyle);
        TextButton wd001 = new TextButton(" ", launchStyle);
        TextButton reloadBtn = new TextButton("reload", launchStyle);
        launchBtn.clicked(() -> {
            if (this.selectedJar != null) {
                this.launchMindustry(this.selectedJar.absolutePath());
            }

        });
        arc.scene.ui.TextField directoryChooseF = new TextField(pathVersions, textFieldStyle);
        arc.scene.ui.TextField argumentField = new TextField(argument, textFieldStyle);
        wd001.setSize(25f, 25f);

        launchBtn.setDisabled(this.jarFiles.isEmpty());
directoryChooseF.changed(()->
{


    pathVersionsInput = System.getProperty("user.home") + pathVersions + "/" + VERSIONS_DIR;
    pathVersions  = directoryChooseF.getText();

this.versionsDir = Core.files.absolute(pathVersionsInput);
});
argumentField.changed(()->{
   argument = argumentField.getText();

});
downloadBtn.clicked(()->{
   this.httpDownloadInFile(urlDownloadLatest, "LatestVersions.jar");
});


        this.mainButtons.add(reloadBtn).size(170f, 50f);


        this.mainButtons.add(launchBtn).width(170.0F).height(50.0F).row();

this.main.add(wd001).size(10f);


        this.main.add(argumentField).width(255F).height(50.0F).row();
        this.main.add(visibleBtn).width(25f).height(45f).right();
        this.main.add(directoryChooseF).width(500.0F).height(25.0F).row();
        this.scene.add(this.main);
       this.main.add(this.mainButtons);
        visibleBtn.clicked(()->{
           directoryChooseF.visible = directoryChooseF.visible  ? false : true;
        });
      wd001.clicked(()->{
          Core.app.exit();
      });
        reloadBtn.clicked(() ->{
            this.scanVersions();
            this.main.validate();
            wd001.act(0f);
        });
    }

    private void selectVersion(Fi jar) {
        this.selectedJar = jar;
        if (this.selectedVersionLabel != null) {
            this.selectedVersionLabel.setText("Selected: " + jar.name());
        }

        Log.info("Selected: " + jar.name());
    }

    private Drawable solidDrawable(Color color) {
        Pixmap pix = new Pixmap(1, 1);
        pix.fill(color);
        Texture tex = new Texture(pix);
        pix.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private void launchMindustry(String jarPath) {
        Log.info("Starting: " + jarPath);

        try {

            arguments.clear();
arguments.add("java");

            if (argument != null && !argument.trim().isEmpty()) {
                String[] parts = argument.split(" ");
                for (String part : parts) {
                    if (!part.isEmpty()) arguments.add(part);
                }
            }

arguments.add("-jar");
arguments.add(jarPath);

//new String[]{"java",  "-jar", jarPath
            process =  new ProcessBuilder(arguments).inheritIO().start();

        } catch (IOException e) {
            Log.err("Start failed: ", e);
        }
    }

    public static void main(String[] args) {
        SdlConfig config = new SdlConfig();
        config.title = "Singularity Launcher";
        config.width = 800;
        config.height = 400;
        config.fullscreen = false;
        config.resizable = true;
        config.decorated = false;
        config.vSyncEnabled = true;

 new SdlApplication(new SingularityLauncher(), config);

    }
}
