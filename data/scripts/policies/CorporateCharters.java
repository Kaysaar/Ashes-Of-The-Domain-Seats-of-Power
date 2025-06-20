package data.scripts.policies;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.factiongoals.ProsperityGoal;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import data.scripts.models.TimelineEventType;

import java.awt.*;

public class CorporateCharters extends BaseFactionPolicy {
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Gain %s income boost for each monopoly owned",0f, Color.ORANGE,"10%").setAlignment(Alignment.MID);
        tooltip.addPara("Reduce stability by %s for each monopoly owned",0f, Misc.getNegativeHighlightColor(),"-1").setAlignment(Alignment.MID);
        super.createTooltipDescription(tooltip);
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {

        tooltip.addPara("Gain %s income boost for each monopoly owned",5f, Color.ORANGE,"10%");
        tooltip.addPara("Monopolies that are counted for bonus are: ",Misc.getTooltipTitleAndLightHighlightColor(),3f);
        tooltip.setBulletedListMode(BaseIntelPlugin.BULLET);
        tooltip.addPara("Food : (Control over %s of Food market shares) ",3f,Color.ORANGE,"30%");
        tooltip.addPara("Underworld : (Control over %s of Drugs and Organs market shares)",3f,Color.ORANGE,"30%");
        tooltip.addPara("Military : (Control over %s of Weapons and Hulls market shares)",3f,Color.ORANGE,"30%");
        tooltip.addPara("Consumer goods : (Control over %s of Domestic and Luxury goods market shares)",3f,Color.ORANGE,"30%");
        tooltip.setBulletedListMode(null);
        tooltip.addPara("Currently achieved monopolies : ",5f);
        tooltip.setBulletedListMode(BaseIntelPlugin.BULLET);
        ProsperityGoal.mapOfMonopolies.entrySet().stream().filter(x->{
            String[] array = x.getValue().toArray(new String[0]);
            return AoTDFactionManager.doesHaveMonopolyOverCommodities(30,array);
        }).forEach(x->tooltip.addPara("%s monopoly",3f,Color.ORANGE,x.getKey()));

        tooltip.setBulletedListMode(null);
        tooltip.addPara("Reduce stability by %s for each monopoly owned",5f, Misc.getNegativeHighlightColor(),"-1");
        super.createDetailedTooltipDescription(tooltip);
    }

    @Override
    public void applyForMarket(MarketAPI x) {

        int amountOfMonopolies = ProsperityGoal.mapOfMonopolies.entrySet().stream().filter(y->{
            String[] array = y.getValue().toArray(new String[0]);
            return AoTDFactionManager.doesHaveMonopolyOverCommodities(30,array);
        }).toList().size();
        x.getIncomeMult().modifyMult(getID(),1+(0.1f*amountOfMonopolies),"Market Sustenance");
    }


    @Override
    public void unapplyForMarket(MarketAPI x) {
        x.getIncomeMult().unmodifyMult(getID());
    }
    @Override
    public boolean showInUI() {
        return AoTDFactionManager.getInstance().getScriptForGoal(TimelineEventType.PROSPERITY).reachedGoal("goal_3");
    }

    @Override
    public void unapplyPolicy() {
        super.unapplyPolicy();
    }

}
