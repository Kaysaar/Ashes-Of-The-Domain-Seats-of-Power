package data.ui.ambitions;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ambition.AmbitionManager;
import data.scripts.ambition.BaseAmbition;

import java.awt.*;
import java.util.List;

public class AmbitionCurrQuestsPanel implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,componentPanel;
    BaseAmbition currAmbition;

    public AmbitionCurrQuestsPanel(float width,float height) {
        mainPanel = Global.getSettings().createCustom(width,height,this);
        currAmbition = AmbitionManager.getInstance().getCurrentAmbition();
        createUI();

    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        if(componentPanel!=null){
            mainPanel.removeComponent(componentPanel);
        }
        UILinesRenderer renderer = new UILinesRenderer(0f);
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),renderer);

        TooltipMakerAPI tooltip  = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),true);
        tooltip.setParaFont(Fonts.ORBITRON_24AA);
        tooltip.addPara("Current Goals", Color.ORANGE,0f).setAlignment(Alignment.MID);
        tooltip.setParaFont(Fonts.ORBITRON_20AA);
        tooltip.addPara("First Steps", Misc.getTooltipTitleAndLightHighlightColor(),5f);
        tooltip.setBulletedListMode(BaseIntelPlugin.BULLET);
        tooltip.addPara("Build Research Facility ( 0 / 1 )",2f);
        tooltip.setBulletedListMode(null);


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

    }

    @Override
    public void render(float alphaMult) {

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
