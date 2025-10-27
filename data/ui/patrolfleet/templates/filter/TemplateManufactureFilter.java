package data.ui.patrolfleet.templates.filter;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.ui.patrolfleet.templates.shiplist.components.RowData;
import data.ui.patrolfleet.templates.shiplist.components.ShipPanelData;
import data.ui.patrolfleet.templates.shiplist.components.ShipUIData;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static data.ui.patrolfleet.templates.shiplist.components.BaseOptionPanelManager.extractManufacturer;
import static data.ui.patrolfleet.templates.shiplist.components.ShipPanelData.arrayContains;

public class TemplateManufactureFilter implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,componentPanel;
    public TemplateManufactureFilter(float width,float height){
        mainPanel = Global.getSettings().createCustom(width,height,this);
    }
    String headerOverride = null;

    public void setHeaderOverride(String headerOverride) {
        this.headerOverride = headerOverride;
    }

    public ArrayList<ButtonAPI>buttons = new ArrayList<>();
    public ArrayList<String> chosenManu = new ArrayList<>();
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }
    public void pruneMap(LinkedHashMap<String,Integer>map){

    }
    @Override
    public void createUI() {
        if(componentPanel!=null){
            mainPanel.removeComponent(componentPanel);
            buttons.clear();
        }
        float currY = 1;
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI testHeader = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),20,false);
        testHeader.setParaFont(Fonts.ORBITRON_20AA);
        if(headerOverride==null)headerOverride = "Manufactures";
        testHeader.addPara(headerOverride,0f).setAlignment(Alignment.MID);
        TooltipMakerAPI tooltipButDesigners = componentPanel.createUIElement(componentPanel.getPosition().getWidth(), componentPanel.getPosition().getHeight()-25, true);

        LinkedHashMap<String,Integer>manu = ShipPanelData.getManuForShipsUnbound();
        pruneMap(manu);
        Color base = Misc.getBasePlayerColor();
        Color bg = Misc.getDarkPlayerColor();
        Color bright = Misc.getBrightPlayerColor();
        for (RowData calculateAmountOfRow : ShipUIData.calculateAmountOfRowsIgnoreBrackets(componentPanel.getPosition().getWidth(),manu, 5)) {
            float x = 0;
            tooltipButDesigners.setButtonFontDefault();
            for (Map.Entry<String, Integer> entry : calculateAmountOfRow.stringsInRow.entrySet()) {
                String manus = extractManufacturer(entry.getKey());
                ButtonAPI button = tooltipButDesigners.addAreaCheckbox("", manus, base, bg, bright, entry.getValue(), 30, 0f);
                button.getPosition().inTL(x, currY);
                buttons.add(button);
                handleButtonHighlight(chosenManu, button);
                tooltipButDesigners.addPara(manus, Misc.getDesignTypeColor(manus), 0f).getPosition().inTL((x + 15), currY + 8);
                x += entry.getValue() + 5f;
            }
            currY += 35;
        }

        tooltipButDesigners.setHeightSoFar(currY);
        componentPanel.addUIElement(testHeader).inTL(0, 0);
        componentPanel.addUIElement(tooltipButDesigners).inTL(0, 25);

        mainPanel.addComponent(componentPanel).inTL(0, 0);

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
    public void onChange(){

    }

    @Override
    public void advance(float amount) {
        for (ButtonAPI button : buttons) {
            handleButtonHighlight(chosenManu, button);
            if (button.isChecked()) {
                if (button.getCustomData() instanceof String) {
                    button.setChecked(false);
                    if (button.getCustomData().equals("All designs")) {;
                        chosenManu.clear();
                        onChange();
                        break;
                    }
                    handleDataList(chosenManu,button);
                    onChange();
                }
                break;
            }
        }
    }
    private void handleDataList(ArrayList<String>array,ButtonAPI button) {
        if(arrayContains(array, (String) button.getCustomData())){
            array.remove((String) button.getCustomData());
        }
        else{
            array.add((String) button.getCustomData());
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
    private void handleButtonHighlight(ArrayList<String> chosenManu, ButtonAPI button) {
        if (!chosenManu.isEmpty()) {
            if (arrayContains(chosenManu, (String) button.getCustomData())) {
                button.highlight();
            } else {
                button.unhighlight();
            }
        } else {
            button.highlight();
        }
    }
}
