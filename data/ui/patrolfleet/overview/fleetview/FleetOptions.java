package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.ui.models.PopUpUI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;
import data.plugins.AoTDSopMisc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.overview.marketdata.FleetMarketData;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class FleetOptions extends PopUpUI {
    CustomPanelAPI mainPanel;
    ButtonAPI change,delete,relocate;
    FleetMarketData data;

    public FleetOptions(FleetMarketData fleet) {
        this.data = fleet;
    }
    @Override
    public void createUI(CustomPanelAPI panelAPI) {
        createUIMockup(panelAPI);
        panelAPI.addComponent(mainPanel).inTL(0, 0);
    }

    @Override
    public float createUIMockup(CustomPanelAPI panelAPI) {
        mainPanel = panelAPI.createCustomPanel(panelAPI.getPosition().getWidth(), panelAPI.getPosition().getHeight(), null);
        TooltipMakerAPI tooltip = mainPanel.createUIElement(panelAPI.getPosition().getWidth(),panelAPI.getPosition().getHeight(),true);
        FactionAPI pirates  = Global.getSector().getFaction(Factions.PIRATES);
        tooltip.addSpacer(0f).getPosition().inTL(7,0);
        change = tooltip.addButton("Edit fleet",null, Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE,mainPanel.getPosition().getWidth()-5,30,3f);
        relocate = tooltip.addButton("Relocate fleet",null, Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE,mainPanel.getPosition().getWidth()-5,30,3f);
        delete = tooltip.addButton("Delete fleet",null, pirates.getBaseUIColor(),pirates.getDarkUIColor(), Alignment.MID, CutStyle.NONE,mainPanel.getPosition().getWidth()-5,30,3f);
        BasePatrolFleet fleet = data.lastChecked.getData();
        if(fleet.isDecomisioned()||fleet.isInTransit()){
            change.setEnabled(false);
            relocate.setEnabled(false);
            delete.setEnabled(false);
        }
        if(fleet.isGrounded()){
            relocate.setEnabled(false);
        }
        int count = 0;
        for (MarketAPI marketAPI : AoTDFactionManager.getMarketsUnderPlayer()) {
            for (Industry industry : marketAPI.getIndustries()) {
                if(AoTDMilitaryBase.industriesValidForBase.contains(industry.getSpec().getId())){
                    count++;
                    break;
                }
            }
        }
        if(count<=1){
            relocate.setEnabled(false);
            tooltip.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return 400;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addPara("To relocate fleet you need to have at least two markets with %s",3f, Color.ORANGE, AoTDSopMisc.getAllIndustriesJoined(AoTDMilitaryBase.industriesValidForBase.stream().toList(),"or"));
                }
            },relocate, TooltipMakerAPI.TooltipLocation.BELOW,false);
        }
        tooltip.addSpacer(3f);
        mainPanel.getPosition().setSize(panelAPI.getPosition().getWidth(),tooltip.getHeightSoFar());
        mainPanel.addUIElement(tooltip).inTL(-3,0);
        return tooltip.getHeightSoFar();


    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if(isButtonChecked(change)){
            data.showEdit = true;
            forceDismiss();
        }
        if(isButtonChecked(relocate)){
            data.showReloc = true;
            forceDismiss();
        }
        if(isButtonChecked(delete)){
            data.showDelete = true;
            forceDismiss();
        }

    }
    public boolean isButtonChecked(ButtonAPI button){
        return button!=null&& button.isChecked();
    }

    @Override
    public void onExit() {
        super.onExit();
    }
}
