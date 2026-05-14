package data.ui.patrolfleet.overview.fleetview.fleetreloc;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.aotd_entities.BiFrostGateEntity;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;

import data.kaysaar.aotd.vok.campaign.econ.megastructures.impl.scripts.BifrostMegastructure;
import data.kaysaar.aotd.vok.campaign.econ.megastructures.impl.scripts.BifrostMegastructureManager;
import data.plugins.AoTDSopMisc;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.utilis.PatrolFleetFactory;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.templates.shiplist.components.SortingState;

import java.awt.*;

public class FleetRelocationDialog extends BasePopUpDialog {
    BasePatrolFleet fleetData;
    FleetRelocationSelectorPanel relocationPanel;
    public FleetRelocationDialog(BasePatrolFleet fleetData) {
        super("Relocate Fleet");
        this.fleetData = fleetData;

    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);
        tooltip.addPara("Choose market to which relocate fleet", 2f)
                .setAlignment(Alignment.MID);

        float panelHeight = panelToInfluence.getPosition().getHeight() - this.y-20;

        relocationPanel = new FleetRelocationSelectorPanel(
                width,
                panelHeight,
                fleetData.getTiedTo()
        );

        tooltip.addCustom(relocationPanel.getMainPanel(), 5f);
        tooltip.setHeightSoFar(0f);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (relocationPanel != null) {
            relocationPanel.advance(amount);
        }
    }

    @Override
    public void applyConfirmScript() {
        if (relocationPanel == null || !relocationPanel.hasSelection()) {
            return;
        }

        MarketAPI curr = relocationPanel.getSelectedMarket();

        float days = RouteLocationCalculator.getTravelDays(
                curr.getPrimaryEntity(),
                fleetData.getTiedTo().getPrimaryEntity()
        );
        if(!AoTDMilitaryBase.isPatroling(fleetData.getId(), fleetData.getTiedTo())){
            FleetParamsV3 params = new FleetParamsV3(
                    fleetData.getTiedTo(),
                    null,
                    fleetData.getTiedTo().getFactionId(),
                    Misc.getShipQuality(fleetData.getTiedTo(), fleetData.getTiedTo().getFactionId()),
                    null,
                    fleetData.getFPTaken(), // combatPts
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f // qualityMod
            );
            CampaignFleetAPI fleetToSpawn = PatrolFleetFactory.createFleetFromAssigned(fleetData, params, fleetData.getTiedTo(), params.random);
            fleetData.getTiedTo().getContainingLocation().addEntity(fleetToSpawn);
            fleetToSpawn.setFacing((float) Math.random() * 360f);
            // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
            fleetToSpawn.setLocation(fleetData.getTiedTo().getPrimaryEntity().getLocation().x, fleetData.getTiedTo().getPrimaryEntity().getLocation().y);
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
            fleetToSpawn.addAssignment(FleetAssignment.ORBIT_PASSIVE, fleetData.getTiedTo().getPrimaryEntity(),1f,"Preparing for re-location");
            if (Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
                /// TOOD - MEgA
                BifrostMegastructure mega = BifrostMegastructureManager.getInstance().getMegastructure();
                if(mega!=null&&mega.areStarSystemsConnected(fleetData.getTiedTo().getStarSystem(),curr.getStarSystem())){
                    SectorEntityToken token = mega.getSectionEntityInStarSystem(fleetData.getTiedTo().getStarSystem());
                    SectorEntityToken travel = mega.getSectionEntityInStarSystem(curr.getStarSystem());

                    fleetToSpawn.addAssignment(FleetAssignment.GO_TO_LOCATION, token, 15, "Taking course towards Bifrost Gate",new Script() {
                        @Override
                        public void run() {
                            float distLY = Misc.getDistanceLY(token, travel);
                            if (token.getCustomPlugin() instanceof BiFrostGateEntity) {
                                BiFrostGateEntity plugin = (BiFrostGateEntity) token.getCustomPlugin();
                                plugin.showBeingUsed(distLY);
                            }
                            if (travel.getCustomPlugin() instanceof BiFrostGateEntity) {
                                BiFrostGateEntity plugin = (BiFrostGateEntity) travel.getCustomPlugin();
                                plugin.showBeingUsed(distLY);
                            }
                            JumpPointAPI.JumpDestination dest = new JumpPointAPI.JumpDestination(travel, null);
                            Global.getSector().doHyperspaceTransition(fleetToSpawn, token, dest, 2f);

                        }
                    });


                }

            }
            fleetToSpawn.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, curr.getPrimaryEntity(),10000f,"Relocating to new market");


        }
        fleetData.setInTransit(true,days);

        fleetData.setTiedTo(curr);
        OverviewPatrolPanel.forceRequestUpdate = true;
    }

    @Override
    public void onExit() {
        if (relocationPanel != null) {
            relocationPanel.clearUI();
        }
    }
}
