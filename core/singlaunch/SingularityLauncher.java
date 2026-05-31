package singlaunch;

import arc.ApplicationCore;
import arc.Core;
import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import backends/backend-sdl/src/arc/backend/sdl;
import arc.backend.sdl.SdlConfig;
import arc.files.Fi;
import arc.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class SingularityLauncher extends ApplicationCore {

    private static final String VERSIONS_DIR = "versions";
    private ArrayList<Fi> jarFiles = new ArrayList<>();

    @Override
    public void setup() {
        Log.info("Launcher started!");

        Fi dir = Core.files.local(VERSIONS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Log.info("Grep versions: " + dir.absolutePath());

        for (Fi file : dir.list()) {
            if (file.extEquals("jar")) {
                jarFiles.add(file);
                Log.info("Version found: " + file.name());
            }
        }

        if (!jarFiles.isEmpty()) {
            Fi targetJar = jarFiles.get(0);
            launchMindustry(targetJar.absolutePath());
        } else {
            Log.warn("Folder 'versions' is empty!");
        }
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
        config.width = 400;
        config.height = 300;

        new SdlApplication(new MindustryLauncher(), config);
    }
}
