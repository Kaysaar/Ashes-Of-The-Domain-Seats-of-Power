package data.ui.patrolfleet.templates.shiplist.dialog.templaterandom;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

public class ShipTypeCounter implements ExtendedUIPanelPlugin {
    ButtonAPI add,remove;
    ButtonAPI add5,remove5;
     ShipAPI.HullSize data;
    public  int current =0;
    CustomPanelAPI mainPanel,contentPanel,textPanel,textPanel2;
    public ShipTypeCounter(float width, ShipAPI.HullSize data) {
        mainPanel = Global.getSettings().createCustom(width,20,this);
        this.data =data;
        createUI();
    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        float width = getMainPanel().getPosition().getWidth();
        float height = getMainPanel().getPosition().getHeight();
        if (contentPanel != null) {
            getMainPanel().removeComponent(contentPanel);
        }
        contentPanel = Global.getSettings().createCustom(width, height, null);
        float effectiveWidth = getEffectiveWidth();
        TooltipMakerAPI tooltipButtons = contentPanel.createUIElement(getEffectiveWidth(), 20, false);

        remove5 = tooltipButtons.addButton("-5",-5, Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE,20,20,0f);
        remove = tooltipButtons.addButton("-1",-1, Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID, CutStyle.NONE,20,20,0f);

        add = tooltipButtons.addButton("+1",1, Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID, CutStyle.NONE,20,20,0f);
        add5 = tooltipButtons.addButton("+5",5, Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID, CutStyle.NONE,20,20,0f);


        remove5.getPosition().inTL(1,0);
        remove.getPosition().inTL(22,0);

        add.getPosition().inTL(effectiveWidth-42,0);
        add5.getPosition().inTL(effectiveWidth-21,0);
        contentPanel.addUIElement(tooltipButtons).inTL(0, 0);

        updateUI();
        getMainPanel().addComponent(contentPanel).inTL(0, 0);
    }

    private static int getEffectiveWidth() {
        return 180;
    }

    @Override
    public void clearUI() {

    }

    public void updateUI() {
        if(textPanel!=null&&contentPanel!=null) {
            contentPanel.removeComponent(textPanel);
        }
        if(textPanel2!=null&&contentPanel!=null) {
            contentPanel.removeComponent(textPanel2);
        }
        float height = getMainPanel().getPosition().getHeight();
        float effectiveWidth = getEffectiveWidth();
        textPanel2 = Global.getSettings().createCustom(contentPanel.getPosition().getWidth()-(getEffectiveWidth()+10), height, null);
        TooltipMakerAPI creditTooltip = textPanel2.createUIElement(contentPanel.getPosition().getWidth()-(getEffectiveWidth()+10), 20, false);
        creditTooltip.setParaFont(Fonts.ORBITRON_12);


        textPanel = Global.getSettings().createCustom(effectiveWidth-88,20,null);
        TooltipMakerAPI tooltipTextPanel = textPanel.createUIElement(effectiveWidth-88,20,false);
        ButtonAPI button =  tooltipTextPanel.addAreaCheckbox("",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Misc.getBrightPlayerColor(),textPanel.getPosition().getWidth(),textPanel.getPosition().getHeight(),0f);
        button.setClickable(false);
        button.setHighlightBrightness(0f);
        button.setGlowBrightness(0f);
        button.getPosition().inTL(0,0);
        LabelAPI label = tooltipTextPanel.addPara("%s",0f,new Color[]{Color.ORANGE},""+current);
        tooltipTextPanel.setParaFont(Fonts.ORBITRON_12);
        LabelAPI label2 = tooltipTextPanel.addPara("%s",0f,new Color[]{Misc.getTooltipTitleAndLightHighlightColor()},Misc.getHullSizeStr(data));
        label.setAlignment(Alignment.MID);
        label2.setAlignment(Alignment.MID);
        label.getPosition().inTL(0,2);
        label2.getPosition().inTL(0,-20);
        textPanel.addUIElement(tooltipTextPanel).inTL(0,0);
        contentPanel.addComponent(textPanel).inTL(44,0);
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
        if(add.isChecked()){
            add.setChecked( false);
            setCurrent(1);
            updateUI();
        }
        if(add5.isChecked()){
            add5.setChecked( false);
            setCurrent(5);
            updateUI();
        }
        if(remove5.isChecked()){
            remove5.setChecked( false);
            setCurrent(-5);
            updateUI();
        }
        if(remove.isChecked()){
            remove.setChecked( false);
            setCurrent(-1);
            updateUI();
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
    public void setCurrent(int byHowMuch){
        current+=byHowMuch;
        if(current<0){
            current =0;
        }
    }
}
