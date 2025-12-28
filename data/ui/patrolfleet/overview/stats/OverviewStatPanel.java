package data.ui.patrolfleet.overview.stats;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.models.ProgressBarComponentV2;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;
import data.plugins.AoTDSopMisc;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.utilis.PatrolFleetFactory;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.overview.components.PatrolFleetHoldingsTable;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OverviewStatPanel implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel, componentPanel;
    UILinesRenderer renderer;
    MarketAPI currentMarket;
    PatrolFleetHoldingsTable table;
    AggressivenessChangerComponent aggressionMeter;
    AdmiraltyLevelComponent levelComponent;
    public OverviewStatPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
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
        if (componentPanel != null) {
            mainPanel.removeComponent(componentPanel);
        }
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), null);
        TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(), componentPanel.getPosition().getHeight(), false);
        tooltip.addSectionHeading("Military Capabilities", Alignment.MID, 0f);
        float width = componentPanel.getPosition().getWidth();
        Color[] colors = new Color[2];
        colors[0] = Misc.getPositiveHighlightColor();
        colors[1] = Color.ORANGE;
        int total = AoTDFactionPatrolsManager.getInstance().getTotalFpGenerated();
        int available = AoTDFactionPatrolsManager.getInstance().getAvailableFP();
        int taken = AoTDFactionPatrolsManager.getInstance().getFPUsed(true);
        if (taken > total) {
            colors[1] = Misc.getNegativeHighlightColor();
        }
        ProgressBarComponentV2 component = new ProgressBarComponentV2(width - 15, 25,"Fleet points : "+total+" / "+taken,null, Misc.getDarkPlayerColor().brighter(),Misc.getBasePlayerColor(), Math.min(1f, (float) total / taken)){
            @Override
            public void influenceLabel() {
                this.getProgressLabel().setHighlightColors(colors);
                this.getProgressLabel().setHighlight(""+total,""+taken);
            }
        };
        tooltip.addCustom(component.getMainPanel(), 0f).getPosition().inTL(7, 25);
        tooltip.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return true;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 400f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Fleet points represent military capability of faction, to deploy fleets across your holdings.", 3f);
                tooltip.addPara("%s provide certain amount of FP points based on market size and fleet size multiplier", 5f, Color.ORANGE, AoTDSopMisc.getAllIndustriesJoined(AoTDMilitaryBase.industriesValidForBase.stream().toList(), "and"));
                int total = AoTDFactionPatrolsManager.getInstance().getTotalFpGenerated();
                int taken = AoTDFactionPatrolsManager.getInstance().getFPUsed(true);
                tooltip.addPara("Your faction generates %s FP points.", 5f, Misc.getPositiveHighlightColor(), "" + total);
                MutableStat stat = AoTDFactionPatrolsManager.getInstance().getAdditionalFpGranted();
                if(expanded){
                    if(!stat.getFlatMods().isEmpty()&&stat.getModifiedInt()>0){
                        tooltip.addPara("From which %s is generated additionally from:",3f,Color.ORANGE,""+stat.getModifiedInt());
                        tooltip.setBulletedListMode(BaseIntelPlugin.BULLET);
                        for (MutableStat.StatMod object : stat.getFlatMods().values()) {
                            tooltip.addPara("%s : %s",3f,new Color[]{Misc.getPositiveHighlightColor(),Misc.getTextColor()},Misc.getRoundedValue(object.getValue()),object.getDesc());
                        }
                        tooltip.setBulletedListMode(null);
                    }
                }

                if (taken > total) {
                    tooltip.addPara("Your faction consumes %s FP points.", 5f, Misc.getNegativeHighlightColor(), "" + taken);
                    createAddditonalCost(tooltip, expanded);


                } else {
                    tooltip.addPara("Your faction consumes %s FP points.", 5f, Color.ORANGE, "" + taken);
                    createAddditonalCost(tooltip, expanded);


                }
                tooltip.addPara("If required FP will exceed FP generated, then some of patrols will become grounded and unable to perform patrol duties!", Misc.getNegativeHighlightColor(), 10f);
                tooltip.addPara("Note : Any other structure that spawn additional fleets can affect FP, based on spawned fleets and can't be controlled like fleets created by our faction directly!", Misc.getTooltipTitleAndLightHighlightColor(), 10f);
            }
        }, TooltipMakerAPI.TooltipLocation.RIGHT, false);
        FactionDoctrineAPI doctrine = Global.getSector().getPlayerFaction().getDoctrine();

        CustomPanelAPI secondPanelInDenial = Global.getSettings().createCustom(width, 55, null);
        TooltipMakerAPI tl = secondPanelInDenial.createUIElement(secondPanelInDenial.getPosition().getWidth(), secondPanelInDenial.getPosition().getHeight(), false);
        LabelAPI admTab = tl.addPara("Admiralty level : %s", 0f, Color.ORANGE, "" + doctrine.getOfficerQuality());
        admTab.setHighlightOnMouseover(true);
        tl.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
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
                tooltip.addPara("Admiralty decides on quality, as well as number of officers spawning in your patrol fleets",3f);
                tooltip.addPara("To achieve admiralty level you need to spent this amount of FP!",5f);
                tooltip.addSpacer(5f);
                for (Map.Entry<Integer, Integer> entry : AoTDFactionPatrolsManager.levels.entrySet()) {
                    tooltip.addPara("Level %s :%s FP required to spent",4f,Color.ORANGE,""+entry.getKey(),""+entry.getValue());
                }
                tooltip.addPara("If FP won't be met, then level will be decreased!",Misc.getNegativeHighlightColor(),3f);
                int typicalCombatShips = Math.round(Global.getSettings().getFloat("baseCombatShipsForMaxOfficerLevel"));
                PatrolFleetFactory.addMiniForCurrentQuality(tooltip,5f,Global.getSector().getPlayerFaction(),typicalCombatShips);

                int total = AoTDFactionPatrolsManager.getInstance().getTotalFpGenerated();
                int points = AoTDFactionPatrolsManager.levels.get(AoTDFactionPatrolsManager.MAX_ADMIRALTY_LEV);
                int needed = total/points;
                tooltip.addSpacer(5f);


            }
        }, TooltipMakerAPI.TooltipLocation.BELOW, false);
        LabelAPI lab = tl.addPara("Aggression", 0f, Color.ORANGE, "" + doctrine.getOfficerQuality());
        if (aggressionMeter == null) {
            aggressionMeter = new AggressivenessChangerComponent();
        }
        if (levelComponent== null) {
            levelComponent = new AdmiraltyLevelComponent();
        }

        tl.addCustomDoNotSetPosition(aggressionMeter.getMainPanel()).getPosition().setLocation(0,0).inTL(width - aggressionMeter.getMainPanel().getPosition().getWidth() - 10f, 20);
        tl.addCustomDoNotSetPosition(levelComponent.getMainPanel()).getPosition().setLocation(0,0).inTL(0, 20);

        float centerX = (width - aggressionMeter.getMainPanel().getPosition().getWidth() - 10f+(aggressionMeter.getMainPanel().getPosition().getWidth()/2));

        lab.getPosition().inTL(centerX- (lab.computeTextWidth(lab.getText()) / 2), 0);

        centerX = levelComponent.getMainPanel().getPosition().getWidth()/2;
        admTab.getPosition().inTL(centerX- (admTab.computeTextWidth(admTab.getText()) / 2), 0);
        admTab.autoSizeToWidth(admTab.computeTextWidth(admTab.getText()));
        secondPanelInDenial.addUIElement(tl).inTL(0, 0);
        tooltip.addCustom(secondPanelInDenial, 5f);

        tooltip.addSectionHeading("Faction Holdings", Alignment.MID, 5f);
        CustomPanelAPI panelInDenial = Global.getSettings().createCustom(width, componentPanel.getPosition().getHeight() - tooltip.getHeightSoFar() - 35, null);

        float offsetY;
        if (table != null) {
            table.recreateTable();
        }
        else{
            CustomPanelAPI panel = Global.getSettings().createCustom(width +1, panelInDenial.getPosition().getHeight(), null);
            table = new PatrolFleetHoldingsTable(panel.getPosition().getWidth(), panel.getPosition().getHeight(), panel, true, 0, 0);
            table.createSections();
            table.createTable();
            table.currentlyChosenMarket = currentMarket;
        }

   ;

        panelInDenial.addComponent(table.mainPanel).inTL(-8, 0);
        tooltip.addCustom(panelInDenial, 5f);

        componentPanel.addUIElement(tooltip).inTL(0, 0);
        mainPanel.addComponent(componentPanel).inTL(0, 0);
    }

    private void createAddditonalCost(TooltipMakerAPI tooltip, boolean expanded) {
        MutableStat stat;
        if(expanded){
            stat = AoTDFactionPatrolsManager.getInstance().getAdditionalFPConsumed();
            if(!stat.getFlatMods().isEmpty()&&stat.getModifiedInt()>0){
                tooltip.addPara("From which %s is consumed on :",3f, Color.ORANGE,""+stat.getModifiedInt());
                tooltip.setBulletedListMode(BaseIntelPlugin.BULLET);
                for (MutableStat.StatMod object : stat.getFlatMods().values()) {
                    tooltip.addPara("%s : %s",3f,new Color[]{Misc.getNegativeHighlightColor(),Misc.getTextColor()},Misc.getRoundedValue(object.getValue()),object.getDesc());
                }
                tooltip.setBulletedListMode(null);
            }
        }
    }

    @Override
    public void clearUI() {
        table.clearUI();
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
        if (table != null) {
            table.advance(amount);
            MarketAPI prev = currentMarket;
            MarketAPI next = table.currentlyChosenMarket;

            if (!Objects.equals(prev, next)) {
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
