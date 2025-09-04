package data.ui.timeline;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import data.ui.basecomps.ExtendUIPanelPlugin;
import data.ui.basecomps.FactionBonusPanel;
import data.ui.basecomps.FactionFlagButtonComponent;
import data.ui.basecomps.FactionXPPanel;

import java.net.URISyntaxException;
import java.util.List;

public class FactionTimelinePanel implements ExtendUIPanelPlugin {
    float goalsWidth = 400;
    CustomPanelAPI mainPanel;
    FactionTimelineViewerComponent component;
    public FactionTimelinePanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
    }

    public void createUI() {
        float width = mainPanel.getPosition().getWidth();
        float height = mainPanel.getPosition().getHeight();

        float widthT = width * 0.5f - 10f;
        float heightT = height - 160;
         component = new FactionTimelineViewerComponent(width-410,height-160);

        mainPanel.addComponent(component.getMainPanel()).inTL(405, 0);

        mainPanel.addComponent(new FactionTimelineGoalsPanel(400, height - 160).getMainPanel()).inTL(0, 0);
        mainPanel.addComponent(new FactionFlagButtonComponent(130, 130).getPanelOfButton()).inTL(((width - 10) / 2) - 65, height - 130);
        mainPanel.addComponent(new FactionXPPanel(450, 130).getMainPanel()).inTL(((width - 10) / 2) - 65 - 455, height - 130);
        mainPanel.addComponent(new FactionBonusPanel(450, 130).getMainPanel()).inTL(((width - 10) / 2) + 70, height - 130);

    }
    public void reset(){
        component.createUI();
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
