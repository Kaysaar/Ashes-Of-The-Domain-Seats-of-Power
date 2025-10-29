package data.ui.patrolfleet.overview.fleetview.fleetreloc;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.plugins.AoTDSopMisc;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.overview.components.HoldingsButton;
import data.ui.patrolfleet.templates.shiplist.components.SortingState;

import java.awt.*;

public class FleetRelocationDialog extends BasePopUpDialog {
    BasePatrolFleet fleet ;
    FleetRelocMarketList list;
    ButtonAPI buttonName,buttonFPUsed,buttonFPGenerated;
    CustomPanelAPI panelInfo,panelInfoContent;
    FleetLocationData currChosen;
    public FleetRelocationDialog(BasePatrolFleet fleet) {
        super("Relocate Fleet");
        this.fleet = fleet;

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
        list = new FleetRelocMarketList(width,panelToInfluence.getPosition().getHeight()-this.y-170,Global.getSector().getPlayerFaction(),fleet.getTiedTo());
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
            tooltip.addPara("Re-location of this fleet to %s will take around %s",3f,Color.ORANGE,market.getName(), AshMisc.convertDaysToString(Math.round(RouteLocationCalculator.getTravelDays(fleet.getTiedTo().getPrimaryEntity(),market.getPrimaryEntity())))).setAlignment(Alignment.MID);
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
        float days = RouteLocationCalculator.getTravelDays(curr.getPrimaryEntity(),fleet.getTiedTo().getPrimaryEntity());
        fleet.setInTransit(true,days);

        fleet.setTiedTo(curr);
        OverviewPatrolPanel.forceRequestUpdate = true;
    }

    @Override
    public void onExit() {
        list.clearUI();
    }
}
