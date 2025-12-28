package data.scripts.economy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.ui.P;

import java.util.LinkedHashMap;
import java.util.Map;

public class AoTDIndustryData {

    public static class CommodityData {
        String id;

        /**
         * inputCommodityId -> amount required per 1 unit of this output commodity
         */
        LinkedHashMap<String, Float> demandReqPerOne = new LinkedHashMap<>();

        public CommodityData(String id, LinkedHashMap<String, Float> demandReqPerOne) {
            this.id = id;
            this.demandReqPerOne = demandReqPerOne;
        }
        public long  maxCapacity =0;

        public long getMaxCapacity() {
            return maxCapacity;
        }

        public void setMaxCapacity(long maxCapacity) {
            this.maxCapacity = maxCapacity;
        }

        public String getId() {
            return id;
        }

        public LinkedHashMap<String, Float> getDemandReqPerOne() {
            return demandReqPerOne;
        }
    }

    public static class IndustryData {
        String id;

        /**
         * outputCommodityId -> per-output commodity data (inputs per 1 output)
         */
        LinkedHashMap<String, CommodityData> baseProductionData = new LinkedHashMap<>();

        public IndustryData(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public LinkedHashMap<String, CommodityData> getBaseProductionData() {
            return baseProductionData;
        }
        public long getMaxCapacity(MarketAPI market,String commodityID){
            populateMaxCapacity(market);
            return baseProductionData.getOrDefault(commodityID,new CommodityData(commodityID,new LinkedHashMap<>())).getMaxCapacity();
        }

        public void populateMaxCapacity(MarketAPI market) {
            Industry ind = Global.getSettings().getIndustrySpec(id).getNewPluginInstance(market);
            MarketAPI copy = market.clone();
            // the copy is a shallow copy and its conditions point to the original market
            // so, make it share the suppressed conditions list, too, otherwise
            // e.g. SolarArray will suppress conditions in the original market and the copy will still apply them
            copy.setSuppressedConditions(market.getSuppressedConditions());
            copy.setRetainSuppressedConditionsSetWhenEmpty(true);
            market.setRetainSuppressedConditionsSetWhenEmpty(true);
            MarketAPI orig = market;

            //int numBeforeAdd = Misc.getNumIndustries(market);

            market = copy;
            boolean needToAddIndustry = !market.hasIndustry(getId());
            //addDialogMode = true;
            if (needToAddIndustry) market.getIndustries().add(ind);
            market.clearCommodities();
            for (CommodityOnMarketAPI curr : market.getAllCommodities()) {
                curr.getAvailableStat().setBaseValue(100);
            }
            market.reapplyConditions();
            ind.reapply();
            for (MutableCommodityQuantity mutableCommodityQuantity : ind.getAllSupply()) {
                long news = Math.round(mutableCommodityQuantity.getQuantity().getModifiedInt() * Global.getSettings().getCommoditySpec(mutableCommodityQuantity.getCommodityId()).getEconUnit() * Math.pow(10, market
                        .getSize()-3));
                baseProductionData.get(mutableCommodityQuantity.getCommodityId()).setMaxCapacity(news);
            }

            if (needToAddIndustry) {
                ind.unapply();
                market.getIndustries().remove(ind);
            }
            market = orig;
            market.setRetainSuppressedConditionsSetWhenEmpty(null);
            if (!needToAddIndustry) {
                ind.reapply();
            }
            market.reapplyConditions();

        }
        public static IndustryData getIndustryData(MarketAPI market,String industryId) {

            // Ensure the industry exists and has up-to-date stats before reading supply/demand
            if (!market.hasIndustry(industryId)) {
                market.addIndustry(industryId);
            }

            // Re-apply/recompute, otherwise supply/demand can be stale/empty depending on timing
            market.reapplyConditions();
            market.reapplyIndustries();

            Industry ind = market.getIndustry(industryId);
            if (ind == null) return null;

            // Some mods/industries need an explicit reapply
            ind.reapply();

            LinkedHashMap<String, Integer> produced = new LinkedHashMap<>();
            LinkedHashMap<String, Integer> demanded = new LinkedHashMap<>();

            for (MutableCommodityQuantity q : ind.getAllSupply()) {
                String cid = q.getCommodityId();
                int units = q.getQuantity().getModifiedInt();
                int econUnit = (int) Global.getSettings().getCommoditySpec(cid).getEconUnit();
                produced.put(cid, units * econUnit);
            }

            for (MutableCommodityQuantity q : ind.getAllDemand()) {
                String cid = q.getCommodityId();
                int units = q.getQuantity().getModifiedInt();
                int econUnit = (int) Global.getSettings().getCommoditySpec(cid).getEconUnit();
                demanded.put(cid, units * econUnit);
            }

            IndustryData out = new IndustryData(industryId);

            // For each output commodity, compute "inputs per 1 output"
            for (Map.Entry<String, Integer> prod : produced.entrySet()) {
                String producedId = prod.getKey();
                int producedAmount = prod.getValue();

                // Avoid division by zero (some industries can report 0 supply due to conditions)
                if (producedAmount <= 0) continue;

                LinkedHashMap<String, Float> req = new LinkedHashMap<>();
                for (Map.Entry<String, Integer> dem : demanded.entrySet()) {
                    String demandedId = dem.getKey();
                    int demandedAmount = dem.getValue();

                    // amount of demanded commodity needed per 1 unit of this produced commodity
                    float perOne = (float) demandedAmount / (float) producedAmount;

                    // Optional: skip true zeros
                    if (perOne > 0f) {
                        req.put(demandedId, perOne);
                    }
                }

                out.baseProductionData.put(producedId, new CommodityData(producedId, req));
            }

            return out;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("IndustryData [id=" + id + "]");
            for (CommodityData value : baseProductionData.values()) {
                builder.append("\nCommodity :" + value.id);
                for (Map.Entry<String, Float> entry : value.getDemandReqPerOne().entrySet()) {
                    builder.append("\n---Demand : " + entry.getKey() + " : " + entry.getValue());
                }
                builder.append("\nMax production capacity  : "+value.getMaxCapacity());
            }

            return builder.toString();
        }
    }


