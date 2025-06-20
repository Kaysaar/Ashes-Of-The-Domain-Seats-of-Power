package data.scripts.policies;

import ashlib.data.plugins.ui.models.ProgressBarComponent;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import data.ui.factionpolicies.DetailedFactionPolicyTooltip;

import java.awt.*;

public class ControlledBordersPolicy extends BaseFactionPolicy {
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Stops all colonies from growing",0f).setAlignment(Alignment.MID);
        tooltip.addPara("Reduces accessibility by %s",0f,Misc.getNegativeHighlightColor(),"80%").setAlignment(Alignment.MID);
        tooltip.addPara("Note : Policy once enacted, can only be removed after 365 days",Misc.getTooltipTitleAndLightHighlightColor(),0f).setAlignment(Alignment.MID);
        super.createTooltipDescription(tooltip);
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Stops all colonies from growing",5f);
        tooltip.addPara("Reduces accessibility by %s",3f,Misc.getNegativeHighlightColor(),"50%");
        tooltip.addPara("Note : Policy once enacted, can be removed after 365 days",3f);
        float progress = Math.min(1f,getDaysTillPlaced()/365);

        if(AoTDFactionManager.getInstance().doesHavePolicyEnabled(this.getSpec().getId())){
            int days = (int) (365-getDaysTillPlaced());
            String daysStr = "days";
            if(days<1){
                daysStr ="day";
            }
            if(days>0){
                tooltip.addPara("Policy can be removed in %s",5f, Color.ORANGE,days+" "+daysStr);
            }
            else{
                tooltip.addPara("Policy can now be removed",Misc.getPositiveHighlightColor(),5f);
            }

            ProgressBarComponent component = new ProgressBarComponent(DetailedFactionPolicyTooltip.width,20,progress, Misc.getDarkPlayerColor().brighter());

            tooltip.addCustom(component.getRenderingPanel(), 5f);
        }
        super.createDetailedTooltipDescription(tooltip);
    }

    @Override
    public boolean canBeRemoved() {
        return super.canBeRemoved()&&(getDaysTillPlaced()==0||getDaysTillPlaced()>=365);
    }

    @Override
    public void applyForMarket(MarketAPI x) {
        int size = x.getSize();
        int different = Misc.MAX_COLONY_SIZE - size;
        if (different > 0) {
            x.getStats().getDynamic().getMod(Stats.MAX_MARKET_SIZE).modifyFlat(getID(), -different);
            x.getAccessibilityMod().modifyFlat(getID(), -0.8f,"Controlled Borders");
        }
    }

    @Override
    public void unapplyForMarket(MarketAPI x) {
        x.getStats().getDynamic().getMod(Stats.MAX_MARKET_SIZE).unmodifyFlat(getID());
        x.getAccessibilityMod().unmodifyFlat(getID());
    }

    @Override
    public void applyPolicy() {
        super.applyPolicy();

    }

    @Override
    public void unapplyPolicy() {
        super.unapplyPolicy();

    }
}
