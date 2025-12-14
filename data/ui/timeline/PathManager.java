package data.ui.timeline;

import com.fs.starfarer.api.Global;
import data.plugins.AoDCapitalsModPlugin;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;

import java.nio.file.Paths;

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
    public static String getStarsectorRootPath() {
        try {
            String jarPath = Global.getSettings().getModManager().getModSpec("aotd_sop").getPath();

            String p = jarPath;
            p =  p.replace("\\","/");
            String gameRoot;

            // Prefer trimming at "starsector-core"
            int coreIdx = p.indexOf("/starsector-core/");
            if (coreIdx > 0) {
                gameRoot = p.substring(0, coreIdx + 1); // keep trailing slash
            } else {
                // Otherwise, trim at "/mods/" (parent of mods) or "/jars/"
                int modsIdx = p.indexOf("/mods/");
                if (modsIdx > 0) {
                    gameRoot = p.substring(0, modsIdx + 1);
                } else {
                    int jarsIdx = p.indexOf("/jars/");
                    if (jarsIdx > 0) {
                        gameRoot = p.substring(0, jarsIdx + 1);
                    } else {
                        gameRoot = p.endsWith("/") ? p : p + "/";
                    }
                }
            }

            // Optional: on Windows you might see a leading "/" before "C:/"
            if (gameRoot.matches("^/[A-Za-z]:/.*")) {
                gameRoot = gameRoot.substring(1);
            }
            if(gameRoot.charAt(0)=='/'){
                gameRoot = gameRoot.substring(1);
            }

            return Paths.get(gameRoot).toAbsolutePath().normalize().toString();
        } catch (Exception e) {
            return "";
        }
    }
    public static String getPatrolFleetDataPath() {
        try {
            // Path to starfarer.api.jar inside starsector-core
            String modFolder = PatrolTemplateManager.directoryForTemplates;

            return getStarsectorRootPath()+"/saves/common/Aotd-sop-patrol-templates/" + modFolder;
        } catch (Exception e) {
            return "";
        }
    }

}
