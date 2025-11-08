package data.ui.ambitions;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.ambition.AmbitionManager;
import data.scripts.ambition.BaseAmbition;

import java.util.List;

public class AmbitionFlavourPanel implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,componentPanel;
    BaseAmbition currAmbition;

    public AmbitionFlavourPanel(float height) {
        mainPanel = Global.getSettings().createCustom(480,height,this);
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

        TooltipMakerAPI tooltip  = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),false);
        ImageViewer viewer = new ImageViewer(480,300,Global.getSettings().getSpriteName("illustrations",currAmbition.getSpec().getFullBannerId()));
        renderer.setPanel(viewer.getComponentPanel());
        tooltip.addCustom(viewer.getComponentPanel(),0f);
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
