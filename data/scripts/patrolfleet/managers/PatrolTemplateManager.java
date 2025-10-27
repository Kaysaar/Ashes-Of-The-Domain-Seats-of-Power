package data.scripts.patrolfleet.managers;

import com.fs.starfarer.api.Global;
import data.misc.ReflectionUtilis;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.models.PatrolTemplateDataPackage;
import data.ui.timeline.PathManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PatrolTemplateManager {
    public static LinkedHashMap<String, BasePatrolFleetTemplate> templates = new LinkedHashMap<>();
    public static final String directoryForTemplates = "data/patrol_templates/patrol_fleet_template_data.csv";
    public static boolean updatedList = false;

    public static void loadAllExistingTemplates() {
        if (templates == null) {
            templates = new LinkedHashMap<>();

        }
        templates.clear();
        try {
            JSONArray resArray = Global.getSettings().getMergedSpreadsheetDataForMod("name", directoryForTemplates, "aotd_sop");
            for (int i = 0; i < resArray.length(); i++) {
                JSONObject obj = resArray.getJSONObject(i);
                BasePatrolFleetTemplate template = BasePatrolFleetTemplate.loadFromJsonFile(obj);

                if (template != null) {
                    templates.put(template.nameOfTemplate, template);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean saveAllExistingTemplates() {
        ArrayList<PatrolTemplateDataPackage> packages = new ArrayList<>();
        try {
            for (BasePatrolFleetTemplate value : templates.values()) {
                PatrolTemplateDataPackage pack = value.getPackage();
                if (pack != null) {
                    packages.add(pack);
                }

            }

        } catch (Exception e) {
            return false;
        }
        if (!packages.isEmpty()) {
            Object file = ReflectionUtilis.getFileWriter(PathManager.getPatrolFleetDataPath(), false);
            writeCsvRow(file, "name", "data", "modsReq");
            for (PatrolTemplateDataPackage aPackage : packages) {
                writeCsvRow(file, aPackage.name, aPackage.data, aPackage.modsReq);
            }
            ReflectionUtilis.invokeMethodWithAutoProjection("flush", file);
        }


        return true;
    }

    public static LinkedHashMap<String, BasePatrolFleetTemplate> getTemplatesAvailableSorted() {
        LinkedHashMap<String, BasePatrolFleetTemplate> survivors = new LinkedHashMap<>(PatrolTemplateManager.templates);

        for (BasePatrolFleetTemplate template : PatrolTemplateManager.templates.values()) {
            if (!template.isTemplateValidForModList()) {
                survivors.remove(template.getNameOfTemplate());
            } else if (!template.doesKnowAllShips()) {
                survivors.remove(template.getNameOfTemplate());
            }
        }
        return survivors.entrySet().stream()
                .sorted(new Comparator<Map.Entry<String, BasePatrolFleetTemplate>>() {
                    @Override
                    public int compare(Map.Entry<String, BasePatrolFleetTemplate> o1, Map.Entry<String, BasePatrolFleetTemplate> o2) {
                        return Integer.compare((int) o2.getValue().getTotalFleetPoints(), (int) o1.getValue().getTotalFleetPoints());
                    }
                }) // Sort by value descending
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // Preserve order
                ));
    }

    static String csvCell(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.indexOf(',') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0 || s.indexOf('"') >= 0;
        if (!needsQuotes) return s;
        // escape quotes by doubling them
        String esc = s.replace("\"", "\"\"");
        return "\"" + esc + "\"";
    }

    static void writeCsvRow(Object w, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) ReflectionUtilis.invokeMethodWithAutoProjection("write", w, ",");
            ReflectionUtilis.invokeMethodWithAutoProjection("write", w, csvCell(cells[i]));
        }
        ReflectionUtilis.invokeMethodWithAutoProjection("write", w, "\n");

    }

}
