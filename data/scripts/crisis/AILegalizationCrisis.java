package data.scripts.crisis;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.NPCHassler;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.*;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HegemonyInspectionIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.managers.AoTDFactionManager;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Random;

public class AILegalizationCrisis extends BaseHostileActivityFactor {
    public AILegalizationCrisis(HostileActivityEventIntel intel) {
        super(intel);
    }

    public static final String HASSLE_REASON = "hegemonyInvestigator";

    public static String DEFEATED_HEGEMONY = "$defeatedHegemony";

    public static String INSPECTION_ATTEMPTS = "$hegemonyInspectionAttempts";
    public static String INSPECTIONS_DEFEATED = "$hegemonyInspectionsDefeated";

    public static int INSPECTIONS_TO_DEFEAT = 3;

    public static float INSPECTION_STRENGTH_FIRST = 150;
    public static float INSPECTION_STRENGTH_SECOND = 600;
    public static float INSPECTION_STRENGTH_FINAL = 1400;

    public static boolean isPlayerDefeatedHegemony() {
        return Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(DEFEATED_HEGEMONY);
    }
    public static void setPlayerDefeatedHegemony() {
        Global.getSector().getPlayerMemoryWithoutUpdate().set(DEFEATED_HEGEMONY, true);
    }

    public static int getInspectionAttempts() {
        return Global.getSector().getPlayerMemoryWithoutUpdate().getInt(INSPECTION_ATTEMPTS);
    }
    public static void incrInspectionAttempts() {
        Global.getSector().getPlayerMemoryWithoutUpdate().set(INSPECTION_ATTEMPTS, getInspectionAttempts() + 1);
    }

    public static int getInspectionsDefeated() {
        return Global.getSector().getPlayerMemoryWithoutUpdate().getInt(INSPECTIONS_DEFEATED);
    }
    public static void incrInspectionsDefeated() {
        Global.getSector().getPlayerMemoryWithoutUpdate().set(INSPECTIONS_DEFEATED, getInspectionsDefeated() + 1);
    }




    public String getProgressStr(BaseEventIntel intel) {
        return "";
    }

    @Override
    public int getProgress(BaseEventIntel intel) {
        if(hasPlayerMadeAICoreLegal()){
            if(!checkFactionExists(Factions.HEGEMONY, true)&&!checkFactionExists(Factions.LUDDIC_CHURCH,true)&&checkFactionExists(Factions.LUDDIC_PATH,true)) {
                return 0;
            }
        }
        if (!checkFactionExists(Factions.HEGEMONY, true)) {
            return 0;
        }
        return super.getProgress(intel);
    }

    public float getEffectMagnitude(StarSystemAPI system) {//, boolean adjustByEventProgress) {
        if(hasPlayerMadeAICoreLegal()){
            if(!checkFactionExists(Factions.HEGEMONY, true)&&!checkFactionExists(Factions.LUDDIC_CHURCH,true)&&checkFactionExists(Factions.LUDDIC_PATH,true)) {
                return 0f;
            }
            else {
                return 1f;
            }
        }
        if (!checkFactionExists(Factions.HEGEMONY, true)) {
            return 0;
        }
        return super.getEffectMagnitude(system);
    }

    public String getDesc(BaseEventIntel intel) {
        if(hasPlayerMadeAICoreLegal()){
            return "AI Legalization";
        }
        return "Hegemony";
    }

    public String getNameForThreatList(boolean first) {
        if(hasPlayerMadeAICoreLegal()){
            return "";
        }
        return "The Triumvirate";
    }


    public Color getDescColor(BaseEventIntel intel) {
        if (getProgress(intel) <= 0) {
            return Misc.getGrayColor();
        }
        return Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();
    }
    public static boolean hasPlayerMadeAICoreLegal(){
        return AoTDFactionManager.getInstance().doesHavePolicyEnabled("aotd_ai_legalization");
    }

