package data.scripts.patrolfleet.utilis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;

import java.util.LinkedHashMap;
import java.util.Map;


public class TemplateUtilis {
    public static final float FUEL_PER_FUEL_FP     = 120.0f;
    public static final float CARGO_PER_SUPPLY_FP  = 90f;
    public static final int numOfShipsPerFleet = 30;
    public static int reqFuelFPScaled(int combatFP) {
        double A = FleetPointUtilis.valuesFP.get(FleetFactory.PatrolType.FAST);
        double B = FleetPointUtilis.valuesFP.get(FleetFactory.PatrolType.COMBAT);
        double C = FleetPointUtilis.valuesFP.get(FleetFactory.PatrolType.HEAVY);

        // targets at the anchors (change if you want different anchor values)
        double F_A = 0.0, F_B = 5.0, F_C = 10.0;

        double raw;
        if (combatFP <= A) {
            raw = F_A;
        } else if (combatFP <= B) {
            raw = lerp(F_A, F_B, (combatFP - A) / Math.max(1e-9, (B - A)));
        } else if (combatFP <= C) {
            raw = lerp(F_B, F_C, (combatFP - B) / Math.max(1e-9, (C - B)));
        } else {
            // continue with the last segment's slope beyond HEAVY
            double slope = (F_C - F_B) / Math.max(1e-9, (C - B));
            raw = F_C + slope * (combatFP - C);
        }
        return (int) Math.ceil(Math.max(0.0, raw));
    }

    public static int reqSupplyFPScaled(int combatFP) {
        double A = FleetPointUtilis.valuesFP.get(FleetFactory.PatrolType.FAST);
        double B = FleetPointUtilis. valuesFP.get(FleetFactory.PatrolType.COMBAT);
        double C = FleetPointUtilis. valuesFP.get(FleetFactory.PatrolType.HEAVY);

        // targets at the anchors
        double S_A = 0.0, S_B = 0.0, S_C = 10.0;

        double raw;
        if (combatFP <= B) {
            // flat 0 from A..B (and below A)
            raw = S_A; // == S_B == 0
        } else if (combatFP <= C) {
            raw = lerp(S_B, S_C, (combatFP - B) / Math.max(1e-9, (C - B)));
        } else {
            double slope = (S_C - S_B) / Math.max(1e-9, (C - B));
            raw = S_C + slope * (combatFP - C);
        }
        return (int) Math.ceil(Math.max(0.0, raw));
    }

    private static double lerp(double a, double b, double t) {
        if (t < 0) t = 0; else if (t > 1) t = 1;
        return a + (b - a) * t;
    }

    public static int requiredFuelCapacity(int combatFP) {
        return (int) Math.ceil(reqFuelFPScaled(combatFP) * FUEL_PER_FUEL_FP);
    }

    public static int requiredCargoCapacity(int combatFP) {
        return (int) Math.ceil(reqFuelFPScaled(combatFP) * CARGO_PER_SUPPLY_FP);
    }
    public static double getTotalFuelFPFromTemplate(LinkedHashMap<String,Integer> ships){
        double total = 0.0;
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(entry.getKey());
            total += entry.getValue() * fuelFPFromShip(spec);
        }
        return total;
    }

    public static double getTotalSupplyFPFromTemplate(LinkedHashMap<String,Integer> ships){
        double total = 0.0;
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(entry.getKey());
            total += entry.getValue() * supplyFPFromShip(spec);
        }
        return total;
    }

    // If you want to score a ship's contribution:
    static float fuelFPFromShip(ShipHullSpecAPI s) {
        if (!s.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN)) return (s.getCargo() / FUEL_PER_FUEL_FP)*0.3f;
        return s.getFuel() / FUEL_PER_FUEL_FP;
    }

    static float supplyFPFromShip(ShipHullSpecAPI s) {
        if (!s.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN))return (s.getCargo() / CARGO_PER_SUPPLY_FP)*0.3f;
        return s.getCargo() / CARGO_PER_SUPPLY_FP;
    }
}
