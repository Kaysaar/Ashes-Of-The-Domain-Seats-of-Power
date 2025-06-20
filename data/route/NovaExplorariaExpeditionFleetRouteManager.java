package data.route;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.listeners.CoreDiscoverEntityPlugin;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.impl.campaign.CoreScript;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.util.Misc;
import data.industry.NovaExploraria;
import data.intel.NovaExplorariaExpeditionIntel;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class NovaExplorariaExpeditionFleetRouteManager extends RouteFleetAssignmentAI implements FleetActionTextProvider, FleetEventListener {
    public StarSystemAPI target;
    public NovaExplorariaExpeditionIntel intel;

    public NovaExplorariaExpeditionFleetRouteManager(CampaignFleetAPI fleet, RouteManager.RouteData route, StarSystemAPI target, NovaExplorariaExpeditionIntel intel) {
        super(fleet, route);
        this.target = target;
        this.intel = intel;
        giveInitialAssignments();
    }
    public  static String getSurveyClassForItem(PlanetAPI planet) {
        SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
        String type = plugin.getSurveyDataType(planet);
        if (type != null) {
            CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(type);
            return spec.getId();
        }
        return "Class N";
    }
    @Override
    protected void giveInitialAssignments() {
        if (target == null) return;

        RouteManager.RouteSegment current = route.getCurrent();
        SectorEntityToken source = route.getMarket().getPrimaryEntity();
        fleet.clearAssignments();
        fleet.setNoAutoDespawn(true);
        fleet.addEventListener(this);

        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source, MathUtils.getRandomNumberInRange(2, 4), "Preparing for Expedition");

        SectorEntityToken jumpPointInSystem = target.getJumpPoints().get(0);
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, jumpPointInSystem, RouteLocationCalculator.getTravelDays(source, jumpPointInSystem) * 1.5f, "Going to "+target.getName()+" to explore it");

        SectorEntityToken lastSavedFrom = jumpPointInSystem;

        for (PlanetAPI object : target.getPlanets()) {
            if (object.isStar() || object.isBlackHole()) continue;

            String travelText = "Navigating to " + object.getName();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, object, RouteLocationCalculator.getTravelDays(lastSavedFrom, object) * 2.5f, travelText);

            String orbitText = getPlanetSurveyText(object);
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, object, MathUtils.getRandomNumberInRange(1, 3), orbitText, new Script() {
                @Override
                public void run() {
                    intel.getSurveyMap().put(object, getSurveyClassForItem(object));
                }
            });

            lastSavedFrom = object;
        }

        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source, 10000, "Returning from Expedition", new Script() {
            @Override
            public void run() {
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source, 20000f, "Standing Down");
            }
        });

        fleet.getAI().setActionTextProvider(this);
    }

    private String getPlanetSurveyText(PlanetAPI planet) {
        if (planet.getSpec().isGasGiant()) {
            return "Studying Gas Layers around " + planet.getName();
        }
        if (planet.hasCondition("ruins_scattered") || planet.hasCondition("ruins_vast") || planet.hasCondition("ruins_widespread") || planet.hasCondition("ruins_extensive")) {
            return "Scanning Ancient Ruins on " + planet.getName();
        }
        return "Conducting Survey of " + planet.getName();
    }


    @Override
    public void advance(float amount) {
        super.advance(amount);
    }

    @Override
    public String getActionText(CampaignFleetAPI fleet) {
        return null;
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (reason.equals(CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE)){
            intel.setFinished(true);
            intel.setSuccessful(false);
            intel.sendUpdateIfPlayerHasIntel(null,false);
        }
        if(reason.equals(CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION)){
            intel.setSuccessful(true);
            CoreScript.markSystemAsEntered(target,false);
            MarketAPI gatheringPoint = Global.getSector().getPlayerFaction().getProduction().getGatheringPoint();;
            intel.getSurveyMap().values().forEach(x->gatheringPoint.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity(x,1));
            intel.getSurveyMap().keySet().forEach(ListenerUtil::reportPlayerSurveyedPlanet);

            Misc.setAllPlanetsKnown(target);
            Misc.setAllPlanetsSurveyed(target,false);
            GenericPluginManagerAPI plugins = Global.getSector().getGenericPlugins();
            CoreDiscoverEntityPlugin plugin = (CoreDiscoverEntityPlugin) plugins.getPluginsOfClass(CoreDiscoverEntityPlugin.class).stream().findFirst().orElse(null);
            List<SectorEntityToken> entitiesToDiscover = target.getAllEntities().stream().filter(x->x.isDiscoverable()&& !(x instanceof CampaignFleetAPI)).toList();
            for (SectorEntityToken allEntity : entitiesToDiscover) {
                if(allEntity instanceof CampaignFleetAPI)continue;
                if(allEntity.isDiscoverable()){
                    assert plugin != null;
                    plugin.discoverEntity(allEntity);
                    intel.getOthers().add(allEntity.getName());

                }
            }
            intel.setSuccessful(true);
            intel.setFinished(true);
            intel.sendUpdateIfPlayerHasIntel(null,false);

        }
        NovaExploraria.finishExpedition();
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }
}
