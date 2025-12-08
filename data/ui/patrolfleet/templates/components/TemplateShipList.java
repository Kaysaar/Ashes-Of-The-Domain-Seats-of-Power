package data.ui.patrolfleet.templates.components;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.models.PatrolShipData;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplateShipList implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel, componentPanel;
    BasePatrolFleetTemplate template;
    LinkedHashMap<String, Integer> ships;

    public LinkedHashMap<String, Integer> getShips() {
        return ships;
    }
    boolean forProduction = false;
    boolean overrideButtonEnabled = false;
    public TemplateShipList(float width, float height, BasePatrolFleetTemplate template,boolean forProduction) {
        this.template = template;
        mainPanel = Global.getSettings().createCustom(width, height, this);
        ships = new LinkedHashMap<>();
        if(template!=null){
            ships.putAll(template.assignedShipsThatShouldSpawn);
            for (PatrolShipData datum : template.data.values()) {
                if (!datum.isShipPresent()) {
                    ships.remove(datum.shipId);
                }
            }
        }
        if(template instanceof BasePatrolFleet fleet){
            if(!fleet.getShipsForReplacementWhenInPrep().isEmpty()){
                ships.clear();
                ships.putAll(fleet.getShipsForReplacementWhenInPrep());
            }
        }
        this.forProduction = forProduction;


    }
    public TemplateShipList(float width, float height, BasePatrolFleetTemplate template,boolean forProduction,boolean overrideButtonEnabled) {
        this.template = template;
        mainPanel = Global.getSettings().createCustom(width, height, this);
        ships = new LinkedHashMap<>();
        if(template!=null){
            ships.putAll(template.assignedShipsThatShouldSpawn);
            for (PatrolShipData datum : template.data.values()) {
                if (!datum.isShipPresent()) {
                    ships.remove(datum.shipId);
                }
            }
        }
        this.forProduction = forProduction;
        this.overrideButtonEnabled = overrideButtonEnabled;


    }
    public TemplateShipList(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        ships = new LinkedHashMap<>();

    }


    public void addNewShip(String id) {
        ships.compute(id, (k, v) -> v == null ? 1 : v + 1);
        createUI();
    }
    public void addNewShip(String id,int number) {
        ships.compute(id, (k, v) -> v == null ? 1 : v +number);
        createUI();
    }
    public void removeShip(String id) {
        ships.computeIfPresent(id, (k, v) -> v > 1 ? v - 1 : null);
        if (ships.get(id) != null && ships.get(id) <= 0) {
            ships.remove(id);
        }
        createUI();
    }

    public int getCountOfShips() {
        int amount = 0;
        for (Integer e : ships.values()) {
            amount += e;
        }
        return amount;
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public boolean needsToUpgradeInfo = false;

    @Override
    public void createUI() {
        if (componentPanel != null) {
            mainPanel.removeComponent(componentPanel);
        }

        float admiralShipSize = 110;
        float normalShipSize = 50;
        if (template != null&&!forProduction) {
            admiralShipSize = 130;
            normalShipSize = 60;
        }
        if(overrideButtonEnabled){
            admiralShipSize = 130;
            normalShipSize = 90;
        }
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), null);
        TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(), componentPanel.getPosition().getHeight(), template == null||forProduction);
        sortShipsByFPDescInPlace();
        LinkedHashMap<String, Integer> alreadyPlaced = new LinkedHashMap<>(ships);
        String highest = getShipWithHighestFP();
        if (highest != null) {
            TemplateShipShowcase showcase = new TemplateShipShowcase(admiralShipSize, true, highest, this,forProduction&&!overrideButtonEnabled);
            if(overrideButtonEnabled){
                showcase.renderer.setBoxColor(Color.ORANGE);
                showcase.setClickable(false);
                showcase.setOverrideHighlight(true);
                tooltip.addCustom(showcase.getComponentPanel(), 5f).getPosition().inTL(5, 5);
                alreadyPlaced.computeIfPresent(highest, (k, v) -> v > 1 ? v - 1 : null);
                float heightSoFar = 5;
                int am = 0;
                float seperatorX = 10;
                float seperatorY = 10;
                float startingY = 25;
                float startingX = admiralShipSize + seperatorX + 5;
                TemplateShipShowcase lastSavedShowcase = null;
                for (Map.Entry<String, Integer> entry : alreadyPlaced.entrySet()) {
                    for (int i = 0; i < entry.getValue(); i++) {
                        if (startingX + normalShipSize >= componentPanel.getPosition().getWidth()) {
                            lastSavedShowcase.overrideToBeShortCutForFleetView(template);
                            break;
                        }
                        TemplateShipShowcase subShowcase = new TemplateShipShowcase(normalShipSize, false, entry.getKey(), this,forProduction&&!overrideButtonEnabled);
                        subShowcase.setClickable(false);
                        subShowcase.setOverrideHighlight(true);
                        lastSavedShowcase = subShowcase;
                        tooltip.addCustom(subShowcase.getComponentPanel(), 0f).getPosition().inTL(startingX, startingY);
                        am++;
                        startingX += normalShipSize + seperatorX;

                    }

                }
                needsToUpgradeInfo = true;
                componentPanel.addUIElement(tooltip).inTL(0, 0);
                mainPanel.addComponent(componentPanel).inTL(0, 0);
                return;

            }

            showcase.setClickable(forProduction&&!overrideButtonEnabled);
            showcase.setOverrideHighlight(!overrideButtonEnabled);
            tooltip.addCustom(showcase.getComponentPanel(), 5f).getPosition().inTL(5, 5);
            alreadyPlaced.computeIfPresent(highest, (k, v) -> v > 1 ? v - 1 : null);
            float heightSoFar = 5;
            int am = 0;
            float seperatorX = 10;
            float seperatorY = 10;
            if(template!=null){
                seperatorX = 5;
            }
            float startingY = 5;
            float startingX = admiralShipSize + seperatorX + 5;
            if (forProduction) {
                seperatorX = 10;
                 startingX = admiralShipSize + seperatorX + 5;
                for (Map.Entry<String, Integer> entry : alreadyPlaced.entrySet()) {
                    for (int i = 0; i < entry.getValue(); i++) {
                        if (startingX + normalShipSize >= componentPanel.getPosition().getWidth()) {
                            startingY += seperatorY + normalShipSize;
                            if (!forProduction&&template != null && startingY + normalShipSize > componentPanel.getPosition().getHeight()) {
                                break;
                            }
                            if (startingY < admiralShipSize) {
                                startingX = admiralShipSize + seperatorX + 5;
                            } else {
                                startingX = 5;
                            }
                        }
                        TemplateShipShowcase subShowcase = new TemplateShipShowcase(normalShipSize, false, entry.getKey(), this,forProduction&&!overrideButtonEnabled);
                        subShowcase.setClickable(forProduction&&!overrideButtonEnabled);
                        subShowcase.setOverrideHighlight(!overrideButtonEnabled);
                        tooltip.addCustom(subShowcase.getComponentPanel(), 0f).getPosition().inTL(startingX, startingY);
                        am++;
                        startingX += normalShipSize + seperatorX;

                    }

                }
                tooltip.setHeightSoFar(Math.max(admiralShipSize + 5, startingY + seperatorY + normalShipSize));
            } else {
                TemplateShipShowcase lastSavedShowcase = null;
                for (Map.Entry<String, Integer> entry : alreadyPlaced.entrySet()) {
                    if (startingX + normalShipSize >= componentPanel.getPosition().getWidth()) {

                        startingY += seperatorY + normalShipSize;
                        if (template != null && startingY + normalShipSize > componentPanel.getPosition().getHeight()) {
                            if (lastSavedShowcase != null) {
                                lastSavedShowcase.overrideToBeShortCutForFleetView(template);
                                lastSavedShowcase.setClickable(true);
                                break;
                            }
                        }
                        if (startingY < admiralShipSize) {
                            startingX = admiralShipSize + seperatorX + 5;
                        } else {
                            startingX = 5;
                        }
                    }
                    TemplateShipShowcase subShowcase = new TemplateShipShowcase(normalShipSize, false, entry.getKey(), this, entry.getValue());
                    subShowcase.setClickable(false);
                    subShowcase.setOverrideHighlight(true);
                    lastSavedShowcase = subShowcase;
                    tooltip.addCustom(subShowcase.getComponentPanel(), 0f).getPosition().inTL(startingX, startingY);
                    am++;
                    startingX += normalShipSize + seperatorX;
                    if(startingX>=lastSavedX){
                        lastSavedX = startingX;
                    }
                }

            }

        }

        needsToUpgradeInfo = true;
        componentPanel.addUIElement(tooltip).inTL(0, 0);
        mainPanel.addComponent(componentPanel).inTL(0, 0);


    }

    public float lastSavedX = 0;

    public void sortShipsByFPDescInPlace() {
        if (ships == null || ships.isEmpty()) return;

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(ships.entrySet());

        entries.sort((e1, e2) -> {
            // 1) Hull size priority (Capital -> ... -> Fighter -> Default/unknown)
            int r1 = hullRank(getHullSize(e1.getKey()));
            int r2 = hullRank(getHullSize(e2.getKey()));
            if (r1 != r2) return Integer.compare(r1, r2);

            // 2) FP descending within the same hull size
            float fp1 = getHullFP(e1.getKey());
            float fp2 = getHullFP(e2.getKey());
            int cmp = Float.compare(fp2, fp1);
            if (cmp != 0) return cmp;

            // 3) Stable tie-breaker
            return e1.getKey().compareTo(e2.getKey());
        });

        LinkedHashMap<String, Integer> reordered = new LinkedHashMap<>(entries.size());
        for (Map.Entry<String, Integer> e : entries) {
            reordered.put(e.getKey(), e.getValue());
        }
        ships.clear();
        ships.putAll(reordered);
    }


    public int getTotalFleetPoints() {
        if (ships == null) return 0;
        int value = 0;
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            value += (int) (entry.getValue() * getHullFP(entry.getKey()));
        }
        return value;
    }

    public int getFleetPoints(boolean civilian) {
        if (ships == null) return 0;
        int value = 0;
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            value += (int) (entry.getValue() * getHullFP(entry.getKey()));
        }
        return value;
    }

    public String getShipWithHighestFP() {
        if (ships == null || ships.isEmpty()) return null;

        String bestId = null;
        int bestRank = Integer.MAX_VALUE; // lower is better
        float bestFp = Float.NEGATIVE_INFINITY;

        for (String id : ships.keySet()) {
            ShipAPI.HullSize size = getHullSize(id);
            int rank = hullRank(size); // CAPITAL=0, CRUISER=1, DESTROYER=2, FRIGATE=3, FIGHTER=4, DEFAULT=5
            float fp = getHullFP(id);

            if (rank < bestRank) {
                bestRank = rank;
                bestFp = fp;
                bestId = id;
            } else if (rank == bestRank) {
                if (fp > bestFp) {
                    bestFp = fp;
                    bestId = id;
                } else if (fp == bestFp) {
                    // stable tie-breaker
                    if (bestId == null || id.compareTo(bestId) < 0) {
                        bestId = id;
                    }
                }
            }
        }
        return bestId;
    }


    public float getHullFP(String hullId) {
        try {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            return spec != null ? spec.getFleetPoints() : 0f;
        } catch (Throwable t) {
            return 0f;
        }
    }
    public ShipAPI.HullSize getHullSize(String hullId) {
        try {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            return spec != null ? spec.getHullSize() : ShipAPI.HullSize.FRIGATE;
        } catch (Throwable t) {
            return ShipAPI.HullSize.DEFAULT;
        }
    }
    // Make sure you have: import com.fs.starfarer.api.combat.ShipAPI;
    private int hullRank(ShipAPI.HullSize size) {
        if (size == null) return 5;
        switch (size) {
            case CAPITAL_SHIP: return 0;
            case CRUISER:      return 1;
            case DESTROYER:    return 2;
            case FRIGATE:      return 3;
            case FIGHTER:      return 4;
            case DEFAULT:
            default:           return 5;
        }
    }

    @Override
    public void clearUI() {
        ships.clear();
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
