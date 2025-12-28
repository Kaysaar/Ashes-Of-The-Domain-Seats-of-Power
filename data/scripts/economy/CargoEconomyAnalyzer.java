package data.scripts.economy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * CargoEconomyAnalyzer (weekly)
 *
 * - Translates vanilla supply/demand levels -> cargo flows
 * - Uses effective population scaling (effScale) to avoid 10^n blowups
 * - Applies per-commodity DEMAND_EXP so "activity goods" don't scale like population
 * - Auto-balances with supply-leaning correction (default 70/30)
 *
 * IMPORTANT: This version runs in WEEKLY units (TIME_SCALE = 4).
 * All "prod/dem/deficit" numbers printed are per week, not per month.
 */
public class CargoEconomyAnalyzer {
    private static final Logger log = Global.getLogger(CargoEconomyAnalyzer.class);

    // ============================================================
    // TIME RESOLUTION
    // ============================================================

    /** Weekly economy: 4 "weeks" per vanilla month. Multiply all flows by this. */
    public static float TIME_SCALE = 4f;

    // ============================================================
    // CORE SCALING KNOBS
    // ============================================================

    /** Compresses exponential market size growth. Typical: 0.25..0.35 */
    public static float BETA_EFFPOP = 0.30f;

    /** Reference size: effScale(size=REF_SIZE)=1.0 */
    public static int REF_SIZE = 5;

    /**
     * Global scaling for econ unit to keep numbers manageable.
     * Example: 0.01 means econUnit 2000 -> 20 cargo/level at eff=1 (before TIME_SCALE).
     */
    public static float ECON_UNIT_SCALE = 0.05f;

    // ============================================================
    // MANUAL PER-COMMODITY UNIT MULTIPLIERS (optional)
    // ============================================================

    public static final Map<String, Float> SUPPLY_UNIT_MULT = new HashMap<>();
    public static final Map<String, Float> DEMAND_UNIT_MULT = new HashMap<>();

    // ============================================================
    // DEMAND EXPONENTS + FLOOR (critical)
    // demand uses max(floor, eff^exp) instead of just eff
    // ============================================================

    public static final Map<String, Float> DEMAND_EXP = new HashMap<>();

    /** Prevents tiny markets from having near-zero demand scaling. */
    public static float DEMAND_FLOOR = 0.10f;

    static {
        // population-like
        DEMAND_EXP.put(Commodities.FOOD, 1.00f);
        DEMAND_EXP.put(Commodities.DOMESTIC_GOODS, 1.00f);
        DEMAND_EXP.put(Commodities.ORGANICS, 0.90f);

        // activity-like
        DEMAND_EXP.put(Commodities.SUPPLIES, 0.65f);
        DEMAND_EXP.put(Commodities.FUEL, 0.25f);
        DEMAND_EXP.put(Commodities.HEAVY_MACHINERY, 0.65f);

        // industrial inputs
        DEMAND_EXP.put(Commodities.METALS, 0.70f);
        DEMAND_EXP.put(Commodities.RARE_METALS, 0.70f);

        // military-ish
        DEMAND_EXP.put(Commodities.HAND_WEAPONS, 0.60f);
        DEMAND_EXP.put(Commodities.MARINES, 0.55f);

        // people support tends to not scale as hard as "pop"
        DEMAND_EXP.put(Commodities.CREW, 0.50f);

        // Optional manual multipliers (start neutral)
        SUPPLY_UNIT_MULT.put(Commodities.SUPPLIES, 1.0f);
        DEMAND_UNIT_MULT.put(Commodities.SUPPLIES, 1.0f);
        SUPPLY_UNIT_MULT.put(Commodities.FUEL, 2.0f);
    }

    // ============================================================
    // AUTO BALANCER
    // ============================================================

    private static final Map<String, Float> AUTO_SUPPLY = new HashMap<>();
    private static final Map<String, Float> AUTO_DEMAND = new HashMap<>();

    public static float AUTO_MIN = 0.60f;
    public static float AUTO_MAX = 1.80f;

    /**
     * Weekly tick => smoothing should be ~ monthlySmooth/4.
     * If you previously used ~0.20 monthly, weekly should be ~0.05.
     */
    public static float AUTO_SMOOTH = 0.05f;

    /** 0.70 => 70% correction via supply up, 30% via demand down */
    public static float AUTO_SPLIT_SUPPLY = 0.70f;

    public static float DEFAULT_TARGET_RATIO = 0.95f;

