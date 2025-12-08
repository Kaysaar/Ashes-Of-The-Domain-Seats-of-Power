package data.ui.patrolfleet.overview.components;

import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.DropDownButton;
import ashlib.data.plugins.ui.plugins.UITableImpl;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.utilis.HoldingsUtilis;
import data.ui.patrolfleet.templates.shiplist.components.SortingState;

import java.awt.*;
import java.util.ArrayList;

public class HoldingsTable extends UITableImpl {
    public ButtonAPI buttonName, buttonFPUsed, buttonFPGenerated;
    float currYPos = 0;
    ButtonAPI lastCheckedState;
    public MarketAPI currentlyChosenMarket;

    public HoldingsTable(float width, float height, CustomPanelAPI panelToPlace, boolean doesHaveScroller, float xCord, float yCord) {
        super(width, height, panelToPlace, doesHaveScroller, xCord, yCord);
        if(dropDownButtons.isEmpty()){
            ArrayList<StarSystemAPI>systems = HoldingsUtilis.getSystemsWithPlayerFactionColonies();
            for (StarSystemAPI system : systems) {
                HoldingsDropDownButton button = new HoldingsDropDownButton(this,width-13,60,0,0,false,system,HoldingsUtilis.getFactionMarketsInSystem(Global.getSector().getPlayerFaction(),system));
                dropDownButtons.add(button);
            }

        }

    }

    @Override
    public void clearTable() {
        super.clearTable();
    }

    @Override
    public void clearUI() {
        super.clearUI();
    }

    @Override
    public void createSections() {
        Color base = Misc.getBasePlayerColor();
        Color bg = Misc.getDarkPlayerColor();
        Color bright = Misc.getBrightPlayerColor();
        float usableWidth = width-13;
        float section =usableWidth/3;
        buttonName = tooltipOfButtons.addAreaCheckbox("Name", SortingState.ASCENDING, base, bg, bright, section+20, 20, 0f);
        buttonFPUsed = tooltipOfButtons.addAreaCheckbox("FP allocated", SortingState.NON_INITIALIZED, base, bg, bright, section-11, 20, 0f);
        buttonFPGenerated = tooltipOfButtons.addAreaCheckbox("FP generated", SortingState.NON_INITIALIZED, base, bg, bright, section-11, 20, 0f);
        buttonName.getPosition().inTL(10, 0);
        buttonFPUsed.getPosition().inTL(section+31, 0);
        buttonFPGenerated.getPosition().inTL((section*2)+21, 0);
        mainPanel.addUIElement(tooltipOfButtons).inTL(0, 0);
        lastCheckedState = buttonName;
    }
    @Override
    public void createTable() {
        super.createTable();
        for (DropDownButton dropDownButton : dropDownButtons) {
            HoldingsDropDownButton button = (HoldingsDropDownButton) dropDownButton;
            button.resetUI();
            button.createUI();
            tooltipOfImpl.addCustom(dropDownButton.getPanelOfImpl(), 2f);
        }

        panelToWorkWith.addUIElement(tooltipOfImpl).inTL(0, 0);
        if (tooltipOfImpl.getExternalScroller() != null) {
            if (currYPos + panelToWorkWith.getPosition().getHeight() - 2 >= tooltipOfImpl.getHeightSoFar()) {
                currYPos = tooltipOfImpl.getHeightSoFar() - panelToWorkWith.getPosition().getHeight() + 2;
            }
            if(currYPos<=0){
                currYPos = 0;
            }
            tooltipOfImpl.getExternalScroller().setYOffset(currYPos);
        }

        mainPanel.addComponent(panelToWorkWith).inTL(0, 22);
    }
    private SortingState changeState(SortingState state) {
        if (state == SortingState.NON_INITIALIZED) {
            return SortingState.ASCENDING;
        }
        if (state == SortingState.ASCENDING) {
            return SortingState.DESCENDING;
        }
        if (state == SortingState.DESCENDING) {
            return SortingState.ASCENDING;
        }
        return SortingState.NON_INITIALIZED;
    }
    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (tooltipOfImpl != null && tooltipOfImpl.getExternalScroller() != null) {
            currYPos = tooltipOfImpl.getExternalScroller().getYOffset();
        }
        if (buttonName.isChecked()) {
            buttonName.setChecked(false);
            lastCheckedState = buttonName;
            SortingState state = (SortingState) buttonName.getCustomData();
            state = changeState(state);
            boolean ascending = false;
            if (state == SortingState.ASCENDING) {
                ascending = true;
            }
            HoldingsUtilis.sortDropDownButtonsByName(dropDownButtons, ascending);
            buttonName.setCustomData(state);

            this.recreateTable();

        }
        if (buttonFPUsed.isChecked()) {
            buttonFPUsed.setChecked(false);
            lastCheckedState = buttonFPUsed;
            SortingState state = (SortingState) buttonFPUsed.getCustomData();
            state = changeState(state);
            boolean ascending = false;
            if (state == SortingState.ASCENDING) {
                ascending = true;
            }
            HoldingsUtilis.sortDropDownButtonsByFPConsumed(dropDownButtons, ascending);
            buttonFPUsed.setCustomData(state);

            this.recreateTable();
        }
        if (buttonFPGenerated.isChecked()) {
            buttonFPGenerated.setChecked(false);
            lastCheckedState = buttonFPGenerated;
            SortingState state = (SortingState) buttonFPGenerated.getCustomData();
            state = changeState(state);
            boolean ascending = false;
            if (state == SortingState.ASCENDING) {
                ascending = true;
            }
            HoldingsUtilis.sortDropDownButtonsFPGenerated(dropDownButtons, ascending);
            buttonFPGenerated.setCustomData(state);
            this.recreateTable();
        }
           dropDownButtons.forEach(x->{
               x.buttons.forEach(y->{
                   if(y.buttonData instanceof MarketAPI market){
                       if(currentlyChosenMarket!=null&&market.getId().equals(currentlyChosenMarket.getId())){
                           y.mainButton.highlight();
                       }
                       else{
                           y.mainButton.unhighlight();
                       }
                   }
               });
           });


    }


    @Override
    public void reportButtonPressed(CustomButton buttonPressed) {
        if(buttonPressed.buttonData instanceof MarketAPI market){
            this.currentlyChosenMarket = market;
        }

    }
}
