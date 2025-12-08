package data.ui.patrolfleet.overview.fleetview.templatelist;

import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.DropDownButton;
import ashlib.data.plugins.ui.plugins.UITableImpl;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.utilis.HoldingsUtilis;
import data.ui.patrolfleet.overview.components.HoldingsDropDownButton;
import data.ui.patrolfleet.overview.fleetview.FleetButtonComponent;
import data.ui.patrolfleet.templates.shiplist.components.SortingState;

import java.awt.*;
import java.util.ArrayList;

public class FleetTable extends UITableImpl {
    float currYPos = 0;
    public ButtonAPI buttonName, buttonFleetComp, buttonFpUsed;
    ButtonAPI lastCheckedState;
    public BasePatrolFleet currFleet;
    public ArrayList<String>manuFilters = new ArrayList<>();
    public FleetTable(float width, float height, boolean doesHaveScroller, float xCord, float yCord) {
        super(width, height, doesHaveScroller, xCord, yCord);
        if(dropDownButtons.isEmpty()){
            for (BasePatrolFleetTemplate value : PatrolTemplateManager.getTemplatesAvailableSorted().values()) {
                BasePatrolFleet fleet = new BasePatrolFleet(value);
                fleet.setFleetName(value.nameOfTemplate);
                FleetButtonDrop test = new FleetButtonDrop(this,width-13,60,0,0,fleet);
                dropDownButtons.add(test);

            }


        }

    }
    @Override
    public void createTable() {
        super.createTable();
        boolean found = false;
        for (DropDownButton dropDownButton : dropDownButtons) {
            FleetButtonDrop button = (FleetButtonDrop) dropDownButton;
            if(!manuFilters.isEmpty()){
                if(manuFilters.stream().noneMatch(x->button.fleet.getManufactures().contains(x))){
                    continue;
                }
            }
            if(button.fleet.equals(currFleet)){
                found = true;
            }
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
        if(!found&&currFleet!=null){
            reportButtonPressed(null);
        }
    }
    @Override
    public void createSections() {
        Color base = Misc.getBasePlayerColor();
        Color bg = Misc.getDarkPlayerColor();
        Color bright = Misc.getBrightPlayerColor();
        float usableWidth = width-13;
        buttonName = tooltipOfButtons.addAreaCheckbox("Name", SortingState.ASCENDING, base, bg, bright, 230, 20, 0f);
        buttonFleetComp = tooltipOfButtons.addAreaCheckbox("Fleet Composition", SortingState.NON_INITIALIZED, base, bg, bright, usableWidth-330, 20, 0f);
        buttonFleetComp.setClickable(false);
        buttonFpUsed = tooltipOfButtons.addAreaCheckbox("FP used", SortingState.NON_INITIALIZED, base, bg, bright, 100, 20, 0f);
        buttonName.getPosition().inTL(10, 0);
        buttonFleetComp.getPosition().inTL(240, 0);
        buttonFpUsed.getPosition().inTL(usableWidth-90, 0);
        mainPanel.addUIElement(tooltipOfButtons).inTL(0, 0);
        lastCheckedState = buttonName;
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
            FleetTableUtilis.sortDropDownButtonsByName(dropDownButtons, ascending);
            buttonName.setCustomData(state);

            this.recreateTable();

        }
        if (buttonFpUsed.isChecked()) {
            buttonFpUsed.setChecked(false);
            lastCheckedState = buttonFpUsed;
            SortingState state = (SortingState) buttonFpUsed.getCustomData();
            state = changeState(state);
            boolean ascending = false;
            if (state == SortingState.ASCENDING) {
                ascending = true;
            }
            FleetTableUtilis.sortDropDownButtonsByFPConsumed(dropDownButtons, ascending);
            buttonFpUsed.setCustomData(state);

            this.recreateTable();
        }
        dropDownButtons.forEach(x->{
           if(x instanceof FleetButtonDrop bt){
               if(bt.fleet.equals(currFleet)){
                   x.mainButton.mainButton.highlight();
               }
               else{
                   x.mainButton.mainButton.unhighlight();
               }
           }
        });


    }
    @Override
    public void reportButtonPressed(CustomButton buttonPressed) {
        if(buttonPressed==null)currFleet = null;
        if(buttonPressed instanceof FleetButtonComponent fleet){
            this.currFleet = fleet.getData();
        }

    }
}
