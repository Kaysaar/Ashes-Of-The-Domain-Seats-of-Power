package data.scripts.patrolfleet.utilis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.FactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class FleetPointUtilis {
    public static final Logger log = Logger.getLogger(FleetPointUtilis.class);


    // Patrol type weights (used to compute Armada Points from patrol nums)
    public static LinkedHashMap<FleetFactory.PatrolType, Float> valuesFP = new LinkedHashMap<>();
    static {
        valuesFP.put(FleetFactory.PatrolType.FAST, 25f);
        valuesFP.put(FleetFactory.PatrolType.COMBAT, 50f);
        valuesFP.put(FleetFactory.PatrolType.HEAVY, 75f);
    }
    public static float getHullFP(String hullId) {
        try {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            if(spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN))return 0f;
            return spec != null ? spec.getFleetPoints() : 0f;
        } catch (Throwable t) {
            return 0f;
        }
    }
    public static float getFPOfAllShipsInFleet(LinkedHashMap<String,Integer>ships){
        float am =0;
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            am+=(getHullFP(entry.getKey())*entry.getValue());
        }
        return am;
    }
    public static float getFleetPointsConsumedByMarket(MarketAPI market){
        int am = 0;
        for (BasePatrolFleet fleet : FactionPatrolsManager.getInstance().getAssignedFleetsForMarket(market)) {
            am+=fleet.geTotalFpTaken();
        }
        return am;
    }
    public static float getFleetPointsGeneratedByMarket(MarketAPI market){
        int small,med,large;
        small = (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0f);
        med = (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0f);
        large = (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0f);
        float percentageMult = market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f);
        float computedSmall,computedLarge,computedMedium;
        computedSmall = valuesFP.get(FleetFactory.PatrolType.FAST)*small;
        computedMedium = valuesFP.get(FleetFactory.PatrolType.COMBAT)*med;
        computedLarge = valuesFP.get(FleetFactory.PatrolType.HEAVY)*large;

        float am = computedSmall+computedMedium+computedLarge;
        am*=percentageMult;
        return am;

    }
    public static float getFleetPointsGeneratedByStarSystem(StarSystemAPI system,FactionAPI faction){
        float am =0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system.getCenter().getContainingLocation())) {
            if(market.getFaction()==null)continue;
            if(faction.getId().equals(market.getFaction().getId())){
                am+=getFleetPointsGeneratedByMarket(market);
            }
        }
        return am;
    }
    public static float getFleetPointsTakenByStarSystem(StarSystemAPI system,FactionAPI faction){
        float am =0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system.getCenter().getContainingLocation())) {
            if(market.getFaction()==null)continue;
            if(faction.getId().equals(market.getFaction().getId())){
                am+=getFleetPointsConsumedByMarket(market);
            }
        }
        return am;
    }
    public static void printStats(){
        for (FactionAPI allFaction : Global.getSector().getAllFactions()) {
            if(allFaction.isShowInIntelTab()){
                log.info("Faction "+allFaction.getDisplayName()+" : generates "+getFleetPointsGeneratedByFaction(allFaction)+" armada points");
            }
        }
    }
    public static int getFleetPointsGeneratedByFaction(FactionAPI faction){
        int am = 0;
        for (MarketAPI factionMarket : Misc.getFactionMarkets(faction)) {
            if(!factionMarket.isInEconomy())continue;
            am+= (int) getFleetPointsGeneratedByMarket(factionMarket);
        }
        return am;
    }

}
