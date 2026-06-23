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
import arc.Core.*;
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

    // We now use two separate fonts to avoid pixelated scaling
    private Font titleFont;
    private Font regularFont;

    private Scene scene;
    private Label selectedVersionLabel;
    Table main = new Table();


    @Override
    public void setup() {
        Log.info("Launcher started!");
        Core.batch = new SpriteBatch();
        Draw.batch(Core.batch);
        scene = new Scene(new ScreenViewport());
        Core.scene = scene;

        registerDefaultStyles();

        // Generate a crisp, large font for titles, and a standard one for UI elements
        titleFont = generateFont(48);
        regularFont = generateFont(22);

        scanVersions();
        createUI();
    }

    @Override
    public void update() {


        int w = Core.graphics.getWidth();
        int h = Core.graphics.getHeight();
        if (w == 0 || h == 0) return;

        // Clean dark background
        Core.graphics.clear(Color.valueOf("1e1e24"));

        if (scene != null) {
            scene.getViewport().update(w, h, true);
            scene.act();
            scene.draw();
        }
        Core.input.addProcessor(scene);
        if (main.visible) {
            main.visible = false;
        } else {
            main.visible = true;

        }
    }


    private void registerDefaultStyles() {
        Drawable panel = solidDrawable(Color.valueOf("2b2b36"));
        Drawable hover = solidDrawable(Color.valueOf("3b3b46"));

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

    /**
     * Generates a smooth, anti-aliased bitmap font dynamically using AWT.
     * Rewritten to support multi-row atlas textures and dynamic cell sizing
     * to prevent letter clipping and pixelation.
     *
     * @param fontSize the target point size of the font.
     * @return an Arc Font instance.
     */
    private Font generateFont(int fontSize) {
        try {
          //  java.awt.Font awtFont = new java.awt.Font("SansSerif", java.awt.Font.BOLD, fontSize);
            java.awt.Font awtFont = new java.awt.Font("Monospaced", java.awt.Font.BOLD, fontSize);
            String chars = " !\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
            int count = chars.length();

            // Create a dummy image to extract font metrics
            BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tg = tmp.createGraphics();
            tg.setFont(awtFont);
            FontMetrics fm = tg.getFontMetrics();

            // Calculate exact maximum character width to prevent clipping
            int maxCharWidth = 0;
            for (int i = 0; i < count; i++) {
                maxCharWidth = Math.max(maxCharWidth, fm.charWidth(chars.charAt(i)));
            }

            int cellW = maxCharWidth + 4;
            int cellH = fm.getHeight() + 4;

            // Layout in a grid to avoid exceeding maximum texture width limits on old GPUs
            int maxCols = 16;
            int rows = (int) Math.ceil((double) count / maxCols);
            int imgW = cellW * maxCols;
            int imgH = cellH * rows;

            BufferedImage atlas = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = atlas.createGraphics();
            g.setFont(awtFont);
            g.setColor(java.awt.Color.WHITE);
            // Enable anti-aliasing for smooth, non-pixelated text rendering
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            for (int i = 0; i < count; i++) {
                int col = i % maxCols;
                int row = i / maxCols;
                int x = col * cellW;
                int y = row * cellH;

                int cw = fm.charWidth(chars.charAt(i));
                // Center the character horizontally within its cell
                g.drawString(String.valueOf(chars.charAt(i)), x + (cellW - cw) / 8f, y + fm.getAscent() + 2);
            }
            g.dispose();

            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "singularity-font-" + System.nanoTime());
            tmpDir.mkdirs();
            File pngFile = new File(tmpDir, "font_" + fontSize + ".png");
            ImageIO.write(atlas, "png", pngFile);

            int base = fm.getAscent() + 2;
            StringBuilder fnt = new StringBuilder();

            // Build the .fnt file layout string
            fnt.append("info face=\"SansSerif\" size=").append(fontSize).append(" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1\n");
            fnt.append("common lineHeight=").append(cellH).append(" base=").append(base).append(" scaleW=").append(imgW).append(" scaleH=").append(imgH).append(" pages=1 packed=0\n");
            fnt.append("page id=0 file=\"font_").append(fontSize).append(".png\"\n");
            fnt.append("chars count=").append(count).append("\n");

            for (int i = 0; i < count; i++) {
                char c = chars.charAt(i);
                int col = i % maxCols;
                int row = i / maxCols;
                int x = col * cellW;
                int y = row * cellH;
                int cw = fm.charWidth(c);

                fnt.append("char id=").append((int) c)
                        .append(" x=").append(x)
                        .append(" y=").append(y)
                        .append(" width=").append(cw)
                        .append(" height=").append(cellH)
                        .append(" xoffset=0")
                        .append(" yoffset=1")
                        .append(" xadvance=").append(cw)
                        .append(" page=0 chnl=15\n");
            }

            File fntFile = new File(tmpDir, "font_" + fontSize + ".fnt");
            try (FileWriter fw = new FileWriter(fntFile, java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write(fnt.toString());
            }

            Fi fontFi = new Fi(fntFile.getAbsolutePath());
            Fi pngFi = new Fi(pngFile.getAbsolutePath());

            Pixmap pix = new Pixmap(pngFi);
            Texture tex = new Texture(pix);
            // Use linear filtering to make the edges look smooth
            tex.setFilter(Texture.TextureFilter.linear, Texture.TextureFilter.linear);
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
        Color bg = Color.valueOf("1e1e24");
        Color panel = Color.valueOf("2b2b36");
        Color hover = Color.valueOf("3b3b46");
        Color accent = Color.valueOf("f05d23");
        Color textColor = Color.valueOf("ffffff");
        Color green = Color.valueOf("4caf50");
        Color greenHover = Color.valueOf("66bb6a");

        Drawable bgDrawable = solidDrawable(bg);
        Drawable panelDrawable = solidDrawable(panel);
        Drawable hoverDrawable = solidDrawable(hover);
        Drawable accentDrawable = solidDrawable(accent);
        Drawable greenDrawable = loadTexture("Снимок экрана от 2026-06-23 07-40-29.png");
        Drawable greenHoverDrawable = solidDrawable(greenHover);

        // Standard text style
        Label.LabelStyle regularLabelStyle = new Label.LabelStyle();
        regularLabelStyle.font = regularFont;
        regularLabelStyle.fontColor = textColor;

        // Title text style (uses the high-resolution titleFont)
        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        titleLabelStyle.font = titleFont;
        titleLabelStyle.fontColor = accent;

        // Version button style
        TextButton.TextButtonStyle versionStyle = new TextButton.TextButtonStyle();
        versionStyle.up = panelDrawable;
        versionStyle.over = hoverDrawable;
        versionStyle.down = accentDrawable;
        versionStyle.font = regularFont;
        versionStyle.fontColor = textColor;

        // Play button style
        TextButton.TextButtonStyle launchStyle = new TextButton.TextButtonStyle();
        launchStyle.up = greenDrawable;
        launchStyle.over = greenHoverDrawable;
        launchStyle.down = accentDrawable;
        launchStyle.font = titleFont;
        launchStyle.fontColor = textColor;
        launchStyle.disabled = solidDrawable(Color.valueOf("3a3a3a"));
        launchStyle.disabledFontColor = Color.gray;

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = solidDrawable(Color.valueOf("15151a"));

        // Main layout container
  //      Table main = new Table();
        main.setFillParent(true);
        main.setBackground(bgDrawable);

        // Header
        main.add(new Label("SINGULARITY", titleLabelStyle)).padTop(30).padBottom(5).row();

        main.add(new Label("Launcher", regularLabelStyle)).padBottom(20).row();

        // Versions list
        Table listTable = new Table();
        listTable.defaults().pad(4).fillX();

        if (jarFiles.isEmpty()) {
            listTable.add(new Label("No versions found", regularLabelStyle)).pad(20).row();
            listTable.add(new Label("Place .jar files in 'versions/' folder", regularLabelStyle)).row();
        } else {
            for (Fi jar : jarFiles) {
                TextButton btn = new TextButton(jar.nameWithoutExtension(), versionStyle);
                btn.clicked(() -> selectVersion(jar));
                listTable.add(btn).width(360).height(45).row();
            }
        }

        ScrollPane scroll = new ScrollPane(listTable, scrollStyle);
        scroll.setScrollingDisabled(true, false); // Disable horizontal scrolling
        main.add(scroll).width(400).height(220).pad(10).row();

        // Selected version info
        selectedVersionLabel = new Label("Selected: None", regularLabelStyle);
        selectedVersionLabel.setColor(Color.lightGray);
        main.add(selectedVersionLabel).padBottom(15).row();

        // Auto-select the first version if available
        if (!jarFiles.isEmpty()) {
            selectVersion(jarFiles.get(0));
        }

        // Launch button
        TextButton launchBtn = new TextButton("LAUNCH", launchStyle);
        launchBtn.clicked(() -> {
            if (selectedJar != null) {
                launchMindustry(selectedJar.absolutePath());
            }
        });

        arc.util.Timer.schedule(() -> {
            int h = Core.graphics.getHeight();
            int w = Core.graphics.getWidth();
            launchBtn.x += 1f;
            float RealX = launchBtn.x + launchBtn.getWidth();
            if(RealX >= w) {
                launchBtn.x = 0;
            }
        }, 1f, 0.01f);
        launchBtn.setDisabled(jarFiles.isEmpty());
        main.add(launchBtn).width(280).height(60).padBottom(90);

        scene.add(main);
    }

    private void selectVersion(Fi jar) {
        selectedJar = jar;
        if(selectedVersionLabel != null) {
            selectedVersionLabel.setText("Selected: " + jar.name());
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
                 new ProcessBuilder("java", "-jar", jarPath)
                        .inheritIO()
                        .start()
                         .waitFor();


            } catch (IOException | InterruptedException e) {
                Log.err("Start failed: ", e);
            }

    }
    public static void main(String[] args){
            SdlConfig config = new SdlConfig();
            config.title = "Singularity Launcher";
            config.width = 600;
            config.height = 550;

            new SdlApplication(new SingularityLauncher(), config);

    }

}