package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.ui.models.PopUpUI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.overview.marketdata.FleetMarketData;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.util.ArrayList;

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
        if(data.lastChecked.getData().isDecomisioned()){
            change.setEnabled(false);
            relocate.setEnabled(false);
            delete.setEnabled(false);
        }
        if(Misc.getFactionMarkets(Factions.PLAYER).size()<=1){
            relocate.setEnabled(false);
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
