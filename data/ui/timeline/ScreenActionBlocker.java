package data.ui.timeline;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import data.misc.ProductionUtil;
import data.ui.basecomps.ExtendUIPanelPlugin;

import java.util.List;

public class ScreenActionBlocker implements ExtendUIPanelPlugin {
    CustomPanelAPI mainPanel;
    public ScreenActionBlocker(){
        mainPanel = Global.getSettings().createCustom(Global.getSettings().getScreenWidth(),Global.getSettings().getScreenHeight(),this);
        ProductionUtil.getCoreUI().addComponent(mainPanel).inTL(0,0);
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
            events.stream().filter(x->!x.isConsumed()).forEach(InputEventAPI::consume);

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {

    }
}
