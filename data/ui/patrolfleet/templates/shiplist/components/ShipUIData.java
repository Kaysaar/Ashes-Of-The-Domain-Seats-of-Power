package data.ui.patrolfleet.templates.shiplist.components;

import ashlib.data.plugins.info.ShipInfoGenerator;
import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.rendering.ShipRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.ui.impl.StandardTooltipV2;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;
import data.misc.ReflectionUtilis;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShipUIData {

    public static float HEIGHT_OF_OPTIONS =0f;
    public static float WIDTH_OF_OPTIONS = 0f;
    public static float HEIGHT_OF_BUTTONS = 50;
    public static float WIDTH_OF_NAME = WIDTH_OF_OPTIONS * 0.26f;
    public static float WIDTH_OF_SIZE = WIDTH_OF_OPTIONS * 0.08f;
    public static float WIDTH_OF_TYPE = WIDTH_OF_OPTIONS * 0.08f;
    public static float WIDTH_OF_DESIGN_TYPE = WIDTH_OF_OPTIONS * 0.20f;
    public static float WIDTH_OF_CREDIT_COST = WIDTH_OF_OPTIONS * 0.15f;
    public static float WIDTH_OF_GP = WIDTH_OF_OPTIONS * 0.15f - 5f;

    public static void recompute(float width, float height) {

        HEIGHT_OF_OPTIONS = height - 270;
        WIDTH_OF_OPTIONS = width;
        HEIGHT_OF_BUTTONS = 55;
        WIDTH_OF_NAME = WIDTH_OF_OPTIONS * 0.36f;
        WIDTH_OF_SIZE = WIDTH_OF_OPTIONS * 0.12f;
        WIDTH_OF_TYPE = WIDTH_OF_OPTIONS * 0.12f;
        WIDTH_OF_DESIGN_TYPE = WIDTH_OF_OPTIONS * 0.40f;
    }
    public static UiPackage getShipOption(ShipHullSpecAPI option) {
        FactionAPI faction = Global.getSector().getPlayerFaction();
        Color base = faction.getBaseUIColor();
        Color bright = faction.getBrightUIColor();
        Color bg = faction.getDarkUIColor();
        CustomPanelAPI panel = Global.getSettings().createCustom(WIDTH_OF_OPTIONS - 5, HEIGHT_OF_BUTTONS, null);
        TooltipMakerAPI mainTooltip = panel.createUIElement(WIDTH_OF_OPTIONS - 5, HEIGHT_OF_BUTTONS, false);
        ButtonAPI button = mainTooltip.addAreaCheckbox("", option, base, bg, bright, WIDTH_OF_OPTIONS - 5, HEIGHT_OF_BUTTONS, 0f);
        button.getPosition().inTL(0, 0);

        Pair<CustomPanelAPI, ShipRenderer> panelImage = ShipInfoGenerator.getShipImage(option, 30, null);
        LabelAPI name = mainTooltip.addPara(option.getHullName(), 0f, Misc.getTooltipTitleAndLightHighlightColor());
        name.autoSizeToWidth(WIDTH_OF_NAME - 37);
        name.getPosition().inTL(35, getyPad(name));

        LabelAPI size = mainTooltip.addPara(Misc.getHullSizeStr(option.getHullSize()), 0f);
        LabelAPI type = mainTooltip.addPara(AshMisc.getType(option), 0f);
        LabelAPI designType = mainTooltip.addPara(option.getManufacturer(), Misc.getDesignTypeColor(option.getManufacturer()), 0f);
        size.getPosition().inTL(getxPad(size, getCenter(WIDTH_OF_NAME  , WIDTH_OF_SIZE)), getyPad(size));
        type.getPosition().inTL(getxPad(type, getCenter(WIDTH_OF_NAME  + WIDTH_OF_SIZE, WIDTH_OF_TYPE)), getyPad(type));
        designType.getPosition().inTL(getxPad(designType, getCenter(WIDTH_OF_NAME  + WIDTH_OF_SIZE + WIDTH_OF_TYPE, WIDTH_OF_DESIGN_TYPE)), getyPad(designType));
        float centerXTotalCost =  getCenter(WIDTH_OF_NAME  + WIDTH_OF_SIZE + WIDTH_OF_TYPE+ WIDTH_OF_DESIGN_TYPE,WIDTH_OF_CREDIT_COST+WIDTH_OF_GP);

//        credits.getPosition().inTL(centerXTotalCost-(credits.computeTextWidth(credits.getText())/2),35);
        mainTooltip.addCustom(panelImage.one, 5f).getPosition().inTL(2, 12);
        FleetMemberAPI fleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, AshMisc.getVaraint(option));
        fleetMemberAPI.getRepairTracker().setCR(0.7f);
        fleetMemberAPI.getCrewComposition().addCrew(fleetMemberAPI.getMinCrew());
        fleetMemberAPI.updateStats();
        createTooltipForShip(fleetMemberAPI,mainTooltip);
        panel.addUIElement(mainTooltip).inTL(-5, 0);
        return new UiPackage(panel, panelImage.two, option, button);

    }



    public static void createTooltipForShip(final FleetMemberAPI fleetMemberAPI, final TooltipMakerAPI tooltip) {
       final Object standardTooltipV2 = ReflectionUtilis.invokeStaticMethodWithAutoProjection(StandardTooltipV2.class,"createFleetMemberExpandedTooltip", fleetMemberAPI,null);
       ReflectionUtilis.invokeStaticMethod(StandardTooltipV2Expandable.class,"addTooltipBelow", tooltip.getPrev(),standardTooltipV2);
    }
    public static void createTooltipForShip(final ShipHullSpecAPI spec,final UIComponentAPI comp) {
        FleetMemberAPI fleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, AshMisc.getVaraint(spec));
        fleetMemberAPI.getRepairTracker().setCR(0.7f);
        fleetMemberAPI.getCrewComposition().addCrew(fleetMemberAPI.getMinCrew());
        fleetMemberAPI.updateStats();
        final Object standardTooltipV2 = ReflectionUtilis.invokeStaticMethodWithAutoProjection(StandardTooltipV2.class,"createFleetMemberExpandedTooltip", fleetMemberAPI,null);
        ReflectionUtilis.invokeStaticMethod(StandardTooltipV2Expandable.class,"addTooltipBelow",comp,standardTooltipV2);
    }
    private static float getCenter(float beginX, float width) {
        ;
        float endX = beginX + width;
        float widthOfSection = endX - beginX;
        float center = beginX + widthOfSection / 2;
        return center;
    }

    private static float getxPad(LabelAPI buildTime, float center) {
        return center - (buildTime.computeTextWidth(buildTime.getText()) / 2);
    }

    private static float getyPad(LabelAPI name) {
        return (HEIGHT_OF_BUTTONS / 2) - (name.computeTextHeight(name.getText()) / 2);
    }

    public static ArrayList<RowData> calculateAmountOfRows(float widthOfRow, LinkedHashMap<String, Integer> designs, float xPadding) {
        ArrayList<RowData> data = new ArrayList<>();
        float currentX = 0;
        float rows = 0;
        RowData daten = new RowData(rows, new LinkedHashMap<String, Integer>());
        LabelAPI dummy = Global.getSettings().createLabel("", Fonts.DEFAULT_SMALL);
        for (Map.Entry<String, Integer> entry : designs.entrySet()) {
            if (entry.getValue() == 0) continue;
            String txt = entry.getKey() + "(" + entry.getValue() + ")";
            float widthOfButton = dummy.computeTextWidth(txt) + 30;
            currentX += widthOfButton;
            if (currentX > widthOfRow) {
                currentX = widthOfButton;
                rows++;
                data.add(daten);
                daten = new RowData(rows, new LinkedHashMap<String, Integer>());
            }
            daten.stringsInRow.put(txt, (int) widthOfButton);
            currentX += xPadding;
        }
        data.add(daten);
        return data;
    }
    public static ArrayList<RowData> calculateAmountOfRowsIgnoreBrackets(float widthOfRow, LinkedHashMap<String, Integer> designs, float xPadding) {
        ArrayList<RowData> data = new ArrayList<>();
        float currentX = 0;
        float rows = 0;
        RowData daten = new RowData(rows, new LinkedHashMap<String, Integer>());
        LabelAPI dummy = Global.getSettings().createLabel("", Fonts.DEFAULT_SMALL);
        for (Map.Entry<String, Integer> entry : designs.entrySet()) {
            if (entry.getValue() == 0) continue;
            String txt = entry.getKey();
            float widthOfButton = dummy.computeTextWidth(txt) + 30;
            currentX += widthOfButton;
            if (currentX > widthOfRow) {
                currentX = widthOfButton;
                rows++;
                data.add(daten);
                daten = new RowData(rows, new LinkedHashMap<String, Integer>());
            }
            daten.stringsInRow.put(txt, (int) widthOfButton);
            currentX += xPadding;
        }
        data.add(daten);
        return data;
    }


}
