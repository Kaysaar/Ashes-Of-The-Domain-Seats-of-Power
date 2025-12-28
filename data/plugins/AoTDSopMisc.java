package data.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.RemnantHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction;
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;
import data.intel.HelldiversRaidIntel;
import data.misc.ProductionUtil;
import data.misc.ReflectionUtilis;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.fs.starfarer.api.util.Misc.isMilitary;

public class AoTDSopMisc {
    public static boolean startAttack(MarketAPI source,MarketAPI target, StarSystemAPI system, Random random, FleetGroupIntel.FGIEventListener listener,boolean isLuddite) {
        //System.out.println("RANDOM: " + random.nextLong());

        GenericRaidFGI.GenericRaidParams params = new GenericRaidFGI.GenericRaidParams(new Random(random.nextLong()), false);
        params.raidParams.raidActionText = "Deploying democracy at high velocity.";
        params.makeFleetsHostile = false;
        params.remnant = false;

        params.factionId = Factions.PLAYER;

        target.getMemoryWithoutUpdate().set("$aotd_chosen_for_raid",true);

        params.source = source;

        params.prepDays = 0f;
        params.payloadDays = 27f + 7f * random.nextFloat();

        params.raidParams.where = system;
        params.raidParams.type = FGRaidAction.FGRaidType.SEQUENTIAL;
        params.raidParams.tryToCaptureObjectives = false;
        params.raidParams.allowedTargets.add(target);
        params.raidParams.inSystemActionText = "Deploying democracy at high velocity.";
        params.raidParams.doNotGetSidetracked = true;
        params.raidParams.targetTravelText = "Deploying democracy at high velocity.";
        params.forcesNoun = "helldivers forces";

        params.style = FleetCreatorMission.FleetStyle.STANDARD;
        params.repImpact = HubMissionWithTriggers.ComplicationRepImpact.LOW;


        // standard Askonia fleet size multiplier with no shortages/issues is a bit over 230%
        float fleetSizeMult = 2f;
        params.fleetSizes.add(15);
        params.fleetSizes.add(10);
        params.fleetSizes.add(10);
        params.fleetSizes.add(5);
        params.fleetSizes.add(5);
        params.noun ="offensive of freedom";


        HelldiversRaidIntel raid = new HelldiversRaidIntel(params,isLuddite);

        raid.setListener(listener);
        Global.getSector().getIntelManager().addIntel(raid);
        return true;
    }
    public static String getAllIndustriesJoined(List<String> specs,String joiner){
        ArrayList<String>names =new ArrayList<>();
        for (String spec : specs) {
            names.add(Global.getSettings().getIndustrySpec(spec).getName());
        }
        return Misc.getJoined(joiner,names);
    }
    public static ButtonAPI tryToGetButtonProd(String name) {
        ButtonAPI button = null;
        try {
            for (UIComponentAPI componentAPI : ReflectionUtilis.getChildrenCopy((UIPanelAPI) ProductionUtil.getCurrentTab())) {
                if (componentAPI instanceof ButtonAPI) {
                    if (((ButtonAPI) componentAPI).getText().toLowerCase().contains(name)) {
                        button = (ButtonAPI) componentAPI;
                        break;
                    }
                }
            }
            return button;
        } catch (Exception e) {

        }
        return button;

    }
    public static FactionAPI getClaimingFaction(SectorEntityToken planet) {
        if (planet.getStarSystem() != null) {
            String claimedBy = planet.getStarSystem().getMemoryWithoutUpdate().getString(MemFlags.CLAIMING_FACTION);
            if (claimedBy != null) {
                return Global.getSector().getFaction(claimedBy);
            }
        }

        int max = 0;
        MarketAPI result = null;
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarkets(planet.getContainingLocation());
        for (MarketAPI curr : markets) {
            if (curr.isHidden()) continue;
            int score = curr.getSize();
            for (MarketAPI other : markets) {
                if (other != curr && other.getFaction() == curr.getFaction()) score++;
            }
            if (isMilitary(curr)) score += 10;
            if (score > max) {
                max = score;
                result = curr;
            }
        }
        if (result == null) return null;

        return result.getFaction();
    }
}