    public static final Map<String, Float> TARGET_RATIO = new HashMap<>();
    static {
        TARGET_RATIO.put(Commodities.FOOD, 0.97f);
        TARGET_RATIO.put(Commodities.SUPPLIES, 0.92f);
        TARGET_RATIO.put(Commodities.FUEL, 0.88f);
        TARGET_RATIO.put(Commodities.DOMESTIC_GOODS, 0.95f);
        TARGET_RATIO.put(Commodities.ORGANICS, 0.93f);
    }

    // ============================================================
    // ENTRY POINTS
    // ============================================================

    public static void runTestSet(boolean perMarketLog) {
        List<String> ids = Arrays.asList(
                Commodities.SUPPLIES,
                Commodities.FUEL,
                Commodities.CREW,
                Commodities.MARINES,
                Commodities.FOOD,
                Commodities.ORGANICS,
                Commodities.VOLATILES,
                Commodities.ORE,
                Commodities.RARE_ORE,
                Commodities.METALS,
                Commodities.RARE_METALS,
                Commodities.HEAVY_MACHINERY,
                Commodities.DOMESTIC_GOODS,
                Commodities.ORGANS,
                Commodities.DRUGS,
                Commodities.HAND_WEAPONS,
                Commodities.LUXURY_GOODS,
                "lobster",
                "ships"
        );
        analyzeCommodities(ids, perMarketLog);
    }

    // ============================================================
    // ANALYZER
    // ============================================================

    public static void analyzeCommodities(List<String> commodityIds, boolean perMarketLog) {
        log.info("============================================================");
        log.info(String.format(
                "CargoEconomyAnalyzer (MONTHLY) | TIME_SCALE=%.2f | ECON_UNIT_SCALE=%.4f | BETA_EFFPOP=%.3f | REF_SIZE=%d | " +
                        "DEMAND_FLOOR=%.3f | AUTO[min=%.2f max=%.2f smooth=%.3f splitSupply=%.2f] | defaultTarget=%.2f",
                TIME_SCALE, ECON_UNIT_SCALE, BETA_EFFPOP, REF_SIZE,
                DEMAND_FLOOR, AUTO_MIN, AUTO_MAX, AUTO_SMOOTH, AUTO_SPLIT_SUPPLY, DEFAULT_TARGET_RATIO
        ));
        log.info("============================================================");

        for (String commodityId : commodityIds) {
            try {
                analyzeCommodity(commodityId, perMarketLog);
            } catch (Throwable t) {
                log.warn("Analyzer failed for commodityId=" + commodityId, t);
            }
        }
    }

    private static void analyzeCommodity(String commodityId, boolean perMarketLog) {
        CommoditySpecAPI spec;
        try {
            spec = Global.getSettings().getCommoditySpec(commodityId);
        } catch (Throwable t) {
            return;
        }

        Units u = getUnits(commodityId, spec);

        // PASS 1: RAW totals (auto=1)
        Totals raw = computeTotalsForCommodity(commodityId, u, 1f, 1f, perMarketLog, "RAW");

        // Update auto using exponent-aware demand
        updateAutoSplitForCommodity(commodityId, raw.globalProd, raw.globalDem);

        float autoS = AUTO_SUPPLY.getOrDefault(commodityId, 1f);
        float autoD = AUTO_DEMAND.getOrDefault(commodityId, 1f);

        // PASS 2: BALANCED totals (apply auto)
        Totals bal = computeTotalsForCommodity(commodityId, u, autoS, autoD, perMarketLog, "BAL");

        float target = getTargetRatio(commodityId);
        double rawRatio = raw.globalProd / Math.max(1e-6, raw.globalDem);
        double balRatio = bal.globalProd / Math.max(1e-6, bal.globalDem);

        log.info("------------------------------------------------------------");
        log.info("Commodity: " + commodityId);
        log.info(String.format(
                "econUnit=%.1f | unitScale=%.4f | supplyUnit=%.3f | demandUnit=%.3f | demandExp=%.3f | demandFloor=%.3f",
                u.econUnit, ECON_UNIT_SCALE, u.supplyUnitCargo, u.demandUnitCargo, u.demandExp, DEMAND_FLOOR
        ));
        log.info(String.format(
                "AUTO targetRatio=%.3f | autoS=%.3f | autoD=%.3f",
                target, autoS, autoD
        ));
        log.info(String.format(
                "TOTAL RAW (monthly): prod=%.1f dem=%.1f deficit=%.1f surplus=%.1f ratio=%.3f",
                raw.globalProd, raw.globalDem, raw.deficit(), raw.surplus(), rawRatio
        ));
        log.info(String.format(
                "TOTAL BAL (monthly): prod=%.1f dem=%.1f deficit=%.1f surplus=%.1f ratio=%.3f",
                bal.globalProd, bal.globalDem, bal.deficit(), bal.surplus(), balRatio
        ));
        log.info("------------------------------------------------------------");
    }

