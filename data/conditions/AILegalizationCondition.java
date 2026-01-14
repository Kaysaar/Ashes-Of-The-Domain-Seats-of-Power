package data.conditions;

import ashlib.data.plugins.misc.AshMisc;

public class AILegalizationCondition extends PolicyApplierCondition{
    @Override
    public void apply(String id) {
       if(market.getAdmin()!=null&&market.getAdmin().isAICore()){
           market.getStability().modifyFlat("ai_core_legal",2,"AI Legalization");
           market.getAccessibilityMod().modifyFlat("ai_core_legal",0.2f,"AI Legalization");

       }
       market.getIndustries().stream().filter(x->x.getAICoreId()!=null).forEach(x->x.getSupplyBonusFromOther().modifyFlat("ai_core_legal",1,"Ai Core Legalization"));
    }

    @Override
    public void unapply(String id) {
        market.getStability().unmodifyFlat("ai_core_legal");
        market.getAccessibilityMod().unmodifyFlat("ai_core_legal");
        market.getIndustries().forEach(x->x.getSupplyBonusFromOther().unmodifyFlat("ai_core_legal"));

    }
}
