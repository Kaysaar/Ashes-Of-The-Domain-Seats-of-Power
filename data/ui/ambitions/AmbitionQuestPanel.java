package data.ui.ambitions;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.models.ProgressBarComponent;
import ashlib.data.plugins.ui.models.ProgressBarComponentV2;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ambition.AmbitionManager;
import data.scripts.ambition.BaseAmbition;
import data.scripts.managers.AoTDFactionManager;

import java.awt.*;
import java.util.List;

public class AmbitionQuestPanel implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,componentPanel;
    AmbitionFlavourPanel flavourPanel;
    AmbitionCurrQuestsPanel questPanel;
    public AmbitionQuestPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width,height,this);
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
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),false);
        BaseAmbition ambition = AmbitionManager.getInstance().getCurrentAmbition();
        tooltip.setTitleOrbitronVeryLarge();
        tooltip.addTitle("Ambition Progress");
        tooltip.setParaFont(Fonts.ORBITRON_20AA);
        ProgressBarComponentV2 progres = new ProgressBarComponentV2(componentPanel.getPosition().getWidth()-5,30,"20%",Fonts.ORBITRON_20AA,Misc.getDarkPlayerColor().brighter(),Misc.getBasePlayerColor(),0.2f){
            @Override
            public void influenceLabel() {
                LabelAPI label = getProgressLabel();
                label.setColor(Color.ORANGE);
            }
        };

        tooltip.addCustom(progres.getMainPanel(),5f);
        tooltip.setParaFont(Fonts.ORBITRON_16);
        tooltip.addPara("With each step, we are closer to reach goals we set ourselves",3f).setAlignment(Alignment.MID);
        float currY = tooltip.getHeightSoFar();
        if(flavourPanel==null){
            flavourPanel = new AmbitionFlavourPanel(300);
        }
        if(questPanel==null){
            questPanel = new AmbitionCurrQuestsPanel(componentPanel.getPosition().getWidth()-490,300);
        }
        tooltip.addCustom(questPanel.getMainPanel(),5f).getPosition().inTL(0,currY+5);
        tooltip.addCustom(flavourPanel.getMainPanel(),5f).getPosition().inTL(componentPanel.getPosition().getWidth()-flavourPanel.getMainPanel().getPosition().getWidth()-15,currY+5);
        tooltip.addSpacer(0f).getPosition().inTL(0,currY+310);
        BaseAmbition amb = AmbitionManager.getInstance().getCurrentAmbition();
        amb.createFlavourTooltip(tooltip);
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