    // ============================================================
    // DUMP (per-market breakdown) - uses same math as analyzer
    // ============================================================

    public static void dumpCommodityCapacities(String commodityId) {
        CommoditySpecAPI spec;
        try {
            spec = Global.getSettings().getCommoditySpec(commodityId);
        } catch (Throwable t) {
            return;
        }

        Units u = getUnits(commodityId, spec);

        float autoS = AUTO_SUPPLY.getOrDefault(commodityId, 1f);
        float autoD = AUTO_DEMAND.getOrDefault(commodityId, 1f);

        double totalProdRaw = 0, totalDemRaw = 0, totalProdBal = 0, totalDemBal = 0;

        log.info("============================================================");
        log.info("DUMP commodity=" + commodityId + " (monthly)");
        log.info(String.format(
                "econUnit=%.1f | unitScale=%.4f | supplyUnit=%.3f | demandUnit=%.3f | demandExp=%.3f | demandFloor=%.3f | autoS=%.3f | autoD=%.3f | TIME_SCALE=%.2f",
                u.econUnit, ECON_UNIT_SCALE, u.supplyUnitCargo, u.demandUnitCargo, u.demandExp, DEMAND_FLOOR, autoS, autoD, TIME_SCALE
        ));
        log.info("============================================================");

        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            CommodityOnMarketAPI data = m.getCommodityData(commodityId);
            if (data == null) continue;

            int sLvl = Math.max(0, data.getMaxSupply());
            int dLvl = Math.max(0, data.getMaxDemand());
            if (sLvl == 0 && dLvl == 0) continue;

            MarketFlow raw = computeMarketFlow(m, sLvl, dLvl, u, 1f, 1f);
            MarketFlow bal = computeMarketFlow(m, sLvl, dLvl, u, autoS, autoD);

            totalProdRaw += raw.prod;
            totalDemRaw += raw.dem;
            totalProdBal += bal.prod;
            totalDemBal += bal.dem;

            log.info(String.format(
                    "%-24s | size=%d | sLvl=%2d dLvl=%2d | eff=%.3f demScale=%.3f | RAW prod=%8.1f dem=%8.1f | BAL prod=%8.1f dem=%8.1f",
                    m.getName(), m.getSize(), sLvl, dLvl, raw.effScale, raw.demScale,
                    raw.prod, raw.dem, bal.prod, bal.dem
            ));
        }

