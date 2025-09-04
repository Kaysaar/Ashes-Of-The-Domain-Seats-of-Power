package data.ui.timeline;

public class PathManager {
    public static String getTimelineScreenshotsPath() {
        try {
            // Path to starfarer.api.jar inside starsector-core
            String jarPath = com.fs.starfarer.api.Global.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath()
                    .replace('\\', '/');

            // Strip off "starsector-core/starfarer.api.jar"
            String gameRoot;
            if (jarPath.endsWith("starsector-core/starfarer.api.jar")) {
                gameRoot = jarPath.substring(0, jarPath.length() - "starsector-core/starfarer.api.jar".length());
            } else {
                // Fallback: just go two directories up from the JAR
                int coreIndex = jarPath.indexOf("/starsector-core/");
                if (coreIndex > 0) {
                    gameRoot = jarPath.substring(0, coreIndex + 1);
                } else {
                    gameRoot = jarPath;
                }
            }

            // Construct path to your mod’s subfolder
            String modFolder = "mods/Ashes of  The Domain -Seats Of Power/timeline_screenshots/";
            return gameRoot + modFolder;
        } catch (Exception e) {
            return "";
        }
    }


}
