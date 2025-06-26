package data.scripts.policies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import data.scripts.models.TimelineEventType;

import java.util.List;

public class RapidIndustrialization extends BaseFactionPolicy {
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Increase market upkeep multiplier by %s", 0f, Misc.getNegativeHighlightColor(), "1.5").setAlignment(Alignment.MID);
        tooltip.addPara("Increase build speed for all industries by %s", 0f, Misc.getPositiveHighlightColor(), "50%").setAlignment(Alignment.MID);

        super.createTooltipDescription(tooltip);
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Increase market upkeep multiplier by %s", 0f, Misc.getNegativeHighlightColor(), "1.5");
        tooltip.addPara("Increase build speed for all industries by %s", 0f, Misc.getPositiveHighlightColor(), "50%");

        super.createDetailedTooltipDescription(tooltip);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        AoTDFactionManager.getMarketsUnderPlayer().forEach(x->{
            List<Industry> industries = x.getIndustries().stream().filter(y->!y.getId().equals(Industries.POPULATION)&&(y.isUpgrading()||y.isBuilding())).toList();
            for (Industry industry : industries) {
                if(industry instanceof BaseIndustry){
                    ((BaseIndustry) industry).setBuildProgress(((BaseIndustry) industry).getBuildProgress()+ Global.getSector().getClock().convertToDays(amount));
                }
            }
        });

    }
    @Override
    public void applyForMarket(MarketAPI x) {
        x.getUpkeepMult().modifyMult(getID(), 1.5f, "Rapid industrialization");
    }

    @Override
    public void unapplyForMarket(MarketAPI x) {
        x.getUpkeepMult().unmodifyMult(getID());
    }

    @Override
    public void applyPolicy() {
    }

    @Override
    public void unapplyPolicy() {

    }

    @Override
    public boolean showInUI() {
        return AoTDFactionManager.getInstance().getScriptForGoal(TimelineEventType.PROSPERITY).reachedGoal("goal_1");
    }
}
