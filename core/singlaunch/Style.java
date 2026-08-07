package singlaunch;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;

public class Style {
   public static TextButton.TextButtonStyle versionStyle = new TextButton.TextButtonStyle();
   public static TextButton.TextButtonStyle remoteStyle = new TextButton.TextButtonStyle();
    public static  TextButton.TextButtonStyle launchStyle = new TextButton.TextButtonStyle();
    public static Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
    public static TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
    public static Label.LabelStyle regularLabelStyle = new Label.LabelStyle();

    public static void load() {
        Color bg = Color.valueOf("1e1e24");
        Color panel = Color.valueOf("2b2b36");
        Color hover = Color.valueOf("3b3b46");
        Color accent = Color.valueOf("f05d23");
        Color textColor = Color.valueOf("ffffff");
        Color green = Color.valueOf("4caf50");
        Color greenHover = Color.valueOf("66bb6a");
        Drawable bgDrawable = drawable(bg);
        Drawable panelDrawable = drawable(panel);
        Drawable hoverDrawable = drawable(hover);
        Drawable accentDrawable = drawable(accent);
        Drawable greenDrawable = new SingularityLauncher().loadTexture("img.png");
        Drawable greenHoverDrawable = drawable(greenHover);
        versionStyle.up = drawable(Color.valueOf("2b2b36"));
        versionStyle.over = drawable(Color.valueOf("3b3b46"));
        versionStyle.down = drawable(Color.valueOf("f05d23"));
        versionStyle.font = FontsAWT.regular;
        versionStyle.fontColor = Color.white;

        remoteStyle.up = drawable(Color.valueOf("#4278cf"));
        remoteStyle.over = drawable(Color.valueOf("#7393c7"));
        remoteStyle.down = drawable(Color.valueOf("f05d23"));
        remoteStyle.font = FontsAWT.regular;
        remoteStyle.fontColor = Color.white;

        regularLabelStyle.font =FontsAWT.regular;
        regularLabelStyle.fontColor = Color.valueOf("ffffff");
        TextButton.TextButtonStyle versionStyle = new TextButton.TextButtonStyle();
        versionStyle.up = panelDrawable;
        versionStyle.over = hoverDrawable;
        versionStyle.down = accentDrawable;
        versionStyle.font = FontsAWT.regular;
        versionStyle.fontColor = textColor;

        launchStyle.up = greenDrawable;
        launchStyle.over = greenHoverDrawable;
        launchStyle.down = accentDrawable;
        launchStyle.font = FontsAWT.title;
        launchStyle.fontColor = textColor;


        titleLabelStyle.font = FontsAWT.title;
        textFieldStyle.font = FontsAWT.regular;
        textFieldStyle.fontColor = textColor;
        textFieldStyle.cursor = new SingularityLauncher().loadTexture("cat.png");
        titleLabelStyle.fontColor = accent;
    }

    private static Drawable drawable(Color color) {
        Pixmap pix = new Pixmap(1, 1);
        pix.fill(color);
        Texture tex = new Texture(pix);
        pix.dispose();

              Drawable d =new  TextureRegionDrawable(new TextureRegion(tex));
              return d;
    }
}
