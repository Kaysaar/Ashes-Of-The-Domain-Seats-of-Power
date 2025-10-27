package data.ui.patrolfleet.templates.shiplist.components;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ShipOptionPanelInterface extends BaseOptionPanelManager {

    public static int maxItemsPerPage = 70;
    public ShipOptionPanelInterface(CustomPanelAPI panel, float padding, boolean isForProd) {


        this.padding = padding;
        mapOfButtonStates = new HashMap<>();
        this.mainPanel = panel;
        this.panel = mainPanel.createCustomPanel(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight()-padding, null);
        if(isForProd){
            YHeight = panel.getPosition().getHeight() * 0.45f-padding;
        }
        else {
            YHeight = panel.getPosition().getHeight() * 0.35f-padding;
        }

    }


    public void init() {
        createShipOptions(panel);
        createDesignButtons(ShipPanelData.getShipManInfo());
        createSizeOptions(ShipPanelData.getShipSizeInfo());
        createTypeOptions(ShipPanelData.getShipTypeInfo());
        createSortingButtons(false, false);
        createSerachBarPanel();
        this.mainPanel.addComponent(panel).inTL(0, padding);
    }
    public void initForFleet() {
        createShipOptions(panel);
        createDesignButtons(ShipPanelData.getShipManInfo());
        createSizeOptions(ShipPanelData.getShipSizeInfo());
        createTypeOptions(ShipPanelData.getShipTypeInfo());
        createSortingButtons(false, false);
        createSerachBarPanel();
        this.mainPanel.addComponent(panel).inTL(0, padding);
    }
    @Override
    public void clear() {
        buttonsPage.clear();
        buttons.clear();
        sortingButtons.clear();
        chosenManu.clear();
        if(searchbar!=null){
            searchbar.deleteAll();
        }
        this.mainPanel.removeComponent(panel);
        clearButtons();
    }

    @Override
    public void reInit() {
        this.panel = mainPanel.createCustomPanel(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight()-padding, null);
        init();
    }




    private void createShipOptions(CustomPanelAPI panel) {
        if(orderButtons==null)orderButtons = new ArrayList<>();
        ArrayList<ShipHullSpecAPI> packages = new ArrayList<>(ShipPanelData.learnedShips);
        packages = ShipPanelSorter.getShipPackagesBasedOnTags(chosenManu,chosenSize,chosenType);
        if (resetToText) {
            packages = ShipPanelSorter.getMatchingShipGps(searchbar.getText());
        }
        for (Map.Entry<String, SortingState> option : mapOfButtonStates.entrySet()) {
            if(option.getValue()!= SortingState.NON_INITIALIZED){
                packages= ShipPanelSorter.getShipPackagesBasedOnData(option.getKey(),option.getValue(),packages);
            }
        }
        wantsAll = false;
        resetToText = false;
        currOffset = currPage * maxItemsPerPage;
        float size = packages.size();
        int maxPages = (int) (size / maxItemsPerPage);
        if ((float) maxPages != size / maxItemsPerPage) maxPages++;
        Pair<CustomPanelAPI,ArrayList<ButtonAPI>> orders = OptionPanelDesigner.createShipPanel(ShipUIData.WIDTH_OF_OPTIONS, YHeight, this.panel, packages, currOffset, maxItemsPerPage);
        pageInitalization(panel, maxPages, orders);
    }

    @Override
    public ArrayList<ButtonAPI> getOrderButtons() {
        return orderButtons;
    }


    public void reset() {
        buttonsPage.clear();
        panel.removeComponent(optionPanel);
        panel.removeComponent(buttonPanel);
        createShipOptions(this.panel);
    }


    @Override
    public CustomPanelAPI getOptionPanel() {
        return optionPanel;
    }

    @Override
    public CustomPanelAPI getDesignPanel() {
        return buttonDesignPanel;
    }
}
