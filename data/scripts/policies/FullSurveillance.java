package data.scripts.policies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.fleet.MutableMarketStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCellsIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import data.scripts.models.TimelineEventType;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;

public class FullSurveillance extends BaseFactionPolicy implements EconomyTickListener {
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Every month",0f).setAlignment(Alignment.MID);
        tooltip.addPara("Reveal Luddic Path and Pirate bases, that are influencing your colonies",0f).setAlignment(Alignment.MID);
        tooltip.addPara("Lowers stability by %s",0f, Misc.getNegativeHighlightColor(),"3").setAlignment(Alignment.MID);
        super.createTooltipDescription(tooltip);
    }

    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Every month",5f);
        tooltip.addPara("Reveal Luddic Path and Pirate bases, that are influencing your colonies",3f);
        tooltip.addPara("Can reveal up to from %s to %s hostile bases each month",5f,Color.ORANGE,"1","4");
        tooltip.addPara("Lowers stability by %s",3f, Misc.getNegativeHighlightColor(),"3");
        super.createDetailedTooltipDescription(tooltip);
    }
    @Override
    public boolean showInUI() {
        return AoTDFactionManager.getInstance().getScriptForGoal(TimelineEventType.MILITARY).reachedGoal("goal_4");
    }
    @Override
    public void applyPolicyEffectAfterChangeInUI(boolean removing) {
        if(removing){
            Global.getSector().getListenerManager().removeListener(this.getClass());
        }
        else{
            if(!Global.getSector().getListenerManager().hasListenerOfClass(this.getClass())){
                Global.getSector().getListenerManager().addListener(this);
            }
        }

    }
    @Override
    public void applyForMarket(MarketAPI x) {
        x.getStability().modifyFlat(getID(),-3,"Full Surveillance");
    }

    @Override
    public void unapplyForMarket(MarketAPI x) {
        x.getStability().unmodifyFlat(getID());
    }
    @Override
    public void applyPolicy() {
    }

    @Override
    public void unapplyPolicy() {

    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        int amount =0;
        int pirateBasesAmount =0;
        int max = MathUtils.getRandomNumberInRange(1,2);
        for (MarketAPI marketAPI : AoTDFactionManager.getMarketsUnderPlayer()) {
            if(LuddicPathCellsIntel.getClosestBase(marketAPI)!=null){
                LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(marketAPI);
                if(base.isHidden()){
                    base.makeKnown();
                    base.setHidden(false);
                    amount++;
                    base.sendUpdateIfPlayerHasIntel(LuddicPathBaseIntel.DISCOVERED_PARAM,false);
                    if(amount>=max)break;
                }

            }
        }
        ArrayList<PirateBaseIntel>toKnow = new ArrayList<>();
        for (MarketAPI marketAPI : AoTDFactionManager.getMarketsUnderPlayer()) {
            for (IntelInfoPlugin intelInfoPlugin : Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class)) {
                PirateBaseIntel intel = (PirateBaseIntel) intelInfoPlugin;
                if(intel.affectsMarket(marketAPI)&&intel.isHidden()){
                    toKnow.add(intel);
                }
            }
        }
        toKnow.sort((a, b) -> {
            boolean aInPlayerSystem = hasPlayerMarketInSystem(a);
            boolean bInPlayerSystem = hasPlayerMarketInSystem(b);

            return Boolean.compare(aInPlayerSystem, bInPlayerSystem); // puts true (player system) first
        });

// Helper method

        for (PirateBaseIntel intel : toKnow) {
            intel.makeKnown();
            intel.setHidden(false);
            intel.sendUpdateIfPlayerHasIntel(PirateBaseIntel.DISCOVERED_PARAM,false);
            pirateBasesAmount++;
            if(pirateBasesAmount>=max)break;
        }
        toKnow.clear();
        Color color = new Color(0,0,0);
        if(amount<=0){
            color = Misc.getTooltipTitleAndLightHighlightColor();
        }
        else{
            color = Misc.getPositiveHighlightColor();
        }
        Global.getSector().getCampaignUI().addMessage("Discovered "+amount+" luddic bases, that are supporting local pather cells",color);

        if(pirateBasesAmount<=0){
            color = Misc.getTooltipTitleAndLightHighlightColor();
        }
        else{
            color = Misc.getPositiveHighlightColor();
        }
        Global.getSector().getCampaignUI().addMessage("Discovered "+pirateBasesAmount+" pirate bases, that are supporting local raids",color);

    }
    private boolean hasPlayerMarketInSystem(PirateBaseIntel obj) {
        if(obj.getTarget()==null)return false;
        return Global.getSector().getEconomy()
                .getMarkets(obj.getTarget().getCenter().getContainingLocation())
                .stream()
                .anyMatch(market -> market.getFaction().isPlayerFaction());
    }
}
