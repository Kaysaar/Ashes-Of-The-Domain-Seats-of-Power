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
    ArrayList<DropDownButton> copyOfButtons = new ArrayList<>();
    public ButtonAPI buttonName, buttonFPUsed, buttonFPGenerated;
    float currYPos = 0;
    ButtonAPI lastCheckedState;
    public MarketAPI currentlyChosenMarket;

    public HoldingsTable(float width, float height, CustomPanelAPI panelToPlace, boolean doesHaveScroller, float xCord, float yCord) {
        super(width, height, panelToPlace, doesHaveScroller, xCord, yCord);
        if(dropDownButtons.isEmpty()){
            ArrayList<StarSystemAPI>systems = HoldingsUtilis.getSystemsWithPlayerFactionColonies();
            for (StarSystemAPI system : systems) {
                HoldingsDropDownButton button = new HoldingsDropDownButton(this,width+9,60,0,0,false,system,HoldingsUtilis.getFactionMarketsInSystem(Global.getSector().getPlayerFaction(),system));
                button.isDropped = true;
                dropDownButtons.add(button);
            }

            copyOfButtons.addAll(dropDownButtons);}
    }

    @Override
    public void createSections() {
        Color base = Misc.getBasePlayerColor();
        Color bg = Misc.getDarkPlayerColor();
        Color bright = Misc.getBrightPlayerColor();
        float usableWidth = width-13;
        float section =usableWidth/3;
        buttonName = tooltipOfButtons.addAreaCheckbox("Name", SortingState.ASCENDING, base, bg, bright, section, 20, 0f);
        buttonFPUsed = tooltipOfButtons.addAreaCheckbox("FP allocated", SortingState.NON_INITIALIZED, base, bg, bright, section, 20, 0f);
        buttonFPGenerated = tooltipOfButtons.addAreaCheckbox("FP generated", SortingState.NON_INITIALIZED, base, bg, bright, section, 20, 0f);
        buttonName.getPosition().inTL(10, 0);
        buttonFPUsed.getPosition().inTL(section+1, 0);
        buttonFPGenerated.getPosition().inTL((section+1)*2, 0);
        mainPanel.addUIElement(tooltipOfButtons).inTL(0, 0);
        lastCheckedState = buttonName;
    }
    public void recreateOldListBasedOnPrevSort(){
        dropDownButtons.clear();
        dropDownButtons.addAll(copyOfButtons);
        sortDBList();
        this.recreateTable();

    }
    private void sortDBList() {
        if(lastCheckedState!=null){
            SortingState state = (SortingState) buttonName.getCustomData();
            boolean ascending = false;
            if (state == SortingState.ASCENDING) {
                ascending = true;
            }
            if(lastCheckedState.equals(buttonName)){
                HoldingsUtilis.sortDropDownButtonsByName(dropDownButtons, ascending);
            }
            if(lastCheckedState.equals(buttonFPGenerated)){
                HoldingsUtilis.sortDropDownButtonsFPGenerated(dropDownButtons, ascending);
            }
            if(lastCheckedState.equals(buttonFPUsed)){
                //Note : func not completed
                HoldingsUtilis.sortDropDownButtonsByFPConsumed(dropDownButtons, ascending);
            }
        }
    }
    public ArrayList<DropDownButton> getHoldings(){
        sortDBList();
        return dropDownButtons;


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
            if (currYPos + panelToWorkWith.getPosition().getHeight() - 22 >= tooltipOfImpl.getHeightSoFar()) {
                currYPos = tooltipOfImpl.getHeightSoFar() - panelToWorkWith.getPosition().getHeight() + 22;
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
