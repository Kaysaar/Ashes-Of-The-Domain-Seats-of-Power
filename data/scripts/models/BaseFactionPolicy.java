package data.scripts.models;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.managers.FactionPolicySpecManager;

public  class BaseFactionPolicy {
    String specId;
    float daysTillPlaced = 0f;

    public float getDaysTillPlaced() {
        return daysTillPlaced;
    }

    public String getID(){
        return specId;
    }
    public void setSpecId(String specId) {
        this.specId = specId;
    }

    public void createTooltipDescription(TooltipMakerAPI tooltip){
        if(!getSpec().canBeRemoved()){
            tooltip.addPara("This policy can't be removed once in effect!",Misc.getNegativeHighlightColor(),3f).setAlignment(Alignment.MID);
        }
    }
    public  void applyForMarket(MarketAPI x){

    }
    public void unapplyForMarket(MarketAPI x){

    }
    public boolean canBeRemoved(){
        if(!getSpec().canBeRemoved()){
            return !AoTDFactionManager.getInstance().doesHavePolicyEnabled(specId);
        }
        return true;
    }
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip){
        if(!getSpec().canBeRemoved()){
            tooltip.addPara("This policy can't be removed once in effect!",Misc.getNegativeHighlightColor(),3f);
        }
    }


    public FactionPolicySpec getSpec(){
        return FactionPolicySpecManager.getSpec(this.specId);
    }
    public void advance(float amount){
        daysTillPlaced += Global.getSector().getClock().convertToDays(amount);

    }
    public void applyPolicyEffectAfterChangeInUI(boolean removing){


    }
    public void applyPolicy(){

    }
    public void unapplyPolicy(){

    }
    public boolean showInUI(){
        return true;
    }
    public boolean canRemovePolicy(){
        return true;
    }
    public boolean canUsePolicy(){
        return true;
    }
}
