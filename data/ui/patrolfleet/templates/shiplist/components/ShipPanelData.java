package data.ui.patrolfleet.templates.shiplist.components;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.vok.campaign.econ.globalproduction.models.GPManager;
import data.kaysaar.aotd.vok.scripts.specialprojects.models.ProjectReward;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShipPanelData {
    public static LinkedHashMap<String, Integer> shipSizeInfo = new LinkedHashMap<>();
    public static LinkedHashMap<String, Integer> shipManInfo = new LinkedHashMap<>();

    public static LinkedHashMap<String, Integer> getShipSizeInfo() {
        return shipSizeInfo;
    }

    public static LinkedHashMap<String, Integer> getShipManInfo() {
        return shipManInfo;
    }

    public static String ensureManBeingNotNull(String man) {
        return man == null ? "Unknown" : man;
    }

    public static boolean arrayContains(ArrayList<String> array, String key) {
        for (String s : array) {
            if (s.equals(key)) return true;
        }
        return false;
    }

    public static ArrayList<ShipHullSpecAPI>learnedShips = new ArrayList<>();
    public static LinkedHashMap<String, Integer> shipTypeInfo = new LinkedHashMap<>();

    public static LinkedHashMap<String, Integer> getShipTypeInfo() {
        return shipTypeInfo;
    }

    public static LinkedHashMap<String,Integer>getManuForShipsUnbound(){
        LinkedHashMap<String, Integer> shipManInfos = new LinkedHashMap<>();
        for (ShipHullSpecAPI learnedShipPackage : Global.getSettings().getAllShipHullSpecs()) {
            String man = learnedShipPackage.getManufacturer();
            if (man == null || man.isEmpty()) {
                man = "Unknown";
            }
            if (shipManInfos.get(man) == null) {
                shipManInfos.put(man, 1);
            } else {
                shipManInfos.compute(man, (k, amount) -> amount + 1);
            }
        }
        int val = 0;
        for (Integer value : shipManInfos.values()) {
            val += value;
        }
        shipManInfos.put("All designs", val);
        shipManInfos = sortByValueDescending(shipManInfos);
        return shipManInfos;

    }
    public static void populateShipInfo() {
        shipManInfo.clear();
        LinkedHashMap<String, Integer> shipManInfos = new LinkedHashMap<>();
        for (ShipHullSpecAPI learnedShipPackage : learnedShips) {
            String man = learnedShipPackage.getManufacturer();
            if (man == null || man.isEmpty()) {
                man = "Unknown";
            }
            if (shipManInfos.get(man) == null) {
                shipManInfos.put(man, 1);
            } else {
                shipManInfos.compute(man, (k, amount) -> amount + 1);
            }
        }
        int val = 0;
        for (Integer value : shipManInfos.values()) {
            val += value;
        }
        shipManInfos.put("All designs", val);

        shipManInfo.putAll(sortByValueDescending(shipManInfos));

    }

    public static LinkedHashMap<String, Integer> sortByValueDescending(LinkedHashMap<String, Integer> map) {
        // Convert the map's entries to a list
        List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());

        // Sort the list by value in descending order
        Collections.sort((List<Map.Entry<String, Integer>>) list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Create a new LinkedHashMap to store the sorted entries
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    public static void updateList(){
        learnedShips.clear();
        learnedShips.addAll(getLearnedShipPackages());
    }
    public static ArrayList<ShipHullSpecAPI> getLearnedShipPackages() {
        ArrayList<ShipHullSpecAPI> list = new ArrayList<>();
        for (ShipHullSpecAPI allShipHullSpec : Global.getSettings().getAllShipHullSpecs()) {
            if (allShipHullSpec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.STATION)) continue;
            if (allShipHullSpec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.MODULE)) continue;
            if (allShipHullSpec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNDER_PARENT)) continue;
            if (allShipHullSpec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE)) continue;
            if (allShipHullSpec.getHullSize().equals(ShipAPI.HullSize.FIGHTER)) continue;

            if(allShipHullSpec.hasTag(Tags.MODULE_HULL_BAR_ONLY))continue;

            if (Global.getSettings().getHullIdToVariantListMap().get(allShipHullSpec.getHullId()).isEmpty()) {
                boolean found = false;
                for (String allVariantId : Global.getSettings().getAllVariantIds()) {
                    if (allVariantId.contains(allShipHullSpec.getHullId())) {
                        found = true;
                        break;
                    }

                }
                if (!found) continue;
            }
            if (Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
                if (GPManager.hasSpecialProject(ProjectReward.ProjectRewardType.SHIP, allShipHullSpec.getHullId()))
                    continue;
            }
            if (!Global.getSector().getPlayerFaction().knowsShip(allShipHullSpec.getHullId())&&!Global.getSettings().isDevMode()) continue;
            list.add(allShipHullSpec);


        }
        return list;
    }


    public static void populateShipSizeInfo() {
        if (shipSizeInfo == null) shipSizeInfo = new LinkedHashMap<>();
        shipSizeInfo.clear();
        LinkedHashMap<String, Integer> shipManInfo = new LinkedHashMap<>();
        for (ShipHullSpecAPI learnedShipPackage : learnedShips) {
            String indicator = Misc.getHullSizeStr(learnedShipPackage.getHullSize());
            if (shipManInfo.get(indicator) == null) {
                shipManInfo.put(indicator, 1);
            } else {
                int amount = shipManInfo.get(indicator);
                shipManInfo.put(indicator, amount + 1);
            }
        }
        int val = 0;
        for (Integer value : shipManInfo.values()) {
            val += value;
        }
        shipManInfo.put("All sizes", val);
        shipSizeInfo.putAll(sortByValueDescending(shipManInfo));
    }

    @Nullable
    public static String getVaraint(ShipHullSpecAPI allShipHullSpec) {
        String variantId = null;
        for (String allVariantId : Global.getSettings().getAllVariantIds()) {
            ShipVariantAPI variant = Global.getSettings().getVariant(allVariantId);
            if (variant.getVariantFilePath() == null) continue;
            if (variant.getHullSpec().getHullId().equals(allShipHullSpec.getHullId())) {
                variantId = allVariantId;
                break;
            }
        }
        if (variantId == null) {
            final String withHull = allShipHullSpec.getHullId() + "_Hull";
            for (String id : Global.getSettings().getAllVariantIds()) {
                if (withHull.equalsIgnoreCase(id)) {
                    variantId = id;
                    break;
                }
            }
        }

        return variantId;

    }

    public static String getType(ShipHullSpecAPI ship) {
        if (ship.isPhase()) {
            return "Phase";
        }
        ShipVariantAPI spec = Global.getSettings().getVariant(getVaraint(ship));
        if (spec.isCivilian()) {
            return "Civilian";
        }
        if (spec.isCarrier()) {
            return "Carrier";
        }
        return "Warship";
    }

    public static void populateShipTypeInfo() {
        if (shipTypeInfo == null) shipTypeInfo = new LinkedHashMap<>();
        shipTypeInfo.clear();
        LinkedHashMap<String, Integer> shipManInfo = new LinkedHashMap<>();
        for (ShipHullSpecAPI learnedShipPackage : learnedShips) {
            String indicator = getType(learnedShipPackage);
            if (shipManInfo.get(indicator) == null) {
                shipManInfo.put(indicator, 1);
            } else {
                int amount = shipManInfo.get(indicator);
                shipManInfo.put(indicator, amount + 1);
            }
        }
        int val = 0;
        for (Integer value : shipManInfo.values()) {
            val += value;
        }
        shipManInfo.put("All types", val);
        shipTypeInfo.putAll(sortByValueDescending(shipManInfo));
    }


}
