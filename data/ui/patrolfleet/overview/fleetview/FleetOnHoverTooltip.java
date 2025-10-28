package data.ui.patrolfleet.overview.fleetview;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.patrolfleet.models.BasePatrolFleet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        tooltip.addTitle(fleet.getNameOfFleet()).setAlignment(Alignment.MID);
        tooltip.addSectionHeading("Currently assigned ships to fleet",Alignment.MID,5f);
        LinkedHashMap<String,Integer>ships =new LinkedHashMap<>();
        ships.putAll(fleet.assignedShipsThatShouldSpawn);
        float startX =5;
        float width =getTooltipWidth(tooltipParam);
        float maxHeight = 55;
        CustomPanelAPI row = Global.getSettings().createCustom(width,maxHeight,null);
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {

            }
        }

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
