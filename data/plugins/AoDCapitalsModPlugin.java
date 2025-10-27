package data.plugins;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import data.industry.AoTDMilitaryBase;
import data.listeners.ChooseCapitalListener;
import data.listeners.timeline.*;
import data.listeners.timeline.models.FirstIncomeColonyListener;
import data.listeners.timeline.models.FirstIndustryListener;
import data.listeners.timeline.models.FirstMarketConditionListener;
import data.listeners.timeline.models.FirstSizeColonyListener;
import data.scripts.listeners.CrisisReplacer;
import data.scripts.managers.TimelineListenerManager;
import data.memory.AoTDSopMemFlags;
import data.scripts.CoreUITrackerScript;
import data.scripts.listeners.FactionAdvance;
import data.scripts.listeners.FactionHistoryUpdateListener;
import data.scripts.listeners.FactionMonthlyUpdateListenner;
import data.scripts.managers.*;
import data.scripts.models.TimelineEventType;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.timelineevents.military.*;
import data.scripts.timelineevents.prosperity.*;
import data.scripts.timelineevents.research_explo.MildConditionEvent;
import data.scripts.timelineevents.special.*;
import data.scripts.timelineevents.research_explo.FirstVastRuins;
import data.scripts.timelineevents.templates.FactionExpansionEvent;
import data.scripts.timelineevents.templates.GroundDefenceModifierEvent;


public class AoDCapitalsModPlugin extends BaseModPlugin {
    @Override
    public void onNewGame() {
        Global.getSector().getListenerManager().addListener(new CrisisReplacer(),true);
    }

    public void onGameLoad(boolean newGame) {
        if (!Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
            Global.getSector().addTransientScript(new CoreUITrackerScript());
        }
        FactionPolicySpecManager.loadSpecs();
        if (!Global.getSector().hasScript(FactionAdvance.class)) {
            Global.getSector().addScript(new FactionAdvance());
        }
        Global.getSector().getListenerManager().addListener(new FactionMonthlyUpdateListenner(), true);
        if (!Global.getSector().getListenerManager().hasListenerOfClass(FactionHistoryUpdateListener.class)) {
            Global.getSector().getListenerManager().addListener(new FactionHistoryUpdateListener());
        }
        addTransientScripts();
        TimelineListenerManager.getInstance().setNeedsResetAfterInterval(true);
        if (newGame) {
            Global.getSector().getEconomy().getMarketsCopy().forEach(x -> x.getPrimaryEntity().getMemoryWithoutUpdate().set("$aotd_was_colonized", true));
        }
        Global.getSector().getListenerManager().addListener(new ChooseCapitalListener(),true);
        if(newGame && Global.getSettings().isDevMode()){
            AoTDFactionManager.getInstance().addXP(100000);
        }
        AoTDFactionManager.getInstance().advance(0f);


        PatrolTemplateManager.loadAllExistingTemplates();



    }

    @Override
    public void onAboutToStartGeneratingCodex() {
        Global.getSettings().getIndustrySpec(Industries.PATROLHQ).setPluginClass(AoTDMilitaryBase.class.getName());
        Global.getSettings().getIndustrySpec(Industries.MILITARYBASE).setPluginClass(AoTDMilitaryBase.class.getName());
        Global.getSettings().getIndustrySpec(Industries.HIGHCOMMAND).setPluginClass(AoTDMilitaryBase.class.getName());
    }

    public void addTransientScripts() {
        TimelineListenerManager.getInstance().addNewListener(new FirstColonyListener(AoTDSopMemFlags.FIRST_COLONY_TIMELINE_FLAG));
        TimelineListenerManager.getInstance().addNewListener(new GateHaulerWitness(AoTDSopMemFlags.GATE_ARRIVAL_WITNESS));
        TimelineListenerManager.getInstance().addNewListener(new ParadiseColonyListener(AoTDSopMemFlags.CLASS_V_COLONY));
        for (int i = 6; i <= 10; i++) {
            TimelineListenerManager.getInstance().addNewListener(new FirstSizeColonyListener(AoTDSopMemFlags.SIZE_FLAG_COLONY, i, i - 5));
        }
        TimelineListenerManager.getInstance().addNewListener(new FirstIncomeColonyListener(AoTDSopMemFlags.REACHED_INCOME, 100000, 1));
        TimelineListenerManager.getInstance().addNewListener(new FirstIncomeColonyListener(AoTDSopMemFlags.REACHED_INCOME, 1000000, 2));
        TimelineListenerManager.getInstance().addNewListener(new FirstIncomeColonyListener(AoTDSopMemFlags.REACHED_INCOME, 10000000, 3));

        TimelineListenerManager.getInstance().addNewListener(new FirstMarketConditionListener(AoTDSopMemFlags.MARKET_CONDITION_COLONIZED, Conditions.RUINS_VAST, new FirstVastRuins(), false));
        TimelineListenerManager.getInstance().addNewListener(new FirstMarketConditionListener(AoTDSopMemFlags.MARKET_CONDITION_COLONIZED,Conditions.MILD_CLIMATE,new MildConditionEvent(),false));
        TimelineListenerManager.getInstance().addNewListener(new FirstMarketConditionListener(AoTDSopMemFlags.MARKET_CONDITION_COLONIZED,Conditions.SOLAR_ARRAY,new OrbitalShadeEvent(),false));

        Global.getSector().getListenerManager().addListener(new ParadiseColonyListenerEnforcer(), true);


        TimelineListenerManager.getInstance().addNewListener(new VastRuinsScouredEventListener(AoTDSopMemFlags.VAST_RUINS_DEPLETED));
        TimelineListenerManager.getInstance().addNewListener(new FirstIndustryListener(AoTDSopMemFlags.FIRST_INDUSTRY,new FirstPlanetaryShieldEvent(null)));
        TimelineListenerManager.getInstance().addNewListener(new FirstIndustryListener(AoTDSopMemFlags.FIRST_INDUSTRY,new FirstHighCommand(null)));

        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new HegemonyInspectionDefeat()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new TriTachyonFendingOffAttacks()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new LuddicChurchDefeat()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new LuddicPathDefeat()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new DefeatingPerseanLeague()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new TriTachyonDealEvent()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new SindiranDiktatDefeat()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new MostWanted()));

        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.FIRST_ITEM,new HypershuntInstallEvent()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.FIRST_ITEM,new PristineNanoforgeEvent()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new FirstFourIndustries()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new TwelveSturcutresEvent()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new FoodMonopolyEvent(TimelineEventType.PROSPERITY,"food")));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new UnderworldMonopolyEvent()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new MilitaryMonopolyEvent()));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new ConsumerGoodsMonopolyEvent()));


        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new GroundDefenceModifierEvent(1000,1)));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new GroundDefenceModifierEvent(10000,2)));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new GroundDefenceModifierEvent(50000,3)));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new GroundDefenceModifierEvent(100000,4)));

        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new FactionExpansionEvent(10,1)));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new FactionExpansionEvent(20,2)));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new FactionExpansionEvent(40,3)));
        TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new FactionExpansionEvent(80,4)));


    }
}