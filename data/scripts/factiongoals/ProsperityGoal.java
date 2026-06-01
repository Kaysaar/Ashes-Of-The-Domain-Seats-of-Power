package data.scripts.factiongoals;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.TimelineEventType;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ProsperityGoal extends BaseFactionGoal{
    public static HashMap<String, HashSet<String>>mapOfMonopolies = new HashMap<>();
    static {
        HashSet<String> military = new HashSet<>();
        military.add(Commodities.HAND_WEAPONS);
        military.add(Commodities.SHIPS);
        mapOfMonopolies.put("Military",military);

        military = new HashSet<>();
        military.add(Commodities.FOOD);
        mapOfMonopolies.put("Food",military);

        military = new HashSet<>();
        military.add(Commodities.DRUGS);
        military.add(Commodities.ORGANS);
        mapOfMonopolies.put("Underworld",military);


        military = new HashSet<>();
        military.add(Commodities.DOMESTIC_GOODS);
        military.add(Commodities.LUXURY_GOODS);
        mapOfMonopolies.put("Consumer goods",military);
    }
    public ProsperityGoal() {
        super(TimelineEventType.PROSPERITY);
        this.goals.put("goal_1",100);
        this.goals.put("goal_2",400);
        this.goals.put("goal_3",700);
        this.goals.put("goal_4",1000);
        initGrantedGoals();

    }

    @Override
    public void grantReward(String id) {
        if(grantedRewards==null)grantedRewards = new HashMap<>();

        List<MarketAPI> markets = AoTDFactionManager.getMarketsUnderPlayer();
        if(id.equals("goal_1")){
            markets.forEach(x->x.getStability().modifyFlat("aotd_prosperity_1",1,"Reached Property Threshold"));
        }
        else if(id.equals("goal_2")){
            AoTDFactionManager.getInstance().getXpPointsPerMonth().modifyFlat("aotd_prosperity",200);

        }
        else if(id.equals("goal_3")){
            Global.getSector().getMemoryWithoutUpdate().set("$aotd_logistic_unlocked",true);
        }
        else if(id.equals("goal_4")){
            if(AoTDFactionManager.doesHaveMonopolyOverCommodities(30, Commodities.FOOD)){
                markets.forEach(x->x.getStability().modifyFlat("aotd_monopoly_food",2,"Monopoly over Food"));
            }
            else{
                markets.forEach(x->{x.getStability().unmodifyFlat("aotd_monopoly_food");});
            }
            if(AoTDFactionManager.doesHaveMonopolyOverCommodities(30, Commodities.DRUGS,Commodities.ORGANS)){
                Global.getSector().getPlayerStats().getDynamic().getStat(
                        Stats.getCommodityExportCreditsMultId(Commodities.DRUGS)).modifyMult("aotd_monopoly_underworld", 1f + 0.5f,"Concierge of Crime");
                Global.getSector().getPlayerStats().getDynamic().getStat(
                        Stats.getCommodityExportCreditsMultId(Commodities.ORGANS)).modifyMult("aotd_monopoly_underworld", 1f + 0.5f,"Concierge of Crime");
            }
            else{
                Global.getSector().getPlayerStats().getDynamic().getStat(
                        Stats.getCommodityExportCreditsMultId(Commodities.DRUGS)).unmodifyMult("aotd_monopoly_underworld");
                Global.getSector().getPlayerStats().getDynamic().getStat(
                        Stats.getCommodityExportCreditsMultId(Commodities.ORGANS)).unmodifyMult("aotd_monopoly_underworld");
            }
            if(!grantedRewards.containsKey(id)){
                AoTDFactionManager.getInstance().addXP(7000);
            }

        }
        grantedRewards.put(id,true);
    }

    @Override
    public void createTooltipForSection(String id, TooltipMakerAPI tooltip) {
        float pad = 2f;
        if(id.equals("goal_1")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"+1 Stability on all worlds.", pad);
            tooltip.addPara(BaseIntelPlugin.INDENT+"Unlock policy : Rapid Industrialisation", pad);
        }
        if(id.equals("goal_2")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"Gain 200 Faction XP monthly", pad);
        }
        if(id.equals("goal_3")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"Unlock policy : Corporate Chatters", pad);
        }
        if(id.equals("goal_4")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"For each monopoly you own (Underworld, Agricultural, Military and Consumer Goods) gain distinct bonuses", pad);
            tooltip.addPara(BaseIntelPlugin.INDENT+"Food : (Control over %s of Food market shares) : %s",pad,Color.ORANGE,"30%","Increase stability by 2");
            tooltip.addPara(BaseIntelPlugin.INDENT+"Underworld : (Control over %s of Drugs and Organs market shares) : %s",pad,Color.ORANGE,"30%","Increase income from those exports by 50%");
            tooltip.addPara(BaseIntelPlugin.INDENT+"Military : (Control over %s of Heavy Armaments, Marines, and Ship Hulls market shares) : %s",pad,Color.ORANGE,"30%","Increase fleet size by 50% on all markets");
            tooltip.addPara(BaseIntelPlugin.INDENT+"Consumer goods : (Control over %s of Domestic and Luxury goods market shares) : %s",pad,Color.ORANGE,"30%","Increase accessibility by 30%");


            tooltip.addPara(BaseIntelPlugin.INDENT+"Gain one time 7000 Faction XP", pad);
        }
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip) {
        tooltip.setTitleOrbitronVeryLarge();
        tooltip.addTitle(getTitle()).setAlignment(Alignment.MID);
        Color[]colors = new Color[]{Color.ORANGE,Color.ORANGE};
        tooltip.setParaFont(Fonts.ORBITRON_20AA);
        tooltip.addPara("%s / %s",5f,colors, AoTDFactionManager.getInstance().getGoalStat(type).getModifiedInt()+"",""+ AoTDFactionManager.maxPerCategory).setAlignment(Alignment.MID);
        tooltip.setParaFont(Fonts.DEFAULT_SMALL);
        tooltip.addPara("Focus on production and income of faction",5f);
        tooltip.addPara("For each threshold reached, you will get one time reward, ranging from new policies to new industries", Misc.getTooltipTitleAndLightHighlightColor(),5f);
        for (Map.Entry<String, Integer> entry : goals.entrySet()) {
            tooltip.addPara(BaseIntelPlugin.BULLET+"At %s points",10f, AoTDFactionManager.getColorForEvent(type),entry.getValue()+"");
            createTooltipForSection(entry.getKey(),tooltip);
        }

    }
}
