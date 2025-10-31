package data.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.fleets.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.ai.PatrolAssigmentAIV5;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.models.AoTDPatrolFleetData;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.utilis.PatrolFleetFactory;
import org.lazywizard.lazylib.MathUtils;

import java.util.LinkedHashSet;
import java.util.List;

public class AoTDMilitaryBase extends MilitaryBase {
    public static LinkedHashSet<String>industriesValidForBase = new LinkedHashSet<>();
    public static  int getVanillaCount(MarketAPI market,FleetFactory.PatrolType... types) {
        int count = 0;
        for (RouteManager.RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId(market))) {
            if (data.getCustom() instanceof PatrolFleetData) {
                PatrolFleetData custom = (PatrolFleetData) data.getCustom();
                for (FleetFactory.PatrolType type : types) {
                    if (type == custom.type) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public void advance(float amount) {
        if (market.getFaction() == null || !market.getFaction().isPlayerFaction()) {
            super.advance(amount);
            return;
        }
        boolean disrupted = isDisrupted();
        if (!disrupted && wasDisrupted) {
            disruptionFinished();
        }
        wasDisrupted = disrupted;

//		if (disrupted) {
//			//if (DebugFlags.COLONY_DEBUG) {
//				String key = getDisruptedKey();
//				market.getMemoryWithoutUpdate().unset(key);
//			//}
//		}

        if (building && !disrupted) {
            float days = Global.getSector().getClock().convertToDays(amount);
            //DebugFlags.COLONY_DEBUG = true;
            if (DebugFlags.COLONY_DEBUG) {
                days *= 100f;
            }
            buildProgress += days;

            if (buildProgress >= buildTime) {
                finishBuildingOrUpgrading();
            }
        }

        if (Global.getSector().getEconomy().isSimMode()) return;
        List<BasePatrolFleet> fleets = AoTDFactionPatrolsManager.getInstance().getAssignedFleetsForMarket(market);
        int num = Math.toIntExact(fleets.stream().filter(x -> !isPatroling(x.getId(), market)).count());
        if(num==0) return;
        if (!isFunctional()) return;

        float days = Global.getSector().getClock().convertToDays(amount);

//		float stability = market.getPrevStability();
//		float spawnRate = 1f + (stability - 5) * 0.2f;
//		if (spawnRate < 0.5f) spawnRate = 0.5f;

        float spawnRate = 1f;
        float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
        spawnRate+=(0.2f*num)+rateMult;

        float extraTime = 0f;
        if (returningPatrolValue > 0) {
            // apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
            float interval = tracker.getIntervalDuration();
            extraTime = interval * days;
            returningPatrolValue -= days;
            if (returningPatrolValue < 0) returningPatrolValue = 0;
        }
        tracker.advance(days * spawnRate + extraTime);
        if (tracker.intervalElapsed()) {
//			if (market.isPlayerOwned()) {
//				System.out.println("ewfwefew");
//			}
//			if (market.getName().equals("Jangala")) {
//				System.out.println("wefwefe");
//			}
            String sid = getRouteSourceId();


            WeightedRandomPicker<BasePatrolFleet> picker = new WeightedRandomPicker<BasePatrolFleet>();

            for (BasePatrolFleet fleet :fleets ) {
                if (fleet.isValidToSpawn()) picker.add(fleet, fleet.getFPTaken());
            }


            if (picker.isEmpty()) return;

            BasePatrolFleet type = picker.pick(Misc.random);
            AoTDPatrolFleetData custom = new AoTDPatrolFleetData();
            custom.setId(type.getId());

            RouteManager.OptionalFleetData extra = new RouteManager.OptionalFleetData(market);

            RouteManager.RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
            extra.strength = (float) type.getFPTaken();
            extra.strength = Misc.getAdjustedStrength(extra.strength, market);


            float patrolDays = 35f + (float) Math.random() * 10f;
            route.addSegment(new RouteManager.RouteSegment(patrolDays, market.getPrimaryEntity()));
        }

    }
@Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if(market.getFaction()==null||!market.getFaction().isPlayerFaction()){
            super.reportFleetDespawnedToListener(fleet, reason, param);
            return;
        }
        RouteManager.RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);

        AoTDPatrolFleetData data = (AoTDPatrolFleetData) route.getCustom();
        BasePatrolFleet fleetData = AoTDFactionPatrolsManager.getInstance().getFleet(data.getId());
        if (fleetData != null) {

            if (fleetData.isDecomisioned()) {
                fleetData.getShipsForReplacementWhenInPrep().clear();
                float days = fleetData.getFPTaken()/AoTDFactionPatrolsManager.getInstance().getDaysPerFP().getModifiedValue();
                fleetData.startProcessOfDecom(days);
                return;
            }
            fleetData.performReplacement();
            if(fleetData.isInTransit()){
              Global.getSector().addScript(new DelayedActionScript(MathUtils.getRandomNumberInRange(2,4)) {
                  @Override
                  public void doAction() {
                      FleetParamsV3 params = new FleetParamsV3(
                              market,
                              null,
                              market.getFactionId(),
                              route.getQualityOverride(),
                              null,
                              fleetData.getFPTaken(), // combatPts
                              0f, // freighterPts
                              0f, // tankerPts
                              0f, // transportPts
                              0f, // linerPts
                              0f, // utilityPts
                              0f // qualityMod
                      );
                      CampaignFleetAPI fleetToSpawn = PatrolFleetFactory.createFleetFromAssigned(fleetData, params, market, params.random);
                      market.getContainingLocation().addEntity(fleetToSpawn);
                      fleetToSpawn.setFacing((float) Math.random() * 360f);
                      // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
                      fleetToSpawn.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
                      fleetToSpawn.addEventListener(new FleetEventListener() {
                          @Override
                          public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
                              if(reason.equals(CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION)){
                                  fleetData.setInTransit(false);
                              }
                              else{
                                  fleetData.forceTransitDays();
                              }
                          }

                          @Override
                          public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

                          }
                      });
                      fleetToSpawn.setTransponderOn(true);
                      fleetToSpawn.addAbility(Abilities.SUSTAINED_BURN);
                      fleetToSpawn.getAbility(Abilities.SUSTAINED_BURN).activate();
                      fleetToSpawn.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
                      fleetToSpawn.addAssignment(FleetAssignment.ORBIT_PASSIVE,market.getPrimaryEntity(),3f,"Preparing for re-location");
                      fleetToSpawn.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,fleetData.getTiedTo().getPrimaryEntity(),10000f,"Relocating to new market");
                  }
              });
              return;
            }

