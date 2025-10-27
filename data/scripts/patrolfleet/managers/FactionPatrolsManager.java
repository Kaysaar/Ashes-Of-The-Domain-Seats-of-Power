package data.scripts.patrolfleet.managers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;

import java.util.*;

public class FactionPatrolsManager {
    public static String keyToData = "$aotd_patrol_fleet_man_instance";
    public LinkedHashMap<String, BasePatrolFleet> fleetsCurrentlyInField = new LinkedHashMap<>();

    public ArrayList<BasePatrolFleet> getAssignedFleetsForMarket(MarketAPI market) {
        ArrayList<BasePatrolFleet> fleets = new ArrayList<>();
        if (market == null) return fleets;

        for (BasePatrolFleet value : fleetsCurrentlyInField.values()) {
            if (value.getTiedTo() != null && market.getId().equals(value.getTiedTo().getId())) {
                fleets.add(value);
            }
        }

        // Sort by FP descending; tie-break by id for stable order
        fleets.sort(
                java.util.Comparator.comparingInt(BasePatrolFleet::getFPTaken)
                        .reversed()
                        .thenComparing(BasePatrolFleet::getId)
        );
        return fleets;
    }


    public int getFPUsed() {
        int curr = 0;
        for (BasePatrolFleet basePatrolFleet : fleetsCurrentlyInField.values()) {
            curr += basePatrolFleet.getFPTaken();
        }
        return curr;
    }

    public int getFPUsedByMarket(MarketAPI market) {
        int curr = 0;
        for (BasePatrolFleet fleet : getAssignedFleetsForMarket(market)) {
            curr += fleet.getFPTaken();
        }
        return curr;
    }

    public int getTotalFpGenerated() {
        return FleetPointUtilis.getFleetPointsGeneratedByFaction(Global.getSector().getPlayerFaction());
    }

    public int getAvailableFP() {
        return getTotalFpGenerated() - getFPUsed();
    }

    public static FactionPatrolsManager getInstance() {
        if (!Global.getSector().getPersistentData().containsKey(keyToData)) {
            setInstance();
        }
        return (FactionPatrolsManager) Global.getSector().getPersistentData().get(keyToData);
    }

    public void addNewFleet(BasePatrolFleet fleet) {
        fleetsCurrentlyInField.put(fleet.getId(), fleet);
    }

    public void removeFleet(String id) {
        fleetsCurrentlyInField.remove(id);
    }
    public BasePatrolFleet getFleet(String id){
        return fleetsCurrentlyInField.get(id);
    }

    public static void setInstance() {
        FactionPatrolsManager instance = new FactionPatrolsManager();
        Global.getSector().getPersistentData().put(keyToData, instance);
    }
}
