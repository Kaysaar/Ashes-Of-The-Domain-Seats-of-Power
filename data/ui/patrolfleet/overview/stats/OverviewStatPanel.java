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
import data.industry.AoTDMilitaryBase;
import data.plugins.AoTDSopMisc;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
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
        colors[0] = Misc.getPositiveHighlightColor();
        colors[1] = Color.ORANGE;
        int total = AoTDFactionPatrolsManager.getInstance().getTotalFpGenerated();
        int available = AoTDFactionPatrolsManager.getInstance().getAvailableFP();
        int taken = AoTDFactionPatrolsManager.getInstance().getFPUsed(true);
        if(taken>total){
            colors[1]=Misc.getNegativeHighlightColor();
        }
        ProgressBarComponent component = new ProgressBarComponent(width-15,25,  Math.min(1f, (float) total /taken) ,Misc.getDarkPlayerColor().brighter().brighter());
        tooltip.addCustom(component.getRenderingPanel(),0f).getPosition().inTL(7,25);
        tooltip.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 400f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Fleet points represent military capability of faction, to deploy fleets across your holdings.",3f);
                tooltip.addPara("%s provide certain amount of FP points based on market size and fleet size multiplier",5f,Color.ORANGE, AoTDSopMisc.getAllIndustriesJoined(AoTDMilitaryBase.industriesValidForBase.stream().toList(),"and"));
                int total = AoTDFactionPatrolsManager.getInstance().getTotalFpGenerated();
                int taken = AoTDFactionPatrolsManager.getInstance().getFPUsed(true);
                tooltip.addPara("Your faction generates %s FP points.",5f,Misc.getPositiveHighlightColor(),""+total);
                if(taken>total){
                    tooltip.addPara("Your faction consumes %s FP points.",3f,Misc.getNegativeHighlightColor(),""+taken);

                }
                else{
                    tooltip.addPara("Your faction consumes %s FP points.",3f,Color.ORANGE,""+taken);

                }
                tooltip.addPara("If required FP will exceed FP generated, then some of patrols will become grounded and unable to perform patrol duties!",Misc.getNegativeHighlightColor(),10f);
                tooltip.addPara("Note : Any other structure that spawn additional fleets can affect FP, based on spawned fleets and can't be controlled like fleets created by our faction directly!",Misc.getTooltipTitleAndLightHighlightColor(),10f);
            }
        }, TooltipMakerAPI.TooltipLocation.BELOW,false);
        LabelAPI labelAPI1 =   tooltip.addPara("Fleet points : %s / %s",5f,colors,""+total,""+taken);
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