            if (reason.equals(CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION)) {
                returningPatrolValue += (float) fleet.getFleetPoints() / fleetData.getFPTaken();
            }

        }


    }
    @Override
    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route) {
        if (market.getFaction() != null && !market.getFaction().isPlayerFaction()) return super.spawnFleet(route);
        AoTDPatrolFleetData custom = (AoTDPatrolFleetData) route.getCustom();
        CampaignFleetAPI fleet = createPatrol(custom.getId(), route);
        if (fleet == null || fleet.isEmpty()) return null;

        fleet.addEventListener(this);

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        fleet.addScript(new PatrolAssigmentAIV5(fleet, route));
        fleet.setTransponderOn(true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);

        //market.getContainingLocation().addEntity(fleet);
        //fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
        return fleet;

    }
    public static String getRouteSourceId(MarketAPI market) {
        return market.getId() + "_" + "military";
    }


    public static boolean isPatroling(String id, MarketAPI market) {
        if(market==null)return false;
        for (RouteManager.RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId(market))) {
            if (data.getCustom() instanceof AoTDPatrolFleetData daten) {
                if (daten.getId().equals(id)) return true;
            }
        }
        return false;
    }



    public CampaignFleetAPI createPatrol(String id, RouteManager.RouteData route) {
        BasePatrolFleet fleets = AoTDFactionPatrolsManager.getInstance().getFleet(id);
        if(fleets==null){

        }
        fleets.performReplacement();
        FleetParamsV3 params = new FleetParamsV3(
                market,
                null,
                market.getFactionId(),
                route == null ? null : route.getQualityOverride(),
                null,
                fleets.getFPTaken(), // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        params.random = route.getRandom();
        CampaignFleetAPI fleet = PatrolFleetFactory.createFleetFromAssigned(fleets, params, market, params.random);
        if (fleet == null || fleet.isEmpty()) return null;

        if (!fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PATROLS_HAVE_NO_PATROL_MEMORY_KEY)) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
            if (fleets.getFPTaken() >= 50) {
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
            }
        } else if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);

            // hidden pather and pirate bases
            // make them raid so there's some consequence to just having a colony in a system with one of those
            if (market != null && market.isHidden()) {
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);
            }
        }

        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;
        float fp = fleets.getFPTaken();
        if (fp >= 30) {
            rankId = Ranks.SPACE_LIEUTENANT;
        }
        if (fp >= 50) {
            rankId = Ranks.SPACE_COMMANDER;

        }
        if (fp >= 100) {
            rankId = Ranks.SPACE_CAPTAIN;
        }
        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);

        return fleet;
    }

}
