package data.scripts.patrolfleet.managers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;

import java.util.*;

public class AoTDFactionPatrolsManager {
    public static String keyToData = "$aotd_patrol_fleet_man_instance";
    public LinkedHashMap<String, BasePatrolFleet> fleetsCurrentlyInField = new LinkedHashMap<>();
    public MutableStat daysPerFP = new MutableStat(5f);
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

    public MutableStat getDaysPerFP() {
        return daysPerFP;
    }

    public void advanceFleets(float amount){

        fleetsCurrentlyInField.values().forEach(x->x.advance(amount));
        //Logic for grounding and un-groudning


    }
    public void advanceAfterFleets(float amount){
        int currFp = getAvailableFP();
        List<BasePatrolFleet>fleets = fleetsCurrentlyInField.values().stream().sorted(new Comparator<BasePatrolFleet>() {
            @Override
            public int compare(BasePatrolFleet o1, BasePatrolFleet o2) {
                return Integer.compare(o1.getFPTaken(), o2.getFPTaken());
            }
        }).toList();
        if(currFp<0){
            for (BasePatrolFleet fleet : fleets) {
                if(fleet.isGrounded())continue;
                fleet.setGrounded(true);
                currFp+=fleet.geTotalFpTaken();
                if(currFp>=0){
                    break;
                }

            }
        }
        else{
            for (BasePatrolFleet fleet : fleets) {
                if(!fleet.isGrounded())continue;
                currFp-=fleet.geTotalFpTaken();
                if(currFp>=0){
                    fleet.setGrounded(false);
                }
                else{
                    //If smallest fleet makes counter go minus there is no point to go further
                    break;
                }

            }
        }

    }


    public int getFPUsed(boolean ignoreGrounded) {
        int curr = 0;
        for (BasePatrolFleet basePatrolFleet : fleetsCurrentlyInField.values()) {
            if(basePatrolFleet.isGrounded()&&!ignoreGrounded)continue;
            curr += basePatrolFleet.geTotalFpTaken();
        }
        return curr;
    }


    public int getFPUsedByMarket(MarketAPI market) {
        int curr = 0;
        for (BasePatrolFleet fleet : getAssignedFleetsForMarket(market)) {
            curr += fleet.geTotalFpTaken();
        }
        return curr;
    }

    public int getTotalFpGenerated() {

        return FleetPointUtilis.getFleetPointsGeneratedByFaction(Global.getSector().getPlayerFaction());
    }

    public int getAvailableFP() {
        return getTotalFpGenerated() - getFPUsed(false);
    }

    public static AoTDFactionPatrolsManager getInstance() {
        if (!Global.getSector().getPersistentData().containsKey(keyToData)) {
            setInstance();
        }
        return (AoTDFactionPatrolsManager) Global.getSector().getPersistentData().get(keyToData);
    }

    public void addNewFleet(BasePatrolFleet fleet) {
        fleetsCurrentlyInField.put(fleet.getId(), fleet);
    }

    public void removeFleet(String id) {
        BasePatrolFleet fleet = fleetsCurrentlyInField.get(id);
        fleetsCurrentlyInField.remove(id);
        fleet.setTiedTo(null);

    }
    public BasePatrolFleet getFleet(String id){
        return fleetsCurrentlyInField.get(id);
    }

    public static void setInstance() {
        AoTDFactionPatrolsManager instance = new AoTDFactionPatrolsManager();
        Global.getSector().getPersistentData().put(keyToData, instance);
    }
}
