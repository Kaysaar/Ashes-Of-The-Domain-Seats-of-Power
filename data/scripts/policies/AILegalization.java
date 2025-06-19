package data.scripts.policies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import org.lazywizard.console.Console;

import java.awt.*;
import java.util.ArrayList;

public class AILegalization extends BaseFactionPolicy {
    public static ArrayList<String>factionsThatMadeItIllegal = new ArrayList<>();
    static {
        factionsThatMadeItIllegal.add(Factions.HEGEMONY);
        factionsThatMadeItIllegal.add(Factions.LUDDIC_CHURCH);
        factionsThatMadeItIllegal.add(Factions.LUDDIC_PATH);
    }
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Increase production by %s for each industry with AI core", 0f, Color.ORANGE,"1").setAlignment(Alignment.MID);
        tooltip.addPara("Increase market's stability by %s and income by %s with AI core admin", 3f, Color.ORANGE,"2","30%").setAlignment(Alignment.MID);
        tooltip.addPara("All factions that forbid AI use will declare permanent hostility.",Misc.getNegativeHighlightColor(),3f).setAlignment(Alignment.MID);
        tooltip.addPara("Significantly speeds up Colony Crisis",Misc.getNegativeHighlightColor(),3f).setAlignment(Alignment.MID);
        super.createTooltipDescription(tooltip);

    }

    @Override
    public void applyPolicy() {
        for (String string : factionsThatMadeItIllegal) {
            Global.getSector().getFaction(string).setRelationship(Factions.PLAYER,-100);
        }
        ///  applyPolicy was missing the entire functionality of AI Legalization except for making the player hostile
        ///  to all AI illegal factions ~Purple Nebula
        AoTDFactionManager.getMarketsUnderPlayer().forEach(x-> {
            x.getIndustries().forEach( i-> {
                if (i.getAICoreId() != null) {
                    for (MutableCommodityQuantity mcq : i.getAllSupply()) {
                        mcq.getQuantity().modifyFlat("AI_Legalization",1f,"AI Legalization (AI Core Assigned");
                    }
                }
                else {
                    for (MutableCommodityQuantity mcq : i.getAllSupply()) {
                        mcq.getQuantity().unmodify("AI_Legalization");
                    }
                }
            });
            if (x.getAdmin().isAICore()) {
                x.getStability().modifyFlat("AI_Legalization",2f,"AI Legalization (AI Core Admin");
                x.getIncomeMult().modifyPercent("AI_Legalization",30f,"AI Legalization (AI Core Admin");
            }
            else {
                if (!x.getStability().isUnmodified()) x.getStability().unmodify("AI_Legalization");
                if (!x.getIncomeMult().isUnmodified()) x.getIncomeMult().unmodify("AI_Legalization");

            }
        });
    }

    @Override
    public boolean showInUI() {
        return AoTDFactionManager.getInstance().getEffectiveLevel()>=5;
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Increase production by %s for each industry with AI core", 5f, Color.ORANGE,"1");
        tooltip.addPara("Increase market's stability by %s and income by %s with AI core admin", 3f, Color.ORANGE,"2","30%");
        tooltip.addPara("All factions that forbid AI use will declare permanent hostility.",Misc.getNegativeHighlightColor(),3f);
        for (String string : factionsThatMadeItIllegal) {
            FactionAPI factionAPI = Global.getSector().getFaction(string);
            tooltip.addPara(BaseIntelPlugin.BULLET+factionAPI.getDisplayName(),Color.ORANGE,3f);
        }
        tooltip.addPara("Significantly speeds up Colony Crisis",Misc.getNegativeHighlightColor(),3f);
        tooltip.addPara("Speed up is calculated by amount of un-resolved colony crisis with factions, that despises AI",Misc.getTooltipTitleAndLightHighlightColor(),5f);



        super.createDetailedTooltipDescription(tooltip);
    }
}
