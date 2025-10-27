package data.ui.patrolfleet.templates.shiplist.components;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;

import java.util.ArrayList;

public class OptionPanelDesigner {
    public static Pair<CustomPanelAPI, ArrayList<ButtonAPI>> createShipPanel(float width , float hegiht , CustomPanelAPI parent, ArrayList<ShipHullSpecAPI>options, int currIndex, int amountPerPage){
        CustomPanelAPI panelTest = parent.createCustomPanel(width, hegiht, null);
        ArrayList<ButtonAPI>buttons = new ArrayList<>();
        TooltipMakerAPI tooltip = panelTest.createUIElement(panelTest.getPosition().getWidth(), panelTest.getPosition().getHeight(), true);
        for (int i = currIndex, j = 0; i < options.size() && j < amountPerPage; i++, j++) {
            ShipHullSpecAPI opt = options.get(i);
            UiPackage shipPackage = ShipUIData.getShipOption(opt);
            tooltip.addCustom(shipPackage.getPanelPackage(), 5f);
            shipPackage.getRender().setAbsoultePanel(panelTest);
            buttons.add(shipPackage.button);

        }
        panelTest.addUIElement(tooltip).inTL(-5, 0);
        return new Pair<>(panelTest,buttons);
    }

}
