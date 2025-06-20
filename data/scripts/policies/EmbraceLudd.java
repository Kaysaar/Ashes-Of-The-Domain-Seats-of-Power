package data.scripts.policies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;

import java.awt.*;

public class EmbraceLudd extends BaseFactionPolicy {
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("All markets get condition : Followers of Ludd",Misc.getPositiveHighlightColor(),0f).setAlignment(Alignment.MID);
        tooltip.addPara("Colonies with %s condition will get additional bonuses",0f, Color.ORANGE,"Luddic Majority").setAlignment(Alignment.MID);
        tooltip.addPara("Usage of AI cores and Colony items will give stability penalty",Misc.getNegativeHighlightColor(),0f).setAlignment(Alignment.MID);

        tooltip.addPara("Cancels Tri Tachyon Deal if present", Misc.getNegativeHighlightColor(),0).setAlignment(Alignment.MID);
        super.createTooltipDescription(tooltip);
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("All markets get condition : Followers of Ludd",Misc.getPositiveHighlightColor(),5f);
        tooltip.setBulletedListMode(BaseIntelPlugin.INDENT);
        tooltip.addPara("+1 Stability",Misc.getPositiveHighlightColor(),3f);
        tooltip.addPara("+5% fleet size",Misc.getPositiveHighlightColor(),3f);
        tooltip.addPara("+5% Accessibility",Misc.getPositiveHighlightColor(),3f);
        tooltip.addPara("+1 to production for all industries",Misc.getPositiveHighlightColor(),3f);
        tooltip.addPara("Bonuses double if %s condition is present",3f, Color.ORANGE,"Luddic Majority");
        tooltip.setBulletedListMode(null);

        tooltip.addPara("%s stability for each Colony Item used in market",5f, Misc.getNegativeHighlightColor(),"-2");

        tooltip.addPara("%s stability for each AI Core in Industry",5f, Misc.getNegativeHighlightColor(),"-3");
        tooltip.addPara("%s stability if AI core is administrator",3f, Misc.getNegativeHighlightColor(),"-8");
        tooltip.addPara("Cancels Tri Tachyon Deal if present", Misc.getNegativeHighlightColor(),3);

        super.createDetailedTooltipDescription(tooltip);
    }
    @Override
    public boolean showInUI() {
        return AoTDFactionManager.getInstance().getEffectiveLevel() >= 5;
    }
        @Override
    public void applyPolicy() {
        AoTDFactionManager.getMarketsUnderPlayer().forEach(x->{
            if(!x.hasCondition("aotd_followers_ludd")){
                x.addCondition("aotd_followers_ludd");
            }
        });
    }

}
