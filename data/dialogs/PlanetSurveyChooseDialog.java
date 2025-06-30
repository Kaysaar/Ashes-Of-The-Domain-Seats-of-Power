package data.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateHaulerIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateHaulerCMD;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.conditions.AoTDFactionCapital;
import data.industry.NovaExploraria;
import data.misc.ReflectionUtilis;
import data.scripts.managers.AoTDFactionManager;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Map;

public class PlanetSurveyChooseDialog implements InteractionDialogPlugin {

    @Override
    public void init(InteractionDialogAPI dialog) {
        dialog.getTextPanel().clear();
        dialog.setBackgroundDimAmount(0.9f);
        final ArrayList<SectorEntityToken> systems = new ArrayList<SectorEntityToken>();
        for (StarSystemAPI curr : Global.getSector().getStarSystems()) {
            if (Global.getSector().getEconomy().getMarkets(curr.getCenter().getContainingLocation()).stream().anyMatch(x -> x.getFaction() != null && x.isInEconomy()))
                continue;
            if (!curr.getPlanets().stream().filter(x -> !x.isBlackHole() && !x.isStar()).toList().isEmpty()) {
                if (curr.getPlanets().stream().filter(x -> x.getMarket() != null).allMatch(x -> x.getMarket().getSurveyLevel() == MarketAPI.SurveyLevel.FULL))
                    continue;
            } else if (curr.isEnteredByPlayer()) {
                continue;
            }
            if (curr.getName().contains("Limbo")) continue;
            if (curr.getHyperspaceAnchor() == null) continue;
            if (Misc.getStarSystemForAnchor(curr.getHyperspaceAnchor()) == null) continue;
            systems.add(curr.getHyperspaceAnchor());
        }
        dialog.showCampaignEntityPicker("Select System for Survey Expedition", "Destination:", "Proceed",
                Global.getSector().getPlayerFaction(), systems,
                new BaseCampaignEntityPickerListener() {
                    public void pickedEntity(SectorEntityToken entity) {
                        dialog.dismiss();
                        StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                        MonthlyReport report = SharedData.getData().getCurrentReport();
                        MonthlyReport.FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
                        MonthlyReport.FDNode marketsNode2 = report.getNode(marketsNode,"nova_exploraria");
                        marketsNode2.mapEntity = AoTDFactionManager.getInstance().getCapitalMarket().getPrimaryEntity();
                        marketsNode2.name = "Nova Exploraria";
                        marketsNode2.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
                        MonthlyReport.FDNode paymentNode = report.getNode(marketsNode2, "tech");
                        int reward;
                        reward = (int) Misc.getDistance(new Vector2f(), entity.getLocationInHyperspace());
                        //reward *= 1.25f;
                        reward = 20000 + (reward / 10000) * 10000;
                        if (reward < 10000) reward = 10000;
                        int payment = reward;
                        paymentNode.name = "Tech Hunters Payment";
                        //paymentNode.custom = MonthlyReport.EXPORTS;
                        //paymentNode.mapEntity = market.getPrimaryEntity();
                        paymentNode.upkeep += payment;
                        paymentNode.tooltipCreator = new PaymentNodeTTC();
                        paymentNode.mapEntity = AoTDFactionManager.getInstance().getCapitalMarket().getPrimaryEntity();
                        paymentNode.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");


                        NovaExploraria.getNova().sentExpeditionFleet(system);
                    }

                    public void cancelledEntityPicking() {

                        dialog.dismiss();

                    }

                    public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
                        StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                        if (system != null) {
                            return system.getNameWithLowercaseTypeShort();
                        }
                        return null;
                    }

                    public String getSelectedTextOverrideFor(SectorEntityToken entity) {
                        StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                        if (system != null) {
                            return system.getNameWithLowercaseType();
                        }
                        return null;
                    }

                    public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {
                        int days = 30;
                        info.setParaSmallInsignia();
                        int reward;
                        reward = (int) Misc.getDistance(new Vector2f(), entity.getLocationInHyperspace());
                        //reward *= 1.25f;
                        reward = 20000 + (reward / 10000) * 10000;
                        if (reward < 10000) reward = 10000;
                        info.addPara("   Estimated costs of expedition: %s ", 0f, Misc.getHighlightColor(), Misc.getDGSCredits(reward));
                    }

                    public boolean canConfirmSelection(SectorEntityToken entity) {
                        return true;
                    }

                    public float getFuelColorAlphaMult() {
                        return 0f;
                    }

                    public float getFuelRangeMult() { // just for showing it on the map when picking destination
                        return 0f;
                    }
                });
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {

    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {

    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return Map.of();
    }
}
