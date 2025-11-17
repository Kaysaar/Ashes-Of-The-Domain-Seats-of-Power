package data.scripts.factiongoals;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.industry.NovaExploraria;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.TimelineEventType;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchAndExplorationGoal extends BaseFactionGoal{
    public ResearchAndExplorationGoal() {
        super(TimelineEventType.RESEARCH_AND_EXPLORATION);
        this.goals.put("goal_1",100);
        this.goals.put("goal_2",400);
        this.goals.put("goal_3",700);
        this.goals.put("goal_4",1000);
        initGrantedGoals();
    }

    @Override
    public void createTooltipForSection(String id, TooltipMakerAPI tooltip) {
        float pad = 2f;
        if(id.equals("goal_1")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"Gain access to Nova Exploraria Industry - That will sent survey teams to specified star systems.", pad);
        }
        if(id.equals("goal_2")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"Nova Exploraria can now sent 3 fleets on expeditions, instead of 1", pad);
            tooltip.addPara(BaseIntelPlugin.INDENT+"Gain 400 Faction XP monthly", pad);
        }
        if(id.equals("goal_3")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"Nova Exploraria Expeditions can now perform advanced tech-mining operations across the Sector", pad);
        }
        if(id.equals("goal_4")){
            tooltip.addPara(BaseIntelPlugin.INDENT+"Increase chance of finding colony items during tech mining operations done by Nova Exploraria", pad);
            tooltip.addPara(BaseIntelPlugin.INDENT+"Unlock policy : Un-restricted exploration", pad);

            tooltip.addPara(BaseIntelPlugin.INDENT+"Gain one time 5000 Faction XP", pad);
            if(Global.getSettings().getModManager().isModEnabled("aotd_vok")){
//                tooltip.addPara(BaseIntelPlugin.INDENT+"Unlock new unique capital building : Persean University", pad);
            }
        }
    }
    @Override
    public void grantReward(String id) {
        if(grantedRewards==null)grantedRewards = new HashMap<>();

        List<MarketAPI> markets = AoTDFactionManager.getMarketsUnderPlayer();
        if(id.equals("goal_1")){
            NovaExploraria.setReachedThreshold();
        }
        else if(id.equals("goal_2")){
            AoTDFactionManager.getInstance().getXpPointsPerMonth().modifyFlat("aotd_research",400);
            if(NovaExploraria.getNova()!=null){
                NovaExploraria.getNova().getMaxAmountOfExpeditions().modifyFlat("aotd_research_goal",2);
            }

        }
        else if(id.equals("goal_3")){
            NovaExploraria.setCanDoInifniteTechmining();
            if(NovaExploraria.getNova()!=null){
                NovaExploraria.getNova().getMultiplierOfTechHunterCost().modifyMult("aotd_research_goal",0.5f);
            }
        }
        else if(id.equals("goal_4")){
            if(!grantedRewards.containsKey(id)){
                AoTDFactionManager.getInstance().addXP(5000);
            }

        }
        grantedRewards.put(id,true);

    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip) {
        tooltip.setTitleOrbitronVeryLarge();
        tooltip.addTitle(getTitle()).setAlignment(Alignment.MID);
        Color[]colors = new Color[]{Color.ORANGE,Color.ORANGE};
        tooltip.setParaFont(Fonts.ORBITRON_20AA);
        tooltip.addPara("%s / %s",5f,colors, AoTDFactionManager.getInstance().getGoalStat(type).getModifiedInt()+"",""+ AoTDFactionManager.maxPerCategory).setAlignment(Alignment.MID);
        tooltip.setParaFont(Fonts.DEFAULT_SMALL);
        tooltip.addPara("Focus on exploration and research of unknown",5f);
        tooltip.addPara("For each threshold reached, you will get one time reward, ranging from new policies to new industries", Misc.getTooltipTitleAndLightHighlightColor(),5f);
        for (Map.Entry<String, Integer> entry : goals.entrySet()) {
            tooltip.addPara(BaseIntelPlugin.BULLET+"At %s points",10f, AoTDFactionManager.getColorForEvent(type),entry.getValue()+"");
            createTooltipForSection(entry.getKey(),tooltip);
        }

    }
}
