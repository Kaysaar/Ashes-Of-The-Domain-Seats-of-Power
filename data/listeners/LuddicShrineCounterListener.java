package data.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.econ.LuddicMajority;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.tot.misc.AoTDToolboxMisc;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class LuddicShrineCounterListener implements EconomyTickListener {
    @Override
    public void reportEconomyTick(int iterIndex) {
        for (LuddicShrineFactionCounterData value : data.values()) {
            value.marketsWithCondition.clear();
        }
        data.clear();
        for (FactionAPI factionAPI : AoTDToolboxMisc.getFactionsInEconomy()) {
            LuddicShrineFactionCounterData data = new LuddicShrineFactionCounterData(factionAPI.getId());
            data.updateMarkets();
            this.data.put(data.factionID, data);
        }
    }

    @Override
    public void reportEconomyMonthEnd() {

    }
    public float getWeightForFaction(FactionAPI faction) {
        float weightTotal =0;
        if(data.isEmpty())reportEconomyTick(-1);
        for (LuddicShrineFactionCounterData value : data.values()) {
            if(value.factionID.equals(faction.getId())) {
                weightTotal+=value.getComputedWeightFromFaction();
            }
            else{
                weightTotal-=value.getComputedWeightFromFaction()*0.1f;
            }
        }
        return weightTotal;
    }

    public class LuddicShrineFactionCounterData{
        public LinkedHashSet<String>marketsWithCondition = new LinkedHashSet<>();
        public String factionID;
        public LuddicShrineFactionCounterData(String factionID){
            this.factionID = factionID;
        }
        public void updateMarkets(){
            marketsWithCondition.clear();
            Misc.getFactionMarkets(factionID).forEach(x->{
                if(x.hasCondition(Conditions.LUDDIC_MAJORITY)&& LuddicMajority.matchesBonusConditions(x)){
                    marketsWithCondition.add(x.getId());
                }
            });

        }
        public int getComputedWeightFromFaction(){
            float weight = 0;
            for (String s : marketsWithCondition) {
               int size =  Global.getSector().getEconomy().getMarket(s).getSize();
               double wg = 5000*size;
               weight+= (float) wg;
            }
            return (int)weight;
        }

    }
    public LinkedHashMap<String,LuddicShrineFactionCounterData>data = new LinkedHashMap<>();

    public static LuddicShrineCounterListener getListenerInstance(){
        if(!Global.getSector().getListenerManager().hasListenerOfClass(LuddicShrineCounterListener.class)){
            Global.getSector().getListenerManager().addListener(new LuddicShrineCounterListener());
        }
        return Global.getSector().getListenerManager().getListeners(LuddicShrineCounterListener.class).stream().findFirst().get();
    }
}
