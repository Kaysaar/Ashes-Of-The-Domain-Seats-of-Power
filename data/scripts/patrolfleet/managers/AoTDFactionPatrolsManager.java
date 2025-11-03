package data.scripts.patrolfleet.managers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;

import java.util.*;

public class AoTDFactionPatrolsManager {
    public static String keyToData = "$aotd_patrol_fleet_man_instance";
    public static int MAX_ADMIRALTY_LEV = 6;
    public static LinkedHashMap<Integer, Integer> levels = new LinkedHashMap<>();

    static {
        levels.put(1, 0);
        levels.put(2, 500);
        levels.put(3, 1000);
        levels.put(4, 1500);
        levels.put(5, 2000);
        levels.put(6, 2500);

    }


    public LinkedHashMap<String, BasePatrolFleet> fleetsCurrentlyInField = new LinkedHashMap<>();
    public MutableStat daysPerFP = new MutableStat(5f);
    @Deprecated
    public MutableStat admiralty = new MutableStat(0f);

    @Deprecated
    public MutableStat getAdmiralty() {
        return admiralty;
    }

    public MutableStat additionalFPConsumed = new MutableStat(0f);
    public MutableStat additionalFpGranted = new MutableStat(0f);

    public MutableStat getAdditionalFPConsumed() {
        if (additionalFPConsumed == null) additionalFPConsumed = new MutableStat(0f);
        return additionalFPConsumed;
    }

    public MutableStat getAdditionalFpGranted() {
        if (additionalFpGranted == null) additionalFpGranted = new MutableStat(0f);
        return additionalFpGranted;
    }


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
                Comparator.comparingInt(BasePatrolFleet::getFPTaken)
                        .reversed()
                        .thenComparing(BasePatrolFleet::getId)
        );
        return fleets;
    }

    public MutableStat getDaysPerFP() {
        return daysPerFP;
    }


    public void advanceFleets(float amount) {

        fleetsCurrentlyInField.values().forEach(x -> x.advance(amount));
        //Logic for grounding and un-groudning


    }

    public void advanceAfterFleets(float amount) {
        int currFp = getAvailableFP();
        FactionDoctrineAPI doctrine = Global.getSector().getPlayerFaction().getDoctrine();

        getAdditionalFPConsumed().modifyFlat("aotd_admiralty", levels.get(doctrine.getOfficerQuality()), "Admiralty level " + doctrine.getOfficerQuality());

        if (currFp < 0) {
            int currentLevel = Math.min(MAX_ADMIRALTY_LEV, doctrine.getOfficerQuality());
            int deficit = -currFp;

            int targetBudget = levels.get(currentLevel) - deficit;
            int bestLevel = 1;
            for (int lvl = currentLevel; lvl >= 1; lvl--) {
                if (levels.get(lvl) <= targetBudget) {
                    bestLevel = lvl;
                    break;
                }
            }
            if (bestLevel != currentLevel) {
                doctrine.setOfficerQuality(bestLevel);
                if(amount>0){
                    MessageIntel msg = new MessageIntel();
                    msg.addLine("Admiralty reduced due to lack of FP", Misc.getBasePlayerColor());
                    msg.addLine(BaseIntelPlugin.BULLET + "Current level %s", Misc.getTextColor(),
                            new String[]{"" + bestLevel},
                            Misc.getHighlightColor());

                    msg.setSound(Sounds.REP_LOSS);
                    Global.getSector().getCampaignUI().addMessage(msg, CommMessageAPI.MessageClickAction.COLONY_INFO);
                }

            }

        }
        List<BasePatrolFleet> fleets = fleetsCurrentlyInField.values().stream().sorted(new Comparator<BasePatrolFleet>() {
            @Override
            public int compare(BasePatrolFleet o1, BasePatrolFleet o2) {
                return Integer.compare(o1.getFPTaken(), o2.getFPTaken());
            }
        }).toList();
        if (currFp < 0) {
            for (BasePatrolFleet fleet : fleets) {
                if (fleet.isGrounded()) continue;
                fleet.setGrounded(true);
                currFp += fleet.geTotalFpTaken();
                if (currFp >= 0) {
                    break;
                }

            }
        }
        currFp = getAvailableFP();
        if (currFp > 0) {
            for (BasePatrolFleet fleet : fleets) {
                if (!fleet.isGrounded()) continue;
                currFp -= fleet.geTotalFpTaken();
                if (currFp >= 0) {
                    fleet.setGrounded(false);
                } else {
                    //If smallest fleet makes counter go minus there is no point to go further
                    break;
                }

            }
        }

    }


    public int getFPUsed(boolean ignoreGrounded) {
        int curr = 0;
        for (BasePatrolFleet basePatrolFleet : fleetsCurrentlyInField.values()) {
            if (basePatrolFleet.isGrounded() && !ignoreGrounded) continue;
            curr += basePatrolFleet.geTotalFpTaken();
        }
        curr += getAdditionalFPConsumed().getModifiedInt();
        return curr;
    }

    public int getFpConsumedByAdmiralty() {
        if(getAdditionalFPConsumed().getFlatStatMod("aotd_admiralty")==null){
            getAdditionalFPConsumed().modifyFlat("aotd_admiralty",0,"Admiralty level 1");
        }
        return Math.round(getAdditionalFPConsumed().getFlatStatMod("aotd_admiralty").getValue());
    }

    public int getFPUsedByMarket(MarketAPI market) {
        int curr = 0;
        for (BasePatrolFleet fleet : getAssignedFleetsForMarket(market)) {
            curr += fleet.geTotalFpTaken();
        }
        return curr;
    }

    public int getTotalFpGenerated() {

        return FleetPointUtilis.getFleetPointsGeneratedByFaction(Global.getSector().getPlayerFaction()) + getAdditionalFpGranted().getModifiedInt();
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

    public BasePatrolFleet getFleet(String id) {
        return fleetsCurrentlyInField.get(id);
    }

    public static void setInstance() {
        AoTDFactionPatrolsManager instance = new AoTDFactionPatrolsManager();
        Global.getSector().getPersistentData().put(keyToData, instance);
    }
}
