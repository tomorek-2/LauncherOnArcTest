package singlaunch;

import arc.ApplicationCore;
import arc.Core;
import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Scene;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.viewport.ScreenViewport;

import javax.imageio.ImageIO;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SingularityLauncher extends ApplicationCore {


    private static final String VERSIONS_DIR = "versions";
    private ArrayList<Fi> jarFiles = new ArrayList<>();
    private Fi selectedJar;
    private Font font;
    private Scene scene;

    @Override
    public void setup() {
        Log.info("Launcher started!");
        Core.batch = new SpriteBatch();
        Draw.batch(Core.batch);
        scene = new Scene(new ScreenViewport());
        Core.scene = scene;
        registerDefaultStyles();
        font = generateFont();
        scanVersions();
        createUI();
    }

    @Override
    public void update() {
        Core.graphics.clear(Color.valueOf("2a2a2a"));
        if (scene != null) {
            scene.act();
            scene.draw();
        }
    }

    private void registerDefaultStyles() {
        Drawable panel = solidDrawable(Color.valueOf("3a3a3a"));
        Drawable hover = solidDrawable(Color.valueOf("4a4a4a"));

        Button.ButtonStyle defBtn = new Button.ButtonStyle();
        defBtn.up = panel;
        defBtn.over = hover;
        scene.addStyle(Button.ButtonStyle.class, defBtn);

        Label.LabelStyle defLabel = new Label.LabelStyle();
        defLabel.fontColor = Color.white;
        scene.addStyle(Label.LabelStyle.class, defLabel);
    }

    private void scanVersions() {
        Fi dir = Core.files.local(VERSIONS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            Log.info("Created versions directory: " + dir.absolutePath());
        }

        Log.info("Scanning: " + dir.absolutePath());

        for (Fi file : dir.list()) {
            if (file.extEquals("jar")) {
                jarFiles.add(file);
                Log.info("Found: " + file.name());
            }
        }

        if (jarFiles.isEmpty()) {
            Log.warn("No JAR files found in 'versions/'");
        }
    }

    private Font generateFont() {
        try {
            java.awt.Font awtFont = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18);
            String chars = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
            int count = chars.length();

            BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = tmp.createGraphics();
            tg.setFont(awtFont);
            FontMetrics fm = tg.getFontMetrics();

            int cellW = 24;
            int cellH = fm.getHeight() + 4;
            int imgW = cellW * count;
            int imgH = cellH;

            BufferedImage atlas = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = atlas.createGraphics();
            g.setFont(awtFont);
            g.setColor(java.awt.Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            for (int i = 0; i < count; i++) {
                String ch = String.valueOf(chars.charAt(i));
                int cw = fm.charWidth(chars.charAt(i));
                g.drawString(ch, cellW * i + (cellW - cw) / 2f, fm.getAscent() + 2);
            }
            g.dispose();

            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "singularity-font-" + System.nanoTime());
            tmpDir.mkdirs();
            File pngFile = new File(tmpDir, "font.png");
            ImageIO.write(atlas, "png", pngFile);

            int base = fm.getAscent() + 2;
            StringBuilder fnt = new StringBuilder();
            fnt.append("info face=\"SansSerif\" size=18 bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1\n");
            fnt.append("common lineHeight=").append(cellH).append(" base=").append(base).append(" scaleW=").append(imgW).append(" scaleH=").append(imgH).append(" pages=1 packed=0\n");
            fnt.append("page id=0 file=\"font.png\"\n");
            fnt.append("chars count=").append(count).append("\n");

            for (int i = 0; i < count; i++) {
                char c = chars.charAt(i);
                int cw = fm.charWidth(c);
                fnt.append("char id=").append((int) c)
                    .append(" x=").append(cellW * i)
                    .append(" y=0")
                    .append(" width=").append(cw)
                    .append(" height=").append(cellH)
                    .append(" xoffset=0")
                    .append(" yoffset=-2")
                    .append(" xadvance=").append(Math.max(cw + 1, 5))
                    .append(" page=0 chnl=15\n");
            }

            File fntFile = new File(tmpDir, "font.fnt");
            try (FileWriter fw = new FileWriter(fntFile)) {
                fw.write(fnt.toString());
            }

            Fi fontFi = new Fi(fntFile.getAbsolutePath());
            Fi pngFi = new Fi(pngFile.getAbsolutePath());

            Pixmap pix = new Pixmap(pngFi);
            Texture tex = new Texture(pix);
            TextureRegion region = new TextureRegion(tex);
            return new Font(fontFi, region, false);
        } catch (Exception e) {
            Log.err("Font generation failed", e);
            return null;
        }
    }

    private void createUI() {
        Color bg = Color.valueOf("2a2a2a");
        Color panel = Color.valueOf("3a3a3a");
        Color accent = Color.valueOf("f09a1e");
        Color hover = Color.valueOf("4a4a4a");
        Color textColor = Color.valueOf("ffffff");
        Color green = Color.valueOf("3a6a1e");
        Color greenHover = Color.valueOf("4a8a2e");

        Drawable bgDrawable = solidDrawable(bg);
        Drawable panelDrawable = solidDrawable(panel);
        Drawable accentDrawable = solidDrawable(accent);
        Drawable hoverDrawable = solidDrawable(hover);
        Drawable greenDrawable = solidDrawable(green);
        Drawable greenHoverDrawable = solidDrawable(greenHover);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = textColor;

        TextButton.TextButtonStyle versionStyle = new TextButton.TextButtonStyle();
        versionStyle.up = panelDrawable;
        versionStyle.over = hoverDrawable;
        versionStyle.down = accentDrawable;
        versionStyle.font = font;
        versionStyle.fontColor = accent;

        TextButton.TextButtonStyle launchStyle = new TextButton.TextButtonStyle();
        launchStyle.up = greenDrawable;
        launchStyle.over = greenHoverDrawable;
        launchStyle.down = accentDrawable;
        launchStyle.font = font;
        launchStyle.fontColor = textColor;

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = panelDrawable;

        Table main = new Table();
        main.setFillParent(true);
        main.setBackground(bgDrawable);

        Label title = new Label("SINGULARITY LAUNCHER", labelStyle);
        title.setFontScale(2f);
        main.add(title).padTop(24).padBottom(16).row();

        Table listTable = new Table();
        listTable.defaults().pad(3).fillX();

        if (jarFiles.isEmpty()) {
            listTable.add(new Label("No versions found", labelStyle)).pad(20).row();
            listTable.add(new Label("Place .jar files in 'versions/'", labelStyle)).row();
        } else {
            for (Fi jar : jarFiles) {
                TextButton btn = new TextButton(jar.nameWithoutExtension(), versionStyle);
                btn.clicked(() -> selectVersion(jar));
                listTable.add(btn).width(320).row();
            }
        }

        ScrollPane scroll = new ScrollPane(listTable, scrollStyle);
        main.add(scroll).width(360).height(200).pad(8).row();

        selectedJar = jarFiles.isEmpty() ? null : jarFiles.get(0);

        TextButton launchBtn = new TextButton("Launch", launchStyle);
        launchBtn.clicked(() -> {
            if (selectedJar != null) {
                launchMindustry(selectedJar.absolutePath());
            }
        });
        launchBtn.setDisabled(jarFiles.isEmpty());
        main.add(launchBtn).width(200).padTop(16).padBottom(24);

        scene.add(main);
    }

    private void selectVersion(Fi jar) {
        selectedJar = jar;
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
            new ProcessBuilder("java", "-jar", jarPath)
                    .inheritIO()
                    .start();
            Core.app.exit();
        } catch (IOException e) {
            Log.err("Start failed: ", e);
        }
    }

    public static void main(String[] args) {
        SdlConfig config = new SdlConfig();
        config.title = "Singularity Launcher";
        config.width = 500;
        config.height = 420;

        new SdlApplication(new SingularityLauncher(), config);
    }
}
