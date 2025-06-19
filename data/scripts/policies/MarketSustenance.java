package data.scripts.policies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.scripts.factiongoals.ProsperityGoal;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import org.lazywizard.console.Console;

import java.awt.*;

public class MarketSustenance extends BaseFactionPolicy {
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Commerce now produces %s",0f,Color.ORANGE,"Food").setAlignment(Alignment.MID);
        tooltip.addPara("Gain additional food based on accessibility",0f).setAlignment(Alignment.MID);
        tooltip.addPara("Shortage of food causes %s stability", 0f,Misc.getNegativeHighlightColor(),"-6").setAlignment(Alignment.MID);
        super.createTooltipDescription(tooltip);
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Commerce now produces %s",5f,Color.ORANGE,"Food");
        tooltip.addPara("Commerce will produce food equal to size of market",Misc.getTooltipTitleAndLightHighlightColor(),3f);
        tooltip.addPara("Gain %s food for every %s accessibility when commerce is present",5f, Color.ORANGE,"+1","50%");
        tooltip.addPara("Shortage of food causes %s stability", 3f,Misc.getNegativeHighlightColor(),"-6");
        super.createDetailedTooltipDescription(tooltip);
    }

    @Override
    public void applyPolicy() {
        AoTDFactionManager.getMarketsUnderPlayer().forEach(x->{
            if(x.hasIndustry(Industries.COMMERCE)){
                x.getIndustry(Industries.COMMERCE).getSupply(Commodities.FOOD).getQuantity().modifyFlat("market_sustenance",x.getSize(),"Market Sustenance");
                // TODO : Based on current accessibility, per 50% do +1 food | (Could not figure out how to grab current accessibility) ~Purple Nebula
            }
            if(x.getCommodityData(Commodities.FOOD).getDeficitQuantity()>0){
                x.getStability().modifyFlat("market_sustanance",-6,"Market Sustenance (Food shortages)");
            }
            /// Added an else to unmodify stability of source "market_sustanance" when the deficit has disappeared again
            /// ~Purple Nebula
            else {
                x.getStability().unmodify("market_sustanance");
            }

        });
        super.applyPolicy();
    }

    @Override
    public void unapplyPolicy() {

        AoTDFactionManager.getMarketsUnderPlayer().forEach(x->{
            if(x.hasIndustry(Industries.COMMERCE)){
                x.getIndustry(Industries.COMMERCE).getSupply(Commodities.FOOD).getQuantity().unmodify("market_sustenance");
            }
            if(x.getCommodityData(Commodities.FOOD) != null){
                x.getStability().unmodify("market_sustanance");
            }
        });

        super.unapplyPolicy();
    }
}