    public static IndustryData getDataFromExampleMarket(String industryID, int size){
        MarketAPI market = initalizeMarket();
        market.setSize(size);
        IndustryData data = IndustryData.getIndustryData(market,industryID);
        data.populateMaxCapacity(market);
        return data;
    }

    public static MarketAPI initalizeMarket() {
        MarketAPI marketToShowTooltip = Global.getFactory().createMarket("to_delete", "TEst", 6);
        marketToShowTooltip.addCondition(Conditions.FARMLAND_ADEQUATE);
        marketToShowTooltip.addCondition(Conditions.ORE_MODERATE);
        marketToShowTooltip.addCondition(Conditions.RARE_ORE_MODERATE);
        marketToShowTooltip.addCondition(Conditions.ORGANICS_COMMON);
        marketToShowTooltip.addCondition(Conditions.VOLATILES_DIFFUSE);

        marketToShowTooltip.addCondition("AoDFoodDemand");
        marketToShowTooltip.addCondition(Conditions.VOLATILES_DIFFUSE);
        marketToShowTooltip.addIndustry("dummy_industry");
        marketToShowTooltip.setFactionId(Global.getSector().getPlayerFaction().getId());
        marketToShowTooltip.reapplyConditions();
        marketToShowTooltip.setFreePort(true);

        for (CommodityOnMarketAPI allCommodity : marketToShowTooltip.getAllCommodities()) {
            allCommodity.getAvailableStat().addTemporaryModFlat(10000, "src", 30);
        }


        marketToShowTooltip.setUseStockpilesForShortages(true);
        return marketToShowTooltip;
    }
}