    public TooltipMakerAPI.TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
        return new BaseFactorTooltip() {
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                if(hasPlayerMadeAICoreLegal()){
                    tooltip.addPara("Legalization of AI usage has caused massive turmoil around sector, prompting some factions to take much more drastic and direct actions.", 0f);
                }
                else{
                    tooltip.addPara("The Hegemony considers the use of AI cores illegal and will not tolerate it "
                            + "even outside the volume of the core worlds.", 0f);
                    tooltip.addPara("Fleets investigating your AI core use can sometimes be found in your space, not overtly "
                            + "hostile, but harassing your shipping and generally acting in a high-handed way.", opad);
                }


            }
        };
    }

    public boolean shouldShow(BaseEventIntel intel) {
        boolean shouldShowDueToCause = false;
        for (HostileActivityCause2 cause : getCauses()) {
            shouldShowDueToCause |= cause.shouldShow();
        }
        return getProgress(intel) > 0 || shouldShowDueToCause;
    }



    @Override
    public int getMaxNumFleets(StarSystemAPI system) {
        return Global.getSettings().getInt("hegemonyMaxFleets");
    }

    public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
        if(hasPlayerMadeAICoreLegal()){
            return null;
        }
        float f = 0f;
        //f += getEffectMagnitude(system);
        f += intel.getMarketPresenceFactor(system);

        if (f > 1f) f = 1f;

