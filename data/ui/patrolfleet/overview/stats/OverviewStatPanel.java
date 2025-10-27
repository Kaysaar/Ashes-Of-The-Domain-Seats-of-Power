package data.ui.patrolfleet.overview.stats;

import ashlib.data.plugins.ui.models.DropDownButton;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.models.ProgressBarComponent;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.vok.ui.basecomps.ImageViewer;
import data.scripts.patrolfleet.managers.FactionPatrolsManager;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.overview.components.EntityRenderer;
import data.ui.patrolfleet.overview.components.EntityWithNameComponent;
import data.ui.patrolfleet.overview.components.HoldingsTable;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class OverviewStatPanel implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,componentPanel;
    UILinesRenderer renderer;
    MarketAPI currentMarket;
    HoldingsTable table;
    public OverviewStatPanel(float width, float height){
        mainPanel = Global.getSettings().createCustom(width,height,this);
        renderer = new UILinesRenderer(0f);
        renderer.setPanel(mainPanel);
        createUI();
    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public MarketAPI getCurrentMarket() {
        return currentMarket;
    }

    @Override
    public void createUI() {
        if(componentPanel!=null){
            mainPanel.removeComponent(componentPanel);
        }
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),false);
        tooltip.addSectionHeading("Military Capabilities", Alignment.MID,0f);
        float width = componentPanel.getPosition().getWidth();
        Color[]colors = new Color[2];
        colors[0] = Misc.getTextColor();
        colors[1] = Color.ORANGE;
        int total = FactionPatrolsManager.getInstance().getTotalFpGenerated();
        int available = FactionPatrolsManager.getInstance().getAvailableFP();
        ProgressBarComponent component = new ProgressBarComponent(width-15,25, (float) available /total, Misc.getDarkPlayerColor().brighter().brighter());
        tooltip.addCustom(component.getRenderingPanel(),0f).getPosition().inTL(7,25);
        LabelAPI labelAPI1 =   tooltip.addPara("Fleet points : %s / %s",5f,colors,""+available,""+total);
        labelAPI1.getPosition().inTL(width/2-(labelAPI1.computeTextWidth(labelAPI1.getText())/2),30);
        tooltip.addSpacer(0f).getPosition().inTL(5,50);
        tooltip.addPara("Admiralty level : %s",5f,Color.ORANGE,"1").setAlignment(Alignment.MID);
        tooltip.addSectionHeading("Faction Holdings", Alignment.MID,5f);
        CustomPanelAPI panelInDenial = Global.getSettings().createCustom(width,componentPanel.getPosition().getHeight()-tooltip.getHeightSoFar()-15,null);
        CustomPanelAPI panel  = Global.getSettings().createCustom(width+3,panelInDenial.getPosition().getHeight(),null);
        if(table!=null){
            table.dropDownButtons.forEach(DropDownButton::clear);
            table.dropDownButtons.clear();
        }

        table = new HoldingsTable(panel.getPosition().getWidth(),panel.getPosition().getHeight(),panel,true,0,0);
        table.createSections();
        table.createTable();
        table.currentlyChosenMarket = currentMarket;
        panelInDenial.addComponent(panel).inTL(-8,0);
        tooltip.addCustom(panelInDenial,5f);

        componentPanel.addUIElement(tooltip).inTL(0,0);
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
        renderer.render(alphaMult);
    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {
        if(table!=null){
            table.advance(amount);
            MarketAPI prev = currentMarket;
            MarketAPI next  = table.currentlyChosenMarket;

            if(!Objects.equals(prev,next)){
                OverviewPatrolPanel.forceRequestUpdateListOnly = true;
            }
            currentMarket = next;
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
