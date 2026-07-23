
package singlaunch;

import arc.ApplicationCore;
import arc.Core;
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
import arc.util.viewport.ScreenViewport;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class SingularityLauncher extends ApplicationCore {
    private static final String VERSIONS_DIR = "versions";
    private ArrayList<Fi> jarFiles = new ArrayList();
    private Fi selectedJar;
    private Font titleFont;
    private Font regularFont;
    private Scene scene;
    private Label selectedVersionLabel;
    Table main = new Table();
    String pathVersions;
    String pathVersionsInput;
    Table listTable = new Table();

    public SingularityLauncher() {
    }

    public void setup() {
        Log.info("Launcher started!");
       pathVersions = Core.files.local(VERSIONS_DIR).absolutePath().replace(System.getProperty("user.home"),  "");

        pathVersionsInput = System.getProperty("user.home") + pathVersions;
        Core.batch = new SpriteBatch();
        Draw.batch(Core.batch);
        this.scene = new Scene(new ScreenViewport());
        Core.scene = this.scene;
        this.titleFont = this.generateFont(48);
        this.regularFont = this.generateFont(22);
        this.registerDefaultStyles();

        this.createUI();
        Core.input.addProcessor(this.scene);

        this.scanVersions();
    }

    public void update() {
        int w = Core.graphics.getWidth();
        int h = Core.graphics.getHeight();
        if (w != 0 && h != 0) {
            Core.graphics.clear(Color.valueOf("1e1e24"));
            if (this.scene != null) {
                this.scene.getViewport().update(w, h, true);
                this.scene.act();
                this.scene.draw();
            }

        //    Core.input.addProcessor(this.scene);
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
        Fi dir = Core.files.absolute(pathVersionsInput);
        if (!dir.exists()) {

            Log.info("versions directory wasn't created");
        }

        Log.info("Scanning: " + dir.absolutePath());

        for(Fi file : dir.list()) {
            if (file.extEquals("jar")) {
                this.jarFiles.add(file);
                Log.info("Found: " + file.name());
            }
        }

        if (this.jarFiles.isEmpty()) {
            Log.warn("No JAR files found in 'versions/'", new Object[0]);
        }
//this.createUI();
        TextButton.TextButtonStyle versionStyle = new TextButton.TextButtonStyle();
        versionStyle.up = this.solidDrawable(Color.valueOf("2b2b36"));
        versionStyle.over = this.solidDrawable(Color.valueOf("3b3b46"));
        versionStyle.down = this.solidDrawable(Color.valueOf("f05d23"));
        versionStyle.font = this.regularFont;
        versionStyle.fontColor = Color.valueOf("ffffff");
        Label.LabelStyle regularLabelStyle = new Label.LabelStyle();
        regularLabelStyle.font = this.regularFont;
        regularLabelStyle.fontColor = Color.valueOf("ffffff");

        if (this.jarFiles.isEmpty()) {
            listTable.clear();
            listTable.add(new Label("No versions found", regularLabelStyle)).pad(20.0F).row();
            listTable.add(new Label("Place .jar files in 'versions/' folder", regularLabelStyle)).row();
        } else {
            listTable.clear();
            for(Fi jar : this.jarFiles) {
                TextButton btn = new TextButton(jar.nameWithoutExtension(), versionStyle);
                btn.clicked(() -> this.selectVersion(jar));
                listTable.add(btn).width(360.0F).height(45.0F).fillX().pad(0.0F, 0.0F, 1.0F, 0.0F).row();
            }
        }
        if (!this.jarFiles.isEmpty()) {
            this.selectVersion((Fi)this.jarFiles.get(0));
        }

    }

    private Font generateFont(int fontSize) {
        try {
            java.awt.Font awtFont = new java.awt.Font("SansSerif", 1, fontSize);
            String chars = " !\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
            int count = chars.length();
            BufferedImage tmp = new BufferedImage(1, 1, 2);
            Graphics2D tg = tmp.createGraphics();
            tg.setFont(awtFont);
            FontMetrics fm = tg.getFontMetrics();
            int maxCharWidth = 0;

            for(int i = 0; i < count; ++i) {
                maxCharWidth = Math.max(maxCharWidth, fm.charWidth(chars.charAt(i)));
            }

            int cellW = maxCharWidth + 4;
            int cellH = fm.getHeight() + 4;
            int maxCols = 16;
            int rows = (int)Math.ceil((double)count / (double)maxCols);
            int imgW = cellW * maxCols;
            int imgH = cellH * rows;
            BufferedImage atlas = new BufferedImage(imgW, imgH, 2);
            Graphics2D g = atlas.createGraphics();
            g.setFont(awtFont);
            g.setColor(java.awt.Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            for(int i = 0; i < count; ++i) {
                int col = i % maxCols;
                int row = i / maxCols;
                int x = col * cellW;
                int y = row * cellH;
                int cw = fm.charWidth(chars.charAt(i));
                g.drawString(String.valueOf(chars.charAt(i)), (float)x + (float)(cellW - cw) / 8.0F, (float)(y + fm.getAscent()- 6));
            }

            g.dispose();
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "singularity-font-" + System.nanoTime());
            tmpDir.mkdirs();
            File pngFile = new File(tmpDir, "font_" + fontSize + ".png");
            ImageIO.write(atlas, "png", pngFile);
            int base = fm.getAscent() + 2;
            StringBuilder fnt = new StringBuilder();
            fnt.append("info face=\"SansSerif\" size=").append(fontSize).append(" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1\n");
            fnt.append("common lineHeight=").append(cellH).append(" base=").append(base).append(" scaleW=").append(imgW).append(" scaleH=").append(imgH).append(" pages=1 packed=0\n");
            fnt.append("page id=0 file=\"font_").append(fontSize).append(".png\"\n");
            fnt.append("chars count=").append(count).append("\n");

            for(int i = 0; i < count; ++i) {
                char c = chars.charAt(i);
                int col = i % maxCols;
                int row = i / maxCols;
                int x = col * cellW;
                int y = row * cellH;
                int cw = fm.charWidth(c);
                fnt.append("char id=").append((int)c).append(" x=").append(x).append(" y=").append(y).append(" width=").append(cw).append(" height=").append(cellH).append(" xoffset=0").append(" yoffset=1").append(" xadvance=").append(cw).append(" page=0 chnl=15\n");
            }

            File fntFile = new File(tmpDir, "font_" + fontSize + ".fnt");

            try (FileWriter fw = new FileWriter(fntFile, StandardCharsets.UTF_8)) {
                fw.write(fnt.toString());
            }

            Fi fontFi = new Fi(fntFile.getAbsolutePath());
            Fi pngFi = new Fi(pngFile.getAbsolutePath());
            Pixmap pix = new Pixmap(pngFi);
            Texture tex = new Texture(pix);
            tex.setFilter(TextureFilter.linear, TextureFilter.linear);
            TextureRegion region = new TextureRegion(tex);
            return new Font(fontFi, region, false);
        } catch (Exception e) {
            Log.err("Font generation failed for size " + fontSize, e);
            return null;
        }
    }

    private Drawable loadTexture(String path) {
        Texture tex = new Texture(Core.files.internal(path));
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private void createUI() {
main.clear();
        this.scene.clear();
        Color bg = Color.valueOf("1e1e24");
        Color panel = Color.valueOf("2b2b36");
        Color hover = Color.valueOf("3b3b46");
        Color accent = Color.valueOf("f05d23");
        Color textColor = Color.valueOf("ffffff");
        Color green = Color.valueOf("4caf50");
        Color greenHover = Color.valueOf("66bb6a");
        Drawable bgDrawable = this.solidDrawable(bg);
        Drawable panelDrawable = this.solidDrawable(panel);
        Drawable hoverDrawable = this.solidDrawable(hover);
        Drawable accentDrawable = this.solidDrawable(accent);
        Drawable greenDrawable = this.loadTexture("img.png");
        Drawable greenHoverDrawable = this.solidDrawable(greenHover);
        Label.LabelStyle regularLabelStyle = new Label.LabelStyle();
        regularLabelStyle.font = this.regularFont;
        regularLabelStyle.fontColor = textColor;
        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        titleLabelStyle.font = this.titleFont;
        textFieldStyle.font = this.regularFont;
        textFieldStyle.fontColor = textColor;
        textFieldStyle.cursor = this.loadTexture("cat.png");
        titleLabelStyle.fontColor = accent;
        TextButton.TextButtonStyle versionStyle = new TextButton.TextButtonStyle();
        versionStyle.up = panelDrawable;
        versionStyle.over = hoverDrawable;
        versionStyle.down = accentDrawable;
        versionStyle.font = this.regularFont;
        versionStyle.fontColor = textColor;
        TextButton.TextButtonStyle launchStyle = new TextButton.TextButtonStyle();
        launchStyle.up = greenDrawable;
        launchStyle.over = greenHoverDrawable;
        launchStyle.down = accentDrawable;
        launchStyle.font = this.titleFont;
        launchStyle.fontColor = textColor;
        launchStyle.disabled = this.solidDrawable(Color.valueOf("3a3a3a"));
        launchStyle.disabledFontColor = Color.gray;
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = this.solidDrawable(Color.valueOf("15151a"));
        scrollStyle.background = this.loadTexture("cat.jpg");
        this.main.setFillParent(true);
       this.main.setBackground(bgDrawable);

        ScrollPane scroll = new ScrollPane(listTable, scrollStyle);
        scroll.setScrollingDisabled(true, false);
        this.main.add(scroll).width(400.0F).height(220.0F).pad(10.0F).center();
       this.selectedVersionLabel = new Label("Selected: None", regularLabelStyle);
        this.selectedVersionLabel.setColor(Color.lightGray);
        if (!this.jarFiles.isEmpty()) {
            this.selectVersion((Fi)this.jarFiles.get(0));
        }

        TextButton launchBtn = new TextButton("LAUNCH", launchStyle);
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
        wd001.setSize(25f, 25f);
        wd001.update(() -> {
            wd001.y = wd.y + wd.getHeight() / 2 - wd001.getHeight() / 2;
wd001.color.a = wd001.x / (wd.x + wd.getWidth());
           if (wd001.x >= (wd.x + wd.getWidth() - wd001.getWidth())) {
               wd001.x = wd.x;
          } else   wd001.x += 1f;
        });

        launchBtn.setDisabled(this.jarFiles.isEmpty());
directoryChooseF.update(()->
{
    launchBtn.setDisabled(this.jarFiles.isEmpty());
    pathVersionsInput = System.getProperty("user.home") + pathVersions;
    pathVersions  = directoryChooseF.getText();
});

       this.main.add(wd).width(450.0F).height(220.0F).left();
       this.main.add(wd001).width(25.0F).height(25.0F).right().row();

       this.main.add(reloadBtn).size(170f, 50f).right();

       this.main.add(launchBtn).width(250.0F).height(60.0F).row();
        this.main.add(visibleBtn).width(25f).height(45f).right();
        this.main.add(directoryChooseF).width(500.0F).height(25.0F).row();

        this.scene.add(this.main);
        visibleBtn.clicked(()->{
           directoryChooseF.visible = directoryChooseF.visible  ? false : true;
        });
        wd001.clicked(() -> System.exit(0));
        reloadBtn.clicked(() -> this.scanVersions());
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
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private void launchMindustry(String jarPath) {
        Log.info("Starting: " + jarPath);

        try {
            (new ProcessBuilder(new String[]{"java", "-jar", jarPath})).inheritIO().start();
        } catch (IOException e) {
            Log.err("Start failed: ", e);
        }

    }

    public static void main(String[] args) {
        SdlConfig config = new SdlConfig();
        config.title = "Singularity Launcher";
        config.width = 900;
        config.height = 350;
        config.fullscreen = false;
        config.resizable = true;
        config.decorated = true;
        new SdlApplication(new SingularityLauncher(), config);
    }
}
