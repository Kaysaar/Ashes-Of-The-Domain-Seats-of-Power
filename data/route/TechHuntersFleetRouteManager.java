package data.route;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem;
import com.fs.starfarer.api.campaign.impl.items.ModSpecItemPlugin;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.intel.TechHunterFleetIntel;
import data.scripts.managers.AoTDFactionManager;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TechHuntersFleetRouteManager extends RouteFleetAssignmentAI implements FleetActionTextProvider, FleetEventListener {
    public boolean toReturn = false;
    CargoAPI cargo;
    int monthsBeingOnExpedition =0;
    int expectedMonths = 1;
    public boolean doomed= false;
    SectorEntityToken trueSource;
    public void setToReturn(boolean toReturn) {
        this.toReturn = toReturn;
    }
    public boolean isSetToReturn() {
        return toReturn;
    }



    float daysSince = 0;
    TechHunterFleetIntel intel;
    @Override
    public void advance(float amount) {
        super.advance(amount);
       performCalcus(amount);
    }
    public void performCalcus(float amount){
        daysSince+=Global.getSector().getClock().convertToDays(amount);
        if(daysSince>=30){
            daysSince = 0;
            performDiceRollForItems();
        }
    }

    public TechHuntersFleetRouteManager(CampaignFleetAPI fleet, RouteManager.RouteData route, int expectedMonths) {
        super(fleet, route);
        this.expectedMonths = expectedMonths;

        cargo = Global.getFactory().createCargo(false);
    }

    @Override
    protected void giveInitialAssignments() {
        RouteManager.RouteSegment current = route.getCurrent();
        intel = new TechHunterFleetIntel(fleet);
        Global.getSector().getIntelManager().addIntel(intel);
        SectorEntityToken source = route.getMarket().getPrimaryEntity();
        trueSource = source;
        fleet.clearAssignments();
        fleet.setNoAutoDespawn(true);
        fleet.addEventListener(this);

        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source, MathUtils.getRandomNumberInRange(2, 4), "Preparing for Tech-Mining Operations");
        pickWorldForTechMining(true);
    }

    @Override
    public String getActionText(CampaignFleetAPI fleet) {
        return "";
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
            if(reason .equals(CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE)){
                intel.setFinished(true);
                intel.setSuccessful(false);
                cargo.clear();
                intel.endAfterDelay(5f);
                intel.sendUpdateIfPlayerHasIntel(null,false);


            }
            if(reason.equals(CampaignEventListener.FleetDespawnReason.OTHER)&&param instanceof String && param.equals("rolled_wrong")){
                intel.setFinished(true);
                intel.setSuccessful(false);
                cargo.clear();
                intel.endAfterDelay(5f);
                intel.sendUpdateIfPlayerHasIntel(null,false);


            }
            if(reason.equals(CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION)){
                intel.setFinished(true);
                intel.setSuccessful(true);
                if(!AoTDFactionManager.getMarketsUnderPlayer().isEmpty()){
                    Global.getSector().getPlayerFaction().getProduction().getGatheringPoint().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addAll(cargo);

                }
                intel.getCargo().addAll(cargo);
                intel.endAfterDelay(5f);
                cargo.clear();
                intel.sendUpdateIfPlayerHasIntel(null,false);

            }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }
    public static TechHuntersFleetRouteManager getInstance(CampaignFleetAPI fleet) {
        return (TechHuntersFleetRouteManager) fleet.getScripts().stream().filter(x->x instanceof TechHuntersFleetRouteManager).findAny().orElse(null);
    }
    public void pickWorldForTechMining(boolean ignorePenalty) {
        float remnantChance = getPercentageForTechHunterFailure(monthsBeingOnExpedition);// 2% per month, capped at 20%
        SectorEntityToken target = null;

        List<StarSystemAPI> systems = new ArrayList<>(Global.getSector().getStarSystems());
        Collections.shuffle(systems);

        // First, try picking a ruins world normally (unless we roll for Remnants)
        boolean tryRemnant =  Misc.random.nextFloat() < remnantChance;
        for (StarSystemAPI system : systems) {
            if (fleet.getStarSystem() != null && fleet.getStarSystem().equals(system)) continue;
            // If Remnant roll succeeds, look for a Remnant-themed system
            if (tryRemnant && system.hasTag(Tags.THEME_REMNANT_MAIN)&&!ignorePenalty) {
                for (PlanetAPI planet : system.getPlanets()) {
                    if(planet.getMarket()!=null&&Misc.hasRuins(planet.getMarket())){
                        target = planet.getMarket().getPrimaryEntity();
                        break;
                    }
                }
            }

            // Otherwise, find ruins world as usual
            if (!tryRemnant && system.hasTag(Tags.THEME_RUINS_MAIN)) {
                if(Misc.getMarketsInLocation(system).isEmpty()){
                    for (PlanetAPI planet : system.getPlanets()) {
                        if(planet.getMarket()!=null&&Misc.hasRuins(planet.getMarket())){
                            target = planet.getMarket().getPrimaryEntity();
                            break;
                        }
                    }
                }
                
            }

            if (target != null) break;
        }

        if (target == null) {
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, trueSource, 10000,
                    "Returning to " + trueSource.getStarSystem().getName());
        } else {
            float travelDays = RouteLocationCalculator.getTravelDays(fleet, target) * 2.5f;
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, travelDays,
                    "Traveling to " + target.getStarSystem().getName());

            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, target,
                    MathUtils.getRandomNumberInRange(12f, 20f), "Scavenging Ruins", new Script() {
                        @Override
                        public void run() {
                            fleet.clearAssignments();
                            pickWorldForTechMining(false); // recursive loop
                        }
                    });
        }
    }


    int scale(int base, int cap) {
        return Math.min(base*monthsBeingOnExpedition,cap);
    }
    public void performDiceRollForItems() {
        // Max cap for chances per group to avoid extreme results
        if(monthsBeingOnExpedition>=expectedMonths){
            return;
        }
        monthsBeingOnExpedition++;

        List<SalvageEntityGenDataSpec.DropData> dropRandom = new ArrayList<>();

        // Helper to scale chances and clamp


        SalvageEntityGenDataSpec.DropData d;

        d = new SalvageEntityGenDataSpec.DropData();
        d.chances = scale(1,1); //
        d.group = "blueprints_low";
        dropRandom.add(d);

        d = new SalvageEntityGenDataSpec.DropData();
        d.chances = scale(1,2);
        d.group = "rare_tech_low";
        dropRandom.add(d);

        d = new SalvageEntityGenDataSpec.DropData();
        d.chances = scale(1,12);
        d.group = "ai_cores3";
        dropRandom.add(d);

        d = new SalvageEntityGenDataSpec.DropData();
        d.chances = scale(1,1);
        d.group = "any_hullmod_low";
        dropRandom.add(d);

        d = new SalvageEntityGenDataSpec.DropData();
        d.chances = scale(1,3);
        d.group = "weapons2";
        dropRandom.add(d);

        // You can keep dropValue empty unless you're controlling exact value output
        List<SalvageEntityGenDataSpec.DropData> dropValue = new ArrayList<>();

        // Run the drop
        Random random = new Random();
        CargoAPI found = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, dropValue, dropRandom);

        // Remove duplicates known to the player
        FactionAPI pf = Global.getSector().getPlayerFaction();
        OUTER: for (CargoStackAPI stack : found.getStacksCopy()) {
            if (stack.getPlugin() instanceof BlueprintProviderItem) {
                BlueprintProviderItem bp = (BlueprintProviderItem) stack.getPlugin();

                List<String> list = bp.getProvidedShips();
                if (list != null) for (String id : list) if (!pf.knowsShip(id)) continue OUTER;

                list = bp.getProvidedWeapons();
                if (list != null) for (String id : list) if (!pf.knowsWeapon(id)) continue OUTER;

                list = bp.getProvidedFighters();
                if (list != null) for (String id : list) if (!pf.knowsFighter(id)) continue OUTER;

                list = bp.getProvidedIndustries();
                if (list != null) for (String id : list) if (!pf.knowsIndustry(id)) continue OUTER;

                found.removeStack(stack);
            } else if (stack.getPlugin() instanceof ModSpecItemPlugin) {
                ModSpecItemPlugin mod = (ModSpecItemPlugin) stack.getPlugin();
                if (!pf.knowsHullMod(mod.getModId())) continue OUTER;
                found.removeStack(stack);
            }
        }

        // Add to fleet’s cargo storage
        cargo.addAll(found);
        if(monthsBeingOnExpedition>=expectedMonths){
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, trueSource,10000, "Returning to "+trueSource.getStarSystem().getName());

        }
    }

    public static float getPercentageForTechHunterFailure(int months){
        return  Math.min(0.2f, months * 0.02f);
    }
}
