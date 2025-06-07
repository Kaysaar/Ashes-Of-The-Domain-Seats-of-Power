package data.scripts.policies;

import ashlib.data.plugins.ui.models.ProgressBarComponent;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import data.ui.factionpolicies.DetailedFactionPolicyTooltip;

import java.awt.*;

public class ExpandInfrastructure extends BaseFactionPolicy {
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("For first two years",0f).setAlignment(Alignment.MID);
        tooltip.addPara("Reduce production of all industries by %s",0f,Color.ORANGE,"40%").setAlignment(Alignment.MID);
        tooltip.addPara("After two years of policy being in effect",5f).setAlignment(Alignment.MID);
        tooltip.addPara("Increase accessibility by %s on all colonies",0f,Color.ORANGE,"25%").setAlignment(Alignment.MID);
        super.createTooltipDescription(tooltip);
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("For first two years",5f);

        tooltip.addPara("Reduce production of all industries by %s",0f,Color.ORANGE,"40%");
        tooltip.addPara("After two years of policy being in effect",5f);
        tooltip.addPara("Increase accessibility by %s on all colonies",0f,Color.ORANGE,"25%");
        float progress = Math.min(1f,getDaysTillPlaced()/720);
        if(AoTDFactionManager.getInstance().doesHavePolicyEnabled(this.getSpec().getId())){
            tooltip.addPara("Current progress of policy : %s",5f,Color.ORANGE,Misc.getRoundedValueMaxOneAfterDecimal(100*progress)+"%");
            ProgressBarComponent component = new ProgressBarComponent(DetailedFactionPolicyTooltip.width,20,progress, Misc.getDarkPlayerColor().brighter());

            tooltip.addCustom(component.getRenderingPanel(), 5f);
        }
        super.createDetailedTooltipDescription(tooltip);
    }

    @Override
    public void applyPolicy() {
        if(getDaysTillPlaced()<720){
            AoTDFactionManager.getMarketsUnderPlayer().forEach(x->x.getIndustries().forEach(y->y.getAllSupply().forEach(z->{
                z.getQuantity().unmodifyFlat(getID());

                int before  = z.getQuantity().getModifiedInt();
                int penalty = Math.round(before*0.4f);
                z.getQuantity().modifyFlat(getID(),-penalty,"Expand Infrastructure Policy");
            })));
        }
        else{
            AoTDFactionManager.getMarketsUnderPlayer().forEach(x->x.getIndustries().forEach(y->y.getAllSupply().forEach(z->{
                z.getQuantity().unmodifyFlat(getID());
            })));            AoTDFactionManager.getMarketsUnderPlayer().forEach(x->x.getAccessibilityMod().modifyFlat("expand_infrastructure",0.25f,"Expanded Infrastructure"));
        }
    }

    @Override
    public void unapplyPolicy() {
        AoTDFactionManager.getMarketsUnderPlayer().forEach(x->x.getIndustries().forEach(y->y.getAllSupply().forEach(z->z.getQuantity().unmodifyMult("expand_infrastructure"))));
    }
}
