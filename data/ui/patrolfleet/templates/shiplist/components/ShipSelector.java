package data.ui.patrolfleet.templates.shiplist.components;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.List;

public class ShipSelector implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel;
    CustomPanelAPI componentPanel;
    public ShipOptionPanelInterface optionPanel;

    public ShipSelector(float width, float height){
        ShipUIData.recompute(width,height);
        mainPanel = Global.getSettings().createCustom(width,height,this);
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        optionPanel =  new ShipOptionPanelInterface(componentPanel,0,true);
        optionPanel.init();
        mainPanel.addComponent(componentPanel).inTL(0,0);

    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {

    }

    @Override
    public void clearUI() {
        optionPanel.clear();
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
        if(optionPanel!=null){
            optionPanel.advance(amount);
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
