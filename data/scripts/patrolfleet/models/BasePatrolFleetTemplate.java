package data.scripts.patrolfleet.models;

import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import data.ui.patrolfleet.templates.shiplist.components.ShipPanelData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static data.scripts.patrolfleet.utilis.FleetPointUtilis.getHullFP;

public class BasePatrolFleetTemplate {

    public String nameOfTemplate;
    public LinkedHashMap<String, Integer> assignedShipsThatShouldSpawn = new LinkedHashMap<>();
    public LinkedHashMap<String, PatrolShipData> data = new LinkedHashMap<>();
    public LinkedHashMap<String, String> modsReq = new LinkedHashMap<>();

    public String getNameOfTemplate() {
        return nameOfTemplate;
    }

    public PatrolTemplateDataPackage getPackage() {
        String name = this.nameOfTemplate;
        StringBuilder builder = new StringBuilder();
        StringBuilder builder1 = new StringBuilder();
        if (this.data.isEmpty()) return null;
        boolean appendedDataB = false;
        boolean appendedData1B = false;
        for (PatrolShipData entry : this.data.values()) {
            builder.append(entry.shipId);
            builder.append(":");
            builder.append(this.assignedShipsThatShouldSpawn.get(entry.shipId));
            builder.append(";");
            appendedDataB = true;


        }
        for (Map.Entry<String, String> entry : this.modsReq.entrySet()) {
            builder1.append(entry.getKey());
            builder1.append("<&>");
            builder1.append(entry.getValue());
            builder1.append(";");
            appendedData1B = true;
        }
        String data = builder.toString();

        String data1 = builder1.toString();

        String prunedData1 = null;
        String prunedData = null;
        if (appendedData1B) {
            prunedData1 = data1.substring(0, data1.length() - 1);
        }
        prunedData = data.substring(0, data.length() - 1);


        return new PatrolTemplateDataPackage(name, prunedData, prunedData1);

    }

    public float getTotalFleetPoints() {
        float value = 0;
        for (Map.Entry<String, Integer> entry : assignedShipsThatShouldSpawn.entrySet()) {
            value += (entry.getValue() * getHullFP(entry.getKey()));
        }
        return value;
    }

    public LinkedHashSet<String> getManufactures() {
        LinkedHashSet<String> manufactures = new LinkedHashSet<>();
        for (PatrolShipData object : data.values()) {
            if (!object.isShipPresent()) {
                manufactures.add("Data error");
            } else {
                manufactures.add(Global.getSettings().getHullSpec(object.shipId).getManufacturer());
            }
        }
        return manufactures;
    }

    public void sortShipsByFPDescInPlace() {
        if (assignedShipsThatShouldSpawn == null || assignedShipsThatShouldSpawn.isEmpty()) return;

        // Copy entries so we can sort them
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(assignedShipsThatShouldSpawn.entrySet());

        // Sort by Fleet Points (desc), tie-break by id (asc)
        entries.sort((e1, e2) -> {
            float fp1 = getHullFP(e1.getKey());
            float fp2 = getHullFP(e2.getKey());
            int cmp = Float.compare(fp2, fp1); // desc
            if (cmp != 0) return cmp;
            return e1.getKey().compareTo(e2.getKey());
        });

        // Rebuild the LinkedHashMap in the new order
        LinkedHashMap<String, Integer> reordered = new LinkedHashMap<>(entries.size());
        for (Map.Entry<String, Integer> e : entries) {
            reordered.put(e.getKey(), e.getValue());
        }
        assignedShipsThatShouldSpawn.clear();
        assignedShipsThatShouldSpawn.putAll(reordered);
    }

    public String getShipWithHighestFP() {
        if (assignedShipsThatShouldSpawn == null || assignedShipsThatShouldSpawn.isEmpty()) return null;

        String bestId = null;
        float bestFp = Float.NEGATIVE_INFINITY;

        for (String id : assignedShipsThatShouldSpawn.keySet()) {
            float fp = getHullFP(id);
            if (fp > bestFp) {
                bestFp = fp;
                bestId = id;
            }
        }
        return bestId;
    }

    public boolean doesKnowAllShips() {
        if (!isTemplateValidForModList()) return false;
        for (PatrolShipData value : data.values()) {
            if (!ShipPanelData.learnedShips.contains(Global.getSettings().getHullSpec(value.shipId))) return false;
        }
        return true;
    }

    public BasePatrolFleetTemplate(LinkedHashMap<String, Integer> shipsInFleet, String nameOfTemplate) {
        this.nameOfTemplate = nameOfTemplate;
        this.assignedShipsThatShouldSpawn = shipsInFleet;
        for (String entry : shipsInFleet.keySet()) {
            PatrolShipData shipData = new PatrolShipData(entry);
            data.put(entry, shipData);
        }
        for (PatrolShipData value : data.values()) {
            if (!AshMisc.isStringValid(value.modID) || "vanilla".equals(value.modID)) continue;
            modsReq.put(value.modID, Global.getSettings().getModManager().getModSpec(value.modID).getName());
        }

    }

    public BasePatrolFleetTemplate() {
    }

    public static BasePatrolFleetTemplate loadFromJsonFile(JSONObject object) throws JSONException {
        BasePatrolFleetTemplate template = new BasePatrolFleetTemplate();
        String id = object.getString("name");
        if (!AshMisc.isStringValid(id)) return null;
        template.nameOfTemplate = id;
        ArrayList<String> entries = AshMisc.loadEntries(object.getString("data"), ";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            PatrolShipData data = new PatrolShipData(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            template.assignedShipsThatShouldSpawn.put(parts[0], amount);
            template.data.put(parts[0], data);
        }
        ArrayList<String> entriesMod = AshMisc.loadEntries(object.getString("modsReq"), ";");
        for (String string : entriesMod) {
            String[] sp = string.split("<&>");
            template.modsReq.put(sp[0], sp[1]);
            try {
                ModSpecAPI spec = Global.getSettings().getModManager().getModSpec(sp[0]);
                if (spec != null) {
                    template.modsReq.replace(sp[0], spec.getName());
                }
            } catch (Exception e) {

            }


        }


        return template;
    }


    public boolean isTemplateValidForModList() {
        for (PatrolShipData value : data.values()) {
            if (!value.isShipPresent()) return false;
        }
        return true;
    }


}
