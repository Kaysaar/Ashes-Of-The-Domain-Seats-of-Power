package data.ui.patrolfleet.overview;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.DataAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.ui.patrolfleet.overview.marketdata.FleetMarketData;
import data.ui.patrolfleet.overview.stats.OverviewStatPanel;
import data.ui.patrolfleet.templates.TemplateShowcaseList;
import data.ui.patrolfleet.templates.filter.TemplateFilterPanel;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.util.List;

public class OverviewPatrolPanel  implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel, componentPanel;
    OverviewStatPanel stats;
    FleetMarketData data;
    MarketAPI lastSavedMarket = null;
    public static float OverviewStatPanelWidth = 430f;
    public static boolean forceRequestUpdate = false;
    public static boolean forceRequestUpdateListOnly = false;


    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public OverviewPatrolPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();

    }

    @Override
    public void createUI() {
        if(componentPanel!=null){
            mainPanel.removeComponent(componentPanel);
        }
        AoTDFactionPatrolsManager.getInstance().advanceAfterFleets(0f);
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), null);
        if(stats!=null){
            stats.clearUI();
        }
        stats = new OverviewStatPanel(OverviewStatPanelWidth,mainPanel.getPosition().getHeight());
        if(data!=null){
            data.clearUI();
        }
        data = new FleetMarketData(mainPanel.getPosition().getWidth()-OverviewStatPanelWidth-5,mainPanel.getPosition().getHeight());
        data.createUI();
        componentPanel.addComponent(stats.getMainPanel()).inTL(0,0);
        componentPanel.addComponent(data.getMainPanel()).inTL(OverviewStatPanelWidth+5,0);

        // createUI

        mainPanel.addComponent(componentPanel).inTL(0,0);

    }

    @Override
    public void clearUI() {

    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {
        if(forceRequestUpdateListOnly){
            forceRequestUpdateListOnly = false;
            lastSavedMarket = stats.getCurrentMarket();
            if(lastSavedMarket!=null){
                data.setMarket(lastSavedMarket);
                data.createUI();
            }
        }
        if(forceRequestUpdate){
            forceRequestUpdate = false;
            AoTDFactionPatrolsManager.getInstance().advanceAfterFleets(0f);
            data.createUI();
            stats.createUI();


        }

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}