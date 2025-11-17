package data.plugins;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import data.industry.AoTDMilitaryBase;
import data.industry.AoTDRelay;
import data.listeners.CapitalReapplyListener;
import data.listeners.ChooseCapitalListener;
import data.listeners.PatrolReportListener;
import data.listeners.timeline.*;
import data.listeners.timeline.models.FirstIncomeColonyListener;
import data.listeners.timeline.models.FirstIndustryListener;
import data.listeners.timeline.models.FirstMarketConditionListener;
import data.listeners.timeline.models.FirstSizeColonyListener;
import data.plugins.coreui.FactionTabListener;
import data.plugins.coreui.PatrolTabListener;
import data.scripts.ambition.AmbitionManager;
import data.scripts.ambition.AmbitionSpecManager;
import data.scripts.listeners.CrisisReplacer;
import data.scripts.managers.TimelineListenerManager;
import data.memory.AoTDSopMemFlags;
import data.scripts.listeners.FactionAdvance;
import data.scripts.listeners.FactionHistoryUpdateListener;
import data.scripts.listeners.FactionMonthlyUpdateListenner;
import data.scripts.managers.*;
import data.scripts.models.TimelineEventType;
import data.scripts.patrolfleet.everyframe.AoTDPatrolManagerMover;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.timelineevents.military.*;
import data.scripts.timelineevents.prosperity.*;
import data.scripts.timelineevents.research_explo.MildConditionEvent;
import data.scripts.timelineevents.special.*;
import data.scripts.timelineevents.research_explo.FirstVastRuins;
import data.scripts.timelineevents.templates.FactionExpansionEvent;
import data.scripts.timelineevents.templates.GroundDefenceModifierEvent;
import kaysaar.bmo.buildingmenu.upgradepaths.CustomUpgradePath;
import kaysaar.bmo.buildingmenu.upgradepaths.UpgradePathManager;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.LinkedHashMap;


public class AoDCapitalsModPlugin extends BaseModPlugin {
    @Override
    public void onNewGame() {
        Global.getSector().getListenerManager().addListener(new CrisisReplacer(),true);
    }

    public void onGameLoad(boolean newGame) {

        try {
            Global.getSettings().loadFont("graphics/fonts/orbitron16.fnt");
        } catch (IOException e) {

        }
        FactionPolicySpecManager.loadSpecs();
        if (!Global.getSector().hasScript(FactionAdvance.class)) {
            Global.getSector().addScript(new FactionAdvance());
        }
        if (!Global.getSector().hasScript(AoTDPatrolManagerMover.class)) {
            Global.getSector().addScript(new AoTDPatrolManagerMover());
        }
        Global.getSector().getListenerManager().addListener(new FactionMonthlyUpdateListenner(), true);
        if (!Global.getSector().getListenerManager().hasListenerOfClass(FactionHistoryUpdateListener.class)) {
            Global.getSector().getListenerManager().addListener(new FactionHistoryUpdateListener());
        }
        Global.getSector().getListenerManager().addListener(new CapitalReapplyListener(),true);
        addTransientScripts();
        TimelineListenerManager.getInstance().setNeedsResetAfterInterval(true);
        if (newGame) {
            Global.getSector().getEconomy().getMarketsCopy().forEach(x -> x.getPrimaryEntity().getMemoryWithoutUpdate().set("$aotd_was_colonized", true));
        }
        Global.getSector().getListenerManager().addListener(new ChooseCapitalListener(),true);
        Global.getSector().getListenerManager().addListener(new PatrolReportListener(),true);
        if(newGame && Global.getSettings().isDevMode()){
            AoTDFactionManager.getInstance().addXP(100000);
        }
        AoTDFactionManager.getInstance().advance(0f);
        PatrolTemplateManager.loadAllExistingTemplates();

        if(newGame){
            AoTDFactionPatrolsManager.getInstance().advanceAfterFleets(-1f);
        }
        CustomUpgradePath path = new CustomUpgradePath(1,4);
        LinkedHashMap<String, Vector2f> map = new LinkedHashMap<>();
        map.put(Industries.PATROLHQ, new Vector2f(1,0));
        map.put(Industries.MILITARYBASE, new Vector2f(0,1));
        map.put(Industries.HIGHCOMMAND, new Vector2f(0,2));
        map.put("aotd_hexagon", new Vector2f(0,3));
        path.setIndustryCoordinates(map);
        UpgradePathManager.getInstance().addNewCustomPath(path,Industries.PATROLHQ);
        Global.getSector().getListenerManager().addListener(new FactionTabListener(),true);
        Global.getSector().getListenerManager().addListener(new PatrolTabListener(),true);

        if(newGame){
            AmbitionManager.getInstance();
            AmbitionManager.getInstance().setNewGameMode(true);
            Global.getSector().getPlayerFaction().getDoctrine().setOfficerQuality(1);
        }
    }

    @Override
    public void onApplicationLoad() throws Exception {
        PatrolTemplateManager.ensureFileExists();
        AmbitionSpecManager.loadSpecs();
    }

    @Override
    public void onAboutToStartGeneratingCodex() {
        Global.getSettings().getIndustrySpec(Industries.PATROLHQ).setPluginClass(AoTDMilitaryBase.class.getName());
        Global.getSettings().getIndustrySpec(Industries.MILITARYBASE).setPluginClass(AoTDMilitaryBase.class.getName());
        Global.getSettings().getIndustrySpec(Industries.HIGHCOMMAND).setPluginClass(AoTDMilitaryBase.class.getName());
        AoTDMilitaryBase.industriesValidForBase.add(Industries.PATROLHQ);
        AoTDMilitaryBase.industriesValidForBase.add(Industries.MILITARYBASE);
        AoTDMilitaryBase.industriesValidForBase.add(Industries.HIGHCOMMAND);
        AoTDMilitaryBase.industriesValidForBase.add("aotd_hexagon");
        if(Global.getSettings().getModManager().isModEnabled("IndEvo")){
            Global.getSettings().getIndustrySpec("IndEvo_ComArray").setPluginClass(AoTDRelay.class.getName());
            Global.getSettings().getIndustrySpec("IndEvo_IntArray").setPluginClass(AoTDRelay.class.getName());
            AoTDMilitaryBase.industriesValidForBase.add("IndEvo_ComArray");
            AoTDMilitaryBase.industriesValidForBase.add("IndEvo_IntArray");

        }


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