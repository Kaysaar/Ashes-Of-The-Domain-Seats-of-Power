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
import data.kaysaar.aotd.vok.campaign.econ.globalproduction.impl.bifrost.BifrostMega;
import data.kaysaar.aotd.vok.campaign.econ.globalproduction.models.GPManager;
import data.plugins.AoTDSopMisc;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.utilis.PatrolFleetFactory;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.templates.shiplist.components.SortingState;

import java.awt.*;

public class FleetRelocationDialog extends BasePopUpDialog {
    BasePatrolFleet fleetData;
    FleetRelocMarketList list;
    ButtonAPI buttonName,buttonFPUsed,buttonFPGenerated;
    CustomPanelAPI panelInfo,panelInfoContent;
    FleetLocationData currChosen;
    public FleetRelocationDialog(BasePatrolFleet fleetData) {
        super("Relocate Fleet");
        this.fleetData = fleetData;

    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);
        tooltip.addPara("Choose market to which relocate fleet",2f).setAlignment(Alignment.MID);
        float usableWidth = width-13;
        float section =usableWidth/3;
        Color base,bg,bright;
        base = Misc.getBasePlayerColor();
        bg= Misc.getDarkPlayerColor();
        bright = Misc.getBrightPlayerColor();
        buttonName = tooltip.addAreaCheckbox("Name", SortingState.ASCENDING, base, bg, bright, section, 20, 0f);
        buttonFPUsed = tooltip.addAreaCheckbox("FP allocated", SortingState.NON_INITIALIZED, base, bg, bright, section, 20, 0f);
        buttonFPGenerated = tooltip.addAreaCheckbox("Distance (LY)", SortingState.NON_INITIALIZED, base, bg, bright, section, 20, 0f);
        buttonFPUsed.getPosition().inTL(section+1, 30);
        buttonFPGenerated.getPosition().inTL((section+1)*2, 30);
        buttonName.getPosition().inTL(0, 30);
        buttonName.setClickable(false);
        buttonFPGenerated.setClickable(false);
        buttonFPUsed.setClickable(false);
        list = new FleetRelocMarketList(width,panelToInfluence.getPosition().getHeight()-this.y-170,Global.getSector().getPlayerFaction(), fleetData.getTiedTo());
        list.createUI();
        tooltip.addCustom(list.getMainPanel(),5f).getPosition().inTL(0,55);
        panelInfo = Global.getSettings().createCustom(width-10,100,null);
        tooltip.addCustom(panelInfo,5f);
        tooltip.setHeightSoFar(0f);
        updateInfo();
    }
    public void updateInfo(){
        if(panelInfoContent!=null){
            panelInfo.removeComponent(panelInfoContent);
        }
        panelInfoContent = Global.getSettings().createCustom(panelInfo.getPosition().getWidth(),panelInfo.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = panelInfoContent.createUIElement(panelInfoContent.getPosition().getWidth(),panelInfo.getPosition().getHeight(),false);
        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);

        if(!FleetRelocMarketList.doesPlayerFactionMeetCriteriaForInterstellarReloc()){
            tooltip.addPara("For relocation between star systems %s must be under control of faction",3f,Color.ORANGE, AoTDSopMisc.getAllIndustriesJoined(FleetRelocMarketList.industriesAllowingInterstellarTransition.stream().toList(),"or")).setAlignment(Alignment.MID);
        }
        if(currChosen!=null){
            MarketAPI market = (MarketAPI) currChosen.buttonData;
            if(Global.getSettings().getModManager().isModEnabled("aotd_vok")){
                BifrostMega mega = (BifrostMega) GPManager.getInstance().getMegastructure("aotd_bifrost");
                if(mega!=null&&mega.areStarSystemsConnected(fleetData.getTiedTo().getStarSystem(),market.getStarSystem())){
                    tooltip.addPara("Re-location of this fleet will take few days, due to working %s connecting both star systems!",3f,Color.ORANGE,"Bifrost Network").setAlignment(Alignment.MID);

                }
                else{
                    tooltip.addPara("Re-location of this fleet to %s will take around %s",3f,Color.ORANGE,market.getName(), AshMisc.convertDaysToString(Math.round(RouteLocationCalculator.getTravelDays(fleetData.getTiedTo().getPrimaryEntity(),market.getPrimaryEntity())))).setAlignment(Alignment.MID);

                }
            }
            else{
                tooltip.addPara("Re-location of this fleet to %s will take around %s",3f,Color.ORANGE,market.getName(), AshMisc.convertDaysToString(Math.round(RouteLocationCalculator.getTravelDays(fleetData.getTiedTo().getPrimaryEntity(),market.getPrimaryEntity())))).setAlignment(Alignment.MID);

            }
        }

        panelInfoContent.addUIElement(tooltip).inTL(0,0);
        panelInfo.addComponent(panelInfoContent);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if(list!=null){
            for (FleetLocationData datum : list.data) {
                if(datum.mainButton.isChecked()){
                    datum.mainButton.setChecked(false);
                    currChosen = datum;
                    updateInfo();
                    break;
                }
            }
            for (FleetLocationData datum : list.data) {
                if(datum.equals(currChosen)){
                    datum.mainButton.highlight();
                }
                else{
                    datum.mainButton.unhighlight();
                }
            }
        }


    }

    @Override
    public void applyConfirmScript() {
        MarketAPI curr = (MarketAPI) currChosen.buttonData;
        float days = RouteLocationCalculator.getTravelDays(curr.getPrimaryEntity(), fleetData.getTiedTo().getPrimaryEntity());
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
                BifrostMega mega = (BifrostMega) GPManager.getInstance().getMegastructure("aotd_bifrost");
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
        list.clearUI();
    }
}
