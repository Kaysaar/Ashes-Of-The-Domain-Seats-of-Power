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
import com.fs.starfarer.api.impl.campaign.intel.group.FleetGroupIntel;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.plugins.AoTDSopMisc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionPolicy;
import data.scripts.models.TimelineEventType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagedDemocracy extends BaseFactionPolicy implements EconomyTickListener {
    int months =0;
    @Override
    public void createTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Increase defence multiplier by %s",0f, Color.ORANGE,"1.5").setAlignment(Alignment.MID);
        tooltip.addPara("Increase fleet size by %s",0f,Color.ORANGE,"50%").setAlignment(Alignment.MID);
        tooltip.addPara("Increase stability by %s",0f,Color.ORANGE,"2").setAlignment(Alignment.MID);
        tooltip.addPara("Decrease income of market by %s",0f, Misc.getNegativeHighlightColor(),"35%").setAlignment(Alignment.MID);
        tooltip.addPara("Every three months a major order is issued to hunt down either pirates or luddites",0f).setAlignment(Alignment.MID);

        super.createTooltipDescription(tooltip);
    }
    @Override
    public boolean showInUI() {
        return AoTDFactionManager.getInstance().getScriptForGoal(TimelineEventType.MILITARY).reachedGoal("goal_2");
    }
    @Override
    public void createDetailedTooltipDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Increase defence multiplier by %s",5f, Color.ORANGE,"1.5");
        tooltip.addPara("Increase fleet size by %s",3f,Color.ORANGE,"50%");
        tooltip.addPara("Increase stability by %s",3f,Color.ORANGE,"2");
        tooltip.addPara("Decrease income of market by %s",3f,  Misc.getNegativeHighlightColor(),"35%");
        tooltip.addPara("Every three months a major order is issued to hunt down either pirates or luddites",3f);
        tooltip.addPara("A sizeable fleet will spawn in random market, that their task will be destroying nearby luddic or pirate base, that is disturbing colony operations",Misc.getTooltipTitleAndLightHighlightColor(),5f);
        tooltip.addPara("If none of bases is discovered, then major order won't be issued",Misc.getTooltipTitleAndLightHighlightColor(),3f);
        super.createDetailedTooltipDescription(tooltip);
    }

    @Override
    public void applyPolicyEffectAfterChangeInUI(boolean removing) {
        if(!removing){
            Global.getSector().getListenerManager().addListener(this);
        }
        else{
            Global.getSector().getListenerManager().removeListener(this);
        }
    }

    @Override
    public void applyPolicy() {
        List<MarketAPI> markets = AoTDFactionManager.getMarketsUnderPlayer();
        markets.forEach(x->{

        });

    }

    @Override
    public void applyForMarket(MarketAPI x) {
        MutableMarketStatsAPI marketStats =x.getStats();
        marketStats.getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMultAlways(getID(),1.5f,"Super Defences (Managed Democracy)");
        marketStats.getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(getID(),0.50f,"Super Fleets (Managed Democracy)");
        x.getIncomeMult().modifyMult(getID(),0.65f,"Super Spending (Managed Democracy)");
        x.getStability().modifyFlat(getID(),2,"Absolute Liberty (Managed Democracy)");
    }

    @Override
    public void unapplyForMarket(MarketAPI x) {
        MutableMarketStatsAPI marketStats =x.getStats();
        marketStats.getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getID());
        marketStats.getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(getID());;
        x.getIncomeMult().unmodifyMult(getID());
        x.getStability().unmodify(getID());
    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        months++;
        if(months>=3){
            months =0;
            ArrayList<PirateBaseIntel>intelss = new ArrayList<>();
            Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class).stream().filter(x -> !x.isHidden()).toList().forEach(x->intelss.add((PirateBaseIntel) x));
            ArrayList<IntelInfoPlugin> infos = new ArrayList<>(  intelss.stream().filter(this::hasPlayerMarketInSystem).toList());

            for (MarketAPI marketAPI : AoTDFactionManager.getMarketsUnderPlayer()) {
                if(LuddicPathCellsIntel.getClosestBase(marketAPI)!=null){
                    LuddicPathBaseIntel base = LuddicPathCellsIntel.getClosestBase(marketAPI);
                    if(!base.isHidden()&&!base.getMarket().getMemoryWithoutUpdate().is("$aotd_chosen_for_raid",true)){
                        infos.add(base);
                    }

                }
            }
            Collections.shuffle(infos);
            if(!infos.isEmpty()){
                IntelInfoPlugin info = infos.get(0);
                if(info instanceof PirateBaseIntel){
                    PirateBaseIntel intels = (PirateBaseIntel) info;
                    if(!intels.isHidden()&&!intels.getMarket().getMemoryWithoutUpdate().is("$aotd_chosen_for_raid",true)){
                        AoTDSopMisc.startAttack(AoTDFactionManager.getMarketsUnderPlayer().get(0), intels.getMarket(), intels.getMarket().getStarSystem(), Misc.random, new FleetGroupIntel.FGIEventListener() {
                            @Override
                            public void reportFGIAborted(FleetGroupIntel intel) {
                                intels.getMarket().getMemoryWithoutUpdate().unset("$aotd_chosen_for_raid");
                            }
                        },false);
                        return;
                    }
                }
                if(info instanceof LuddicPathBaseIntel){
                    LuddicPathBaseIntel intels = (LuddicPathBaseIntel) info;
                    if(!intels.isHidden()&&!intels.getMarket().getMemoryWithoutUpdate().is("$aotd_chosen_for_raid",true)){
                        AoTDSopMisc.startAttack(AoTDFactionManager.getMarketsUnderPlayer().get(0), intels.getMarket(), intels.getMarket().getStarSystem(), Misc.random, new FleetGroupIntel.FGIEventListener() {
                            @Override
                            public void reportFGIAborted(FleetGroupIntel intel) {
                                intels.getMarket().getMemoryWithoutUpdate().unset("$aotd_chosen_for_raid");
                            }
                        },true);
                        return;
                    }
                }

            }
            else{
                infos.addAll(intelss);
                Collections.shuffle(infos);
                if(!infos.isEmpty()){
                    IntelInfoPlugin info = infos.get(0);
                    if(info instanceof PirateBaseIntel){
                        PirateBaseIntel intels = (PirateBaseIntel) info;
                        if(!intels.isHidden()&&!intels.getMarket().getMemoryWithoutUpdate().is("$aotd_chosen_for_raid",true)){
                            AoTDSopMisc.startAttack(AoTDFactionManager.getMarketsUnderPlayer().get(0), intels.getMarket(), intels.getMarket().getStarSystem(), Misc.random, new FleetGroupIntel.FGIEventListener() {
                                @Override
                                public void reportFGIAborted(FleetGroupIntel intel) {
                                    intels.getMarket().getMemoryWithoutUpdate().unset("$aotd_chosen_for_raid");
                                }
                            },false);
                            return;
                        }
                    }
            }

        }
            }


    }
    private boolean hasPlayerMarketInSystem(PirateBaseIntel obj) {
        if(obj.getTarget()==null)return false;
        return Global.getSector().getEconomy()
                .getMarkets(obj.getTarget().getCenter().getContainingLocation())
                .stream()
                .anyMatch(market -> market.getFaction().isPlayerFaction());
    }
}
