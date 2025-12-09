package data.ui.basecomps;

import ashlib.data.plugins.ui.models.ProgressBarComponentV2;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;

import java.awt.*;
import java.util.List;

public class FactionXPPanel implements ExtendUIPanelPlugin {
    public CustomPanelAPI mainPanel;
    public CustomPanelAPI panelForUI;

    UILinesRenderer renderer = new UILinesRenderer(0f);
    boolean renderBorders = true;
    public FactionXPPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
        renderer = new UILinesRenderer(0f);
        renderer.setPanel(mainPanel);

    }
    public FactionXPPanel(float width, float height,boolean renderBorders) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
        this.renderBorders = renderBorders;
        renderer = new UILinesRenderer(0f);
        renderer.setPanel(mainPanel);

    }

    public void createUI(){
        if(panelForUI!=null){
            mainPanel.removeComponent(panelForUI);
        }

        panelForUI = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = panelForUI.createUIElement(panelForUI.getPosition().getWidth(),panelForUI.getPosition().getHeight(),false);
        float width = panelForUI.getPosition().getWidth();
        tooltip.setTitleFont(Fonts.ORBITRON_24AABOLD);
        LabelAPI label =tooltip.addTitle("Faction Experience");
        label.setAlignment(Alignment.MID);
        ProgressBarComponentV2 component = new ProgressBarComponentV2(width-15,21, AoTDFactionManager.getInstance().getProgressXP(), Misc.getDarkPlayerColor().brighter().brighter());
        tooltip.addCustom(component.getRenderingPanel(),5f);
        tooltip.setParaFontOrbitron();
        Color[] colors = new Color[2];
        colors[0]=Misc.getTextColor();
        colors[1]=Misc.getHighlightColor();
        int effectiveLevel = +AoTDFactionManager.getInstance().getEffectiveLevel();
        label = tooltip.addPara("%s / %s",-19f,colors,""+ AoTDFactionManager.getInstance().getEffectiveXP(), AoTDFactionManager.getInstance().getLevelThreshold(effectiveLevel)+"");
        label.autoSizeToWidth(width-15);
        label.setAlignment(Alignment.MID);
        label = tooltip.addPara("Level : %s",7f,Color.ORANGE,""+ effectiveLevel);
        label.setAlignment(Alignment.MID);
        tooltip.setParaFontOrbitron();
        label =  tooltip.addPara("Current amount of policy slots : %s",5f,Color.ORANGE,AoTDFactionManager.getInstance().getPolicySlotsFromLevel(AoTDFactionManager.getInstance().getEffectiveLevel())+"");
        label.setAlignment(Alignment.MID);
        label = tooltip.addPara("Next policy slot will be unlocked at the level : %s",3f,Color.ORANGE,AoTDFactionManager.getInstance().getNextLevelForPolicySlot(AoTDFactionManager.getInstance().getEffectiveLevel())+"");
        label.setAlignment(Alignment.MID);
        label = tooltip.addPara("Next admin slot will be unlocked at the level : %s",3f,Color.ORANGE,AoTDFactionManager.getInstance().getNextLevelForAdminSlot(AoTDFactionManager.getInstance().getEffectiveLevel())+"");
        label.setAlignment(Alignment.MID);

        panelForUI.addUIElement(tooltip).inTL(0,2f);
        mainPanel.addComponent(panelForUI);

    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {
        if(renderBorders){
            renderer.render(alphaMult);

        }
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
