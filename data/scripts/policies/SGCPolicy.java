package data.scripts.policies;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.factors.SGCPolicyFactor;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import data.scripts.models.TimelineEventType;

import java.awt.*;

public class SGCPolicy extends BaseFactionPolicy {
    SGCPolicyFactor factor = new SGCPolicyFactor();
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Provides bonuses towards %s, %s and %s, scaling with presence of Patrol HQ / Military Base / High Command", 0, Misc.getPositiveHighlightColor(), "Ground defence","Stability","Fleet size").setAlignment(Alignment.MID);
        tooltip.addPara("Increases demand for supplies and heavy armaments based on colony size",  Misc.getNegativeHighlightColor(),3f).setAlignment(Alignment.MID);
        tooltip.addPara("Slows down Major Crisis progression.",  Misc.getPositiveHighlightColor(),3f).setAlignment(Alignment.MID);

    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Provides bonuses towards %s, %s and %s, scaling with presence of Patrol HQ / Military Base / High Command (with none present being treated as tier 0)", 5, Misc.getPositiveHighlightColor(), "Ground defence","Stability","Fleet size");
        tooltip.addPara(BaseIntelPlugin.BULLET+"1 + %s ground defence multiplier",3f, Color.ORANGE,"0.25 * tier");
        tooltip.addPara(BaseIntelPlugin.BULLET+"1 + %s stability ( %s )",3f,Color.ORANGE," 0.5 * tier","rounded down, so 1/1/2/2");
        tooltip.addPara(BaseIntelPlugin.BULLET+"%s + %s fleet size",3f,Color.ORANGE,"10%","15% * tier");
        tooltip.addPara("Increases demand for supplies and heavy armaments based on colony size",  Misc.getNegativeHighlightColor(),5f);
        tooltip.addPara(BaseIntelPlugin.BULLET+"1 + %s supplies (rounded down)",3f, Color.ORANGE,"0.5 * market size");
        tooltip.addPara(BaseIntelPlugin.BULLET+"%s - 1 heavy armaments (rounded down)",3f, Color.ORANGE,"0.5 * market size");

        tooltip.addPara("Slows down Major Crisis progression.",  Misc.getPositiveHighlightColor(),5f);
        super.createDetailedTooltipDescription(tooltip);
    }
    public int getTierOfMarket(MarketAPI market){
        int tier = 0;
        if(market.hasIndustry(Industries.PATROLHQ)){
            tier =1;
        }
        if(market.hasIndustry(Industries.MILITARYBASE)){
            tier = 2;
        }
        if(market.hasIndustry(Industries.HIGHCOMMAND)){
            tier = 3;
        }
        return tier;
    }

    @Override
    public void applyPolicyEffectAfterChangeInUI(boolean removing) {
        if(removing){
            if(HostileActivityEventIntel.get()!=null)

                HostileActivityEventIntel.get().removeFactor(factor);
        }

    }
    @Override
    public boolean showInUI() {
        return AoTDFactionManager.getInstance().getScriptForGoal(TimelineEventType.MILITARY).reachedGoal("goal_1");
    }  @Override
    public void applyForMarket(MarketAPI x) {
        int tier = getTierOfMarket(x);  
        if(tier>0){
            x.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(getID(),(0.25f*tier),"Service Guarantees Citizenship");
        }
        x.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(getID(),(10+(15*tier))/100f,"Service Guarantees Citizenship");
        x.getStability().modifyFlat(getID(), (float) Math.floor(1+(0.5f*tier)),"Service Guarantees Citizenship");
        x.getIndustries().stream().filter(y->y.getDemand(Commodities.SUPPLIES).getQuantity().getModifiedInt()>0).forEach(y->y.getDemand(Commodities.SUPPLIES).getQuantity().modifyFlat(getID(), (float) (1+Math.floor(x.getSize()*0.5f))));
        x.getIndustries().stream().filter(y->y.getDemand(Commodities.HAND_WEAPONS).getQuantity().getModifiedInt()>0).forEach(y->y.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyFlat(getID(), (float) (Math.floor(x.getSize()*0.5f))-1));
    }

    @Override
    public void unapplyForMarket(MarketAPI x) {
        x.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(getID());
        x.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(getID());
        x.getStability().unmodifyFlat(getID());
        x.getIndustries().stream().filter(y->y.getDemand(Commodities.SUPPLIES).getQuantity().getModifiedInt()>0).forEach(y->y.getDemand(Commodities.SUPPLIES).getQuantity().unmodifyFlat(getID()));
        x.getIndustries().stream().filter(y->y.getDemand(Commodities.HAND_WEAPONS).getQuantity().getModifiedInt()>0).forEach(y->y.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(getID()));

    }
    @Override
    public void applyPolicy() {
        if(HostileActivityEventIntel.get()!=null) {
            if(HostileActivityEventIntel.get().getFactorOfClass(SGCPolicyFactor.class)==null){
                HostileActivityEventIntel.get().addFactor(factor);
            }
        }
    }

    @Override
    public void unapplyPolicy() {

    }
}