//		float fInvestigators = Global.getSettings().getFloat("hegemonyInvestigatorsFreq");
//		float fRecon = Global.getSettings().getFloat("hegemonyReconFreq");
//
//		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(random);
//		picker.add(1, fInvestigators);
//		picker.add(2, fRecon);
//
//		int pick = picker.pick();
//		boolean recon = pick == 2;
        boolean recon = false;

        int difficulty = 0;

        if (recon) {
            difficulty = 1 + random.nextInt(2);
        } else {
            difficulty = 3;
            difficulty += (int) Math.round(f * 5f);
            difficulty += random.nextInt(4);
        }


        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();

        Vector2f loc = system.getLocation();
        String factionId = Factions.HEGEMONY;

        if (recon) {
            m.createStandardFleet(difficulty, factionId, loc);
        } else {
            m.createStandardFleet(difficulty, factionId, loc);
        }

        m.triggerSetFleetType(FleetTypes.INVESTIGATORS);
        m.triggerSetPatrol();

        if (!recon) {
            m.triggerSetFleetHasslePlayer(HASSLE_REASON);
            m.triggerSetFleetFlag("$hegemonyInvestigator");
            m.triggerFleetAllowLongPursuit();
        }

        m.triggerMakeLowRepImpact();

        CampaignFleetAPI fleet = m.createFleet();

        if (fleet != null && !recon) {
            fleet.addScript(new NPCHassler(fleet, system));
        }

        return fleet;
    }


    @Override
    public void notifyFactorRemoved() {
        //Global.getSector().getListenerManager().removeListener(this);
    }

    public void notifyEventEnding() {
        notifyFactorRemoved();
    }


    public void addBulletPointForEvent(HostileActivityEventIntel intel, BaseEventIntel.EventStageData stage, TooltipMakerAPI info,
                                       IntelInfoPlugin.ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {

        if (!(stage.rollData instanceof HostileActivityEventIntel.HAERandomEventData)) return;
        HostileActivityEventIntel.HAERandomEventData data = (HostileActivityEventIntel.HAERandomEventData) stage.rollData;
        MarketAPI target = (MarketAPI) data.custom;

        //MarketAPI target = pickTargetMarket();
        if (target == null) return;

        MarketAPI from = pickSourceMarket();
        if (from == null) return;

        Color c = Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();

        LabelAPI label = info.addPara("Upcoming Hegemony AI inspection targeting %s",
                initPad, tc, tc, target.getName());
        label.setHighlight("Hegemony", target.getName());
        label.setHighlightColors(c, Misc.getBasePlayerColor());
    }

    public void addBulletPointForEventReset(HostileActivityEventIntel intel, BaseEventIntel.EventStageData stage, TooltipMakerAPI info,
                                            IntelInfoPlugin.ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
        info.addPara("Hegemony AI inspection averted", tc, initPad);
    }

    @Override
    public void addStageDescriptionForEvent(HostileActivityEventIntel intel, BaseEventIntel.EventStageData stage, TooltipMakerAPI info) {
        if (!(stage.rollData instanceof HostileActivityEventIntel.HAERandomEventData)) return;
        HostileActivityEventIntel.HAERandomEventData data = (HostileActivityEventIntel.HAERandomEventData) stage.rollData;
        MarketAPI target = (MarketAPI) data.custom;

        if (target == null) return;
        MarketAPI from = pickSourceMarket();
        if (from == null) return;

        Color c = Global.getSector().getFaction(Factions.HEGEMONY).getBaseUIColor();
        float small = 8f;
        float opad = 10f;

        if (hasPlayerMadeAICoreLegal()) {
            LabelAPI label = info.addPara("Your bold decision to legalize AI usage has sent shockwaves through the sector. "
                            + "For the Hegemony, AI regulation is not just policy—it's the law. By publicly endorsing AI, "
                            + "you've forced their hand: they must respond with overwhelming force to uphold their authority and preserve their position in the Persean Sector.",
                    small, c);
            label.setHighlight("legalize AI usage", "forced their hand", "overwhelming force");
            label.setHighlightColors(Misc.getBasePlayerColor(), Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());

            label = info.addPara("This is no routine inspection. In retaliation, the Hegemony is organizing a single, massive punitive expedition. "
                            + "You won’t be fending off minor fleets—this will be a direct and brutal campaign to break your defiance. "
                            + "Hegemony will send their best fleets and officers as a show of strength. "
                            + "Defeating this major wave is your only path to survival. Moreover, victory will send shockwaves across the Persean Sector.",
                    opad, Misc.getHighlightColor(), "massive punitive expedition", "best fleets", "shockwaves");
            label.setHighlightColors(Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), Misc.getPositiveHighlightColor());

            label = info.addPara("Your declaration has also outraged the Luddic Church and the Luddic Path. "
                            + "To them, AI represents heresy in its purest form. Now, the Luddic Church, Hegemony, and Luddic Path stand together, united by a singular purpose. "
                            + "You are now hostile to the Hegemony, Luddic Church, and Luddic Path, all determined to stamp out your AI acceptance.",
                    opad, Misc.getNegativeHighlightColor(), "heresy", "united by a singular purpose", "hostile");
            label.setHighlightColors(Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());

            // Crisis list setup for AI legalization scenario
            stage.beginResetReqList(info, false, "crisis", opad);
            info.addPara("These factions have mobilized their strongest military forces for this singular objective.", 0f, c, "strongest military forces");
            stage.endResetReqList(info, true, "crisis", -1, -1);

        } else {
            LabelAPI label = info.addPara("You've received intel that the Hegemony is planning "
                    + "an AI inspection targeting %s. If the inspection arrives at your colony, "
                    + "your options would include open hostilities with the Hegemony, or the loss of "
                    + "at least some of your AI cores.", small, c, target.getName());
            label.setHighlight(target.getName(), "open hostilities", "loss of at least some of your AI cores");
            label.setHighlightColors(Misc.getBasePlayerColor(), Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());

            int defeated = getInspectionsDefeated();
            if (defeated < INSPECTIONS_TO_DEFEAT - 1) {
                label = info.addPara("If this inspection is defeated by military means, the Hegemony is likely to "
                        + "escalate the conflict, although only up to a point.", opad, Misc.getNegativeHighlightColor(), "escalate the conflict");
                label.setHighlight("escalate the conflict", "up to a point");
                label.setHighlightColors(Misc.getNegativeHighlightColor(), Misc.getPositiveHighlightColor());
            } else {
                info.addPara("If this massive inspection force — a declaration of war in all but name — is defeated, "
                        + "the Hegemony is likely to reconsider the viability of their approach.", opad, Misc.getPositiveHighlightColor(), "reconsider");
            }

            stage.beginResetReqList(info, true, "crisis", opad);
            info.addPara("The %s has no functional military bases", 0f, c, "Hegemony");
            stage.endResetReqList(info, true, "crisis", -1, -1);
        }

        addBorder(info, c);
    }



    public String getEventStageIcon(HostileActivityEventIntel intel, BaseEventIntel.EventStageData stage) {
        if(hasPlayerMadeAICoreLegal()){
            return Global.getSettings().getSpriteName("events", "hostile_activity");
        }
        return Global.getSector().getFaction(Factions.HEGEMONY).getCrest();
    }

    public TooltipMakerAPI.TooltipCreator getStageTooltipImpl(final HostileActivityEventIntel intel, final BaseEventIntel.EventStageData stage) {
        if (stage.id == HostileActivityEventIntel.Stage.HA_EVENT) {
            if(hasPlayerMadeAICoreLegal()){
                return getDefaultEventTooltip("Coalition Expedition", intel, stage);
            }
            else{
                return getDefaultEventTooltip("Hegemony AI inspection", intel, stage);
            }

        }
        return null;
    }


    public float getEventFrequency(HostileActivityEventIntel intel, BaseEventIntel.EventStageData stage) {
        if (stage.id == HostileActivityEventIntel.Stage.HA_EVENT) {
            if(hasPlayerMadeAICoreLegal()){
                return Float.MAX_VALUE;
            }
            if (pickTargetMarket() != null && pickSourceMarket() != null) {
                return 10f;
            }
        }
        return 0;
    }

    public MarketAPI pickTargetMarket() {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(getRandomizedStageRandom());

        float alpha = Global.getSettings().getFloat("hegemonyPointsAlpha");
        float beta = Global.getSettings().getFloat("hegemonyPointsBeta");
        float gamma = Global.getSettings().getFloat("hegemonyPointsGamma");

        float threshold = alpha + beta + gamma;
        for (MarketAPI market : Misc.getPlayerMarkets(false)) {
            // to put a damper on shenanigans with establishing and abandoning a colony
            // with an Alpha Core admin to bait an attack
            if(hasPlayerMadeAICoreLegal()){
                picker.add(market, 10f);
            }
            if (market.getDaysInExistence() < 180f && !Global.getSettings().isDevMode()) continue;

            float w = HegemonyAICoresActivityCause.getAICorePoints(market);
            if (w <= threshold) continue;
            picker.add(market, w * w);
        }
        return picker.pick();
    }

    public MarketAPI pickSourceMarket() {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(getRandomizedStageRandom(7));
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.getFactionId().equals(Factions.HEGEMONY)) {
                Industry b = market.getIndustry(Industries.MILITARYBASE);
                if (b == null) b = market.getIndustry(Industries.HIGHCOMMAND);
                if (b == null || b.isDisrupted() || !b.isFunctional()) {
                    continue;
                }
                picker.add(market, market.getSize());
            }
        }
        MarketAPI from = picker.pick();
        return from;
    }


    public void rollEvent(HostileActivityEventIntel intel, BaseEventIntel.EventStageData stage) {
//		if (true) return;

        if (isPlayerDefeatedHegemony()) return;

        MarketAPI market = pickTargetMarket();
        if (market == null) return;

        MarketAPI from = pickSourceMarket();
        if (from == null) return;

        HostileActivityEventIntel.HAERandomEventData data = new HostileActivityEventIntel.HAERandomEventData(this, stage);
        data.custom = market;
        stage.rollData = data;
        intel.sendUpdateIfPlayerHasIntel(data, false);
    }

    public boolean fireEvent(HostileActivityEventIntel intel, BaseEventIntel.EventStageData stage) {
        //if (true) return false;

        if (isPlayerDefeatedHegemony()) return false;

        //MarketAPI market = pickTargetMarket();
        if (!(stage.rollData instanceof HostileActivityEventIntel.HAERandomEventData)) return false;
        HostileActivityEventIntel.HAERandomEventData data = (HostileActivityEventIntel.HAERandomEventData) stage.rollData;
        MarketAPI market = (MarketAPI) data.custom;

        if (market == null) return false;
        if (!market.isInEconomy()) return false;

        MarketAPI from = pickSourceMarket();
        if (from == null) return false;

        StarSystemAPI system = market.getStarSystem();
        if (system == null) return false;

        return createInspection(market, null);
    }


    public boolean createInspection(MarketAPI target, Integer fpOverride) {

//		MarketAPI target = pickTargetMarket();
//		if (target == null) return false;

        MarketAPI from = pickSourceMarket();
        if (from == null) return false;


        float fp;
        int defeated = getInspectionsDefeated();
        //defeated = 2;

        if (defeated <= 0) {
            fp = INSPECTION_STRENGTH_FIRST;
        } else if (defeated == 1) {
            fp = INSPECTION_STRENGTH_SECOND;
        } else {
            fp = INSPECTION_STRENGTH_FINAL;
        }

        //fp = 500;
        if (fpOverride != null) {
            fp = fpOverride;
        }
        HegemonyInspectionIntel inspection = new HegemonyInspectionIntel(from, target, fp);
        if (inspection.isDone()) {
            inspection = null;
            return false;
        }


        incrInspectionAttempts();

        return true;
    }


    @Override
    public void advance(float amount) {
        super.advance(amount);

        BaseEventIntel.EventStageData stage = intel.getDataFor(HostileActivityEventIntel.Stage.HA_EVENT);
        if (stage != null && stage.rollData instanceof HostileActivityEventIntel.HAERandomEventData &&
                ((HostileActivityEventIntel.HAERandomEventData)stage.rollData).factor == this) {
            if (pickSourceMarket() == null) {
                intel.resetHA_EVENT();
            }
        }
    }

    public static void avertInspectionIfNotInProgress() {
        HostileActivityEventIntel intel = HostileActivityEventIntel.get();
        if (intel == null) return;

        HostileActivityEventIntel.HAERandomEventData data = intel.getRollDataForEvent();
        if (data != null && data.factor instanceof HegemonyHostileActivityFactor) {
            intel.resetHA_EVENT();
        }
    }


    public void notifyInspectionEnded(HegemonyInspectionIntel.HegemonyInspectionOutcome outcome) {
        // also called when aborted from military base being destroyed, with same outcome enum
        if (outcome == HegemonyInspectionIntel.HegemonyInspectionOutcome.TASK_FORCE_DESTROYED) {
            incrInspectionsDefeated();
            int defeated = getInspectionsDefeated();
            if (defeated >= INSPECTIONS_TO_DEFEAT) {
                setPlayerDefeatedHegemony();

                Global.getSector().addScript(new DelayedActionScript(0.1f) {
                    @Override
                    public void doAction() {
                        MessageIntel msg = new MessageIntel();
                        msg.addLine("Major Hegemony defeat!", Misc.getBasePlayerColor());
                        msg.addLine(BaseIntelPlugin.BULLET +
                                "You may be able to discuss the situation with the High Hegemon on Chicomoztoc");
                        msg.setIcon(Global.getSector().getFaction(Factions.HEGEMONY).getCrest());
                        msg.setSound(Sounds.REP_GAIN);
                        Global.getSector().getCampaignUI().addMessage(msg, CommMessageAPI.MessageClickAction.NOTHING);
                    }
                });
            }
        }
    }

}
