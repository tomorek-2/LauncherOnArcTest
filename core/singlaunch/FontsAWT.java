package singlaunch;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.DoubleStream;

import javax.imageio.ImageIO;

import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;

//В классе UnQuis написал метод, чтобы генерировать шрифт. Да, я не могу шрифт добавить.
public class FontsAWT {
    public static Font title;
    public static Font regular;

    public static void load() {
        title = generateFont(48);
        regular = generateFont(22);
    }
    public static Font generateFont(int fontSize) {
        try {
            java.awt.Font awtFont = new java.awt.Font("SansSerif", 1, fontSize);
            String chars = " !\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~" + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюяёЁ";
            //String chars = " !\\\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
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
            tex.setFilter(Texture.TextureFilter.linear, Texture.TextureFilter.linear);
            TextureRegion region = new TextureRegion(tex);
            pix.dispose();
          //  tex.dispose();
            return new Font(fontFi, region, false);
        } catch (Exception e) {
            Log.err("Font generation failed for size " + fontSize, e);
            return null;
        }
    }
}
