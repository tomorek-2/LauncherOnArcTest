package singlaunch;

public class LauncherSettings {
    public int minMemoryMb = 512;
    public int maxMemoryMb = 2048;
    public String javaPath = "";
    public boolean keepLauncherOpen = true;
    public String selectedInstanceId = "default";
    public String selectedVersionId = "";
    public String extraJvmArgs = "";

    public String resolveJavaPath() {
        if (javaPath != null && !javaPath.isBlank()) {
            return javaPath.trim();
        }
        return PathUtil.defaultJava();
    }
}
