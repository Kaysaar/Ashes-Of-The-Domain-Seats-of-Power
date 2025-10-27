package data.ui.timeline;

import data.plugins.AoDCapitalsModPlugin;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;

public class PathManager {
    public static String getTimelineScreenshotsPath() {
        try {
            // Path to starfarer.api.jar inside starsector-core
            String jarPath = AoDCapitalsModPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();


            // Strip off "starsector-core/starfarer.api.jar"
            String gameRoot;
            if (jarPath.endsWith("jars/AodCapitals.jar")) {
                gameRoot = jarPath.substring(0, jarPath.length() - "jars/AodCapitals.jar".length());
            } else {
                // Fallback: just go two directories up from the JAR
                int coreIndex = jarPath.indexOf("/jars/");
                if (coreIndex > 0) {
                    gameRoot = jarPath.substring(0, coreIndex + 1);
                } else {
                    gameRoot = jarPath;
                }
            }

            // Construct path to your mod’s subfolder
            String modFolder = "timeline_screenshots/";
            return gameRoot + modFolder;
        } catch (Exception e) {
            return "";
        }
    }
    public static String getPatrolFleetDataPath() {
        try {
            // Path to starfarer.api.jar inside starsector-core
            String jarPath = AoDCapitalsModPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();


            // Strip off "starsector-core/starfarer.api.jar"
            String gameRoot;
            if (jarPath.endsWith("jars/AodCapitals.jar")) {
                gameRoot = jarPath.substring(0, jarPath.length() - "jars/AodCapitals.jar".length());
            } else {
                // Fallback: just go two directories up from the JAR
                int coreIndex = jarPath.indexOf("/jars/");
                if (coreIndex > 0) {
                    gameRoot = jarPath.substring(0, coreIndex + 1);
                } else {
                    gameRoot = jarPath;
                }
            }

            // Construct path to your mod’s subfolder
            String modFolder = PatrolTemplateManager.directoryForTemplates;
            return gameRoot + modFolder;
        } catch (Exception e) {
            return "";
        }
    }

}