        log.info("------------------------------------------------------------");
        log.info(String.format(
                "TOTAL RAW (monthly): prod=%.1f dem=%.1f deficit=%.1f surplus=%.1f",
                totalProdRaw, totalDemRaw,
                Math.max(0, totalDemRaw - totalProdRaw),
                Math.max(0, totalProdRaw - totalDemRaw)
        ));
        log.info(String.format(
                "TOTAL BAL (monthly): prod=%.1f dem=%.1f deficit=%.1f surplus=%.1f",
                totalProdBal, totalDemBal,
                Math.max(0, totalDemBal - totalProdBal),
                Math.max(0, totalProdBal - totalDemBal)
        ));
        log.info("============================================================");
    }

    // ============================================================
    // SHARED MATH (single source of truth)
    // ============================================================

    private static Totals computeTotalsForCommodity(String commodityId, Units u,
                                                    float autoSupply, float autoDemand,
                                                    boolean perMarketLog, String passTag) {
        double globalProd = 0.0;
        double globalDem = 0.0;

        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m == null) continue;

            CommodityOnMarketAPI data = m.getCommodityData(commodityId);
            if (data == null) continue;

            int supplyLvl = Math.max(0, data.getMaxSupply());
            int demandLvl = Math.max(0, data.getMaxDemand());
            if (supplyLvl == 0 && demandLvl == 0) continue;

            MarketFlow flow = computeMarketFlow(m, supplyLvl, demandLvl, u, autoSupply, autoDemand);
            globalProd += flow.prod;
            globalDem += flow.dem;

            if (perMarketLog) {
                log.info(String.format(
                        "%s | %s | %s (size %d) | s=%d d=%d | eff=%.3f demScale=%.3f | prod=%.1f | dem=%.1f",
                        passTag, commodityId, m.getName(), m.getSize(),
                        supplyLvl, demandLvl, flow.effScale, flow.demScale, flow.prod, flow.dem
                ));
            }
        }

        return new Totals(globalProd, globalDem);
    }

    private static MarketFlow computeMarketFlow(MarketAPI m, int sLvl, int dLvl, Units u,
                                                float autoSupply, float autoDemand) {
        double eff = effPopScale(m.getSize(), REF_SIZE, BETA_EFFPOP);

        double demScale = Math.max((double) DEMAND_FLOOR, Math.pow(eff, u.demandExp));

        // WEEKLY: multiply everything by TIME_SCALE
        double prod = sLvl * (double) u.supplyUnitCargo * eff * autoSupply * TIME_SCALE;
        double dem = dLvl * (double) u.demandUnitCargo * demScale * autoDemand * TIME_SCALE;

        return new MarketFlow(eff, demScale, prod, dem);
    }

    private static Units getUnits(String commodityId, CommoditySpecAPI spec) {
        float econUnit = spec.getEconUnit();

        float manualS = SUPPLY_UNIT_MULT.getOrDefault(commodityId, 1f);
        float manualD = DEMAND_UNIT_MULT.getOrDefault(commodityId, 1f);

        float supplyUnitCargo = econUnit * ECON_UNIT_SCALE * manualS;
        float demandUnitCargo = econUnit * ECON_UNIT_SCALE * manualD;

        float demandExp = DEMAND_EXP.getOrDefault(commodityId, 1f);

        return new Units(econUnit, supplyUnitCargo, demandUnitCargo, demandExp);
    }

    private static double effPopScale(int size, int refSize, float beta) {
        return Math.pow(10.0, (size - refSize) * (double) beta);
    }

    // ============================================================
    // AUTO SPLIT (robust)
    // ============================================================

    private static void updateAutoSplitForCommodity(String commodityId, double rawProd, double rawDem) {
        float target = getTargetRatio(commodityId);

        // No demand => keep neutral
        if (rawDem <= 1e-6) {
            AUTO_SUPPLY.put(commodityId, 1f);
            AUTO_DEMAND.put(commodityId, 1f);
            return;
        }

        double ratio = rawProd / rawDem;
        float f = clamp(AUTO_MIN, AUTO_MAX, (float) (target / Math.max(1e-6, ratio)));

        float supplyFactor = (float) Math.pow(f, AUTO_SPLIT_SUPPLY);
        float demandFactor = (float) Math.pow(f, -(1f - AUTO_SPLIT_SUPPLY));

        float prevS = AUTO_SUPPLY.getOrDefault(commodityId, 1f);
        float prevD = AUTO_DEMAND.getOrDefault(commodityId, 1f);

        float newS = lerp(prevS, supplyFactor, AUTO_SMOOTH);
        float newD = lerp(prevD, demandFactor, AUTO_SMOOTH);

        AUTO_SUPPLY.put(commodityId, newS);
        AUTO_DEMAND.put(commodityId, newD);
    }

    private static float getTargetRatio(String commodityId) {
        Float v = TARGET_RATIO.get(commodityId);
        return v != null ? v : DEFAULT_TARGET_RATIO;
    }

    // ============================================================
    // UTIL
    // ============================================================

    private static float clamp(float a, float b, float v) {
        return Math.max(a, Math.min(b, v));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // ============================================================
    // DATA TYPES
    // ============================================================

    private static final class Units {
        final float econUnit;
        final float supplyUnitCargo;
        final float demandUnitCargo;
        final float demandExp;

        Units(float econUnit, float supplyUnitCargo, float demandUnitCargo, float demandExp) {
            this.econUnit = econUnit;
            this.supplyUnitCargo = supplyUnitCargo;
            this.demandUnitCargo = demandUnitCargo;
            this.demandExp = demandExp;
        }
    }

    private static final class MarketFlow {
        final double effScale;
        final double demScale;
        final double prod;
        final double dem;

        MarketFlow(double effScale, double demScale, double prod, double dem) {
            this.effScale = effScale;
            this.demScale = demScale;
            this.prod = prod;
            this.dem = dem;
        }
    }

    private static final class Totals {
        final double globalProd;
        final double globalDem;

        Totals(double prod, double dem) {
            this.globalProd = prod;
            this.globalDem = dem;
        }

        double deficit() { return Math.max(0.0, globalDem - globalProd); }
        double surplus() { return Math.max(0.0, globalProd - globalDem); }
    }
}
