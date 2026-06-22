package singlaunch;

public final class GameVersionUtil {
    private GameVersionUtil() {}

    public static int[] parse(String versionId, String versionName) {
        String source = versionId != null ? versionId : "";
        if (versionName != null) source += " " + versionName;
        source = source.replace("Mindustry", "").replace("mindustry", "");

        int build = 0;
        int revision = 0;

        var matcher = java.util.regex.Pattern.compile("v?(\\d+)(?:\\.(\\d+))?").matcher(source);
        int bestBuild = -1;
        while (matcher.find()) {
            int candidateBuild = Integer.parseInt(matcher.group(1));
            int candidateRevision = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            if (candidateBuild > bestBuild) {
                bestBuild = candidateBuild;
                build = candidateBuild;
                revision = candidateRevision;
            }
        }
        return new int[]{build, revision};
    }

    public static boolean isAtLeast(int build, int revision, String required) {
        if (build <= 0 || required == null || required.isBlank()) return true;
        int dot = required.indexOf('.');
        if (dot >= 0) {
            int major = parseInt(required.substring(0, dot));
            int minor = parseInt(required.substring(dot + 1));
            return build > major || (build == major && revision >= minor);
        }
        return build >= parseInt(required);
    }

    public static String format(int build, int revision) {
        if (build <= 0) return "неизвестно";
        return revision == 0 ? String.valueOf(build) : build + "." + revision;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
