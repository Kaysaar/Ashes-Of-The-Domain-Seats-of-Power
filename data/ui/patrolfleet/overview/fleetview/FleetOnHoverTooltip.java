package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.info.ShipInfoGenerator;
import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static data.ui.patrolfleet.overview.fleetview.FleetButtonComponent.buttonScale;

public class FleetOnHoverTooltip implements TooltipMakerAPI.TooltipCreator {
    BasePatrolFleet fleet;
    public FleetOnHoverTooltip(BasePatrolFleet fleet) {
        this.fleet = fleet;
    }

    @Override
    public boolean isTooltipExpandable(Object tooltipParam) {
        return true;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 700;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        tooltip.setTitleOrbitronVeryLarge();
        tooltip.addTitle("Fleet - "+fleet.getNameOfFleet()).setAlignment(Alignment.MID);
        tooltip.addSectionHeading("Current composition of fleet",Alignment.MID,5f);

        CustomPanelAPI mainRow =null;
        if(!fleet.isStartedProcessOfDecom()){
            mainRow= getRowPanel(getTooltipWidth(tooltipParam),new LinkedHashMap<>(fleet.assignedShipsThatShouldSpawn));
        }
        else{
            mainRow= getRowPanel(getTooltipWidth(tooltipParam),new LinkedHashMap<>(fleet.getShipsInDecomFleet()));

        }
        tooltip.addCustom(mainRow,5f);
        if(!fleet.getShipsForReplacementWhenInPrep().isEmpty()){
            tooltip.addSectionHeading("Scheduled composition of fleet for next patrol",Alignment.MID,5f);
            mainRow = getRowPanel(getTooltipWidth(tooltipParam),new LinkedHashMap<>(fleet.getShipsForReplacementWhenInPrep()));
            tooltip.addCustom(mainRow,5f);
        }
        tooltip.addPara("Fp used by fleet %s",5f,Color.ORANGE,""+fleet.getFPTaken());
        if(!fleet.getShipsForReplacementWhenInPrep().isEmpty()){
            tooltip.addPara("Fp used by fleet after change of composition %s",5f,Color.ORANGE,""+fleet.geTotalFpTaken());
        }
        tooltip.addPara("Current status : %s",5f,Color.ORANGE,fleet.getCurrentStatus());
        if(fleet.isStartedProcessOfDecom()){
            tooltip.addPara("Days left till de-commission - %s",3f,Color.ORANGE, AshMisc.convertDaysToString(Math.round(fleet.getDaysTillSomething())));
        }
        else{
            tooltip.addPara("Current designated role : %s",3f,Color.ORANGE,BasePatrolFleet.getRole(fleet.getPatrolType()));

        }


    }

    @NotNull
    public CustomPanelAPI getRowPanel(float width,LinkedHashMap<String,Integer>ships) {
        float startX =5;
        float maxHeight = 55;
        float seperator = -7f;
        float currY = 0;
        CustomPanelAPI mainRow = Global.getSettings().createCustom(width,700,null);
        CustomPanelAPI row = Global.getSettings().createCustom(width,maxHeight,null);
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                ShipHullSpecAPI spec = Global.getSettings().getHullSpec(entry.getKey());
                int boxSize = (int) Math.floor(maxHeight*buttonScale.get(spec.getHullSize()));
                if(startX+boxSize>=width){
                    mainRow.addComponent(row).inTL(Math.max(0,(width-startX)/2),currY);
                    startX = 5;
                    currY+=maxHeight+5;
                    row = null;
                }
                if(row==null){
                    row = Global.getSettings().createCustom(width,maxHeight,null);
                }
                CustomPanelAPI ship = ShipInfoGenerator.getShipImage(spec,boxSize,null).one;
                row.addComponent(ship).inTL(startX,(maxHeight-boxSize)/2);
                startX+=boxSize+seperator;
            }
        }
        if(row!=null){
            mainRow.addComponent(row).inTL(Math.max(0,(width-startX)/2),currY);
        }
        mainRow.getPosition().setSize(width,currY+maxHeight);
        return mainRow;
    }

    public void sortShipsByFPDescInPlace(LinkedHashMap<String,Integer> ships) {
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
            case CAPITAL_SHIP:
                return 0;
            case CRUISER:
                return 1;
            case DESTROYER:
                return 2;
            case FRIGATE:
                return 3;
            case FIGHTER:
                return 4;
            case DEFAULT:
            default:
                return 5;
        }
    }
}
