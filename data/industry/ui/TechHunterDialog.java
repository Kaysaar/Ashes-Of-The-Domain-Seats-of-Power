package data.industry.ui;

import ashlib.data.plugins.ui.models.BaseSliderDialog;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.dialogs.PaymentNodeTechTTC;
import data.industry.NovaExploraria;
import data.route.AbyssDelversFleetRouteManager;
import data.route.TechHuntersFleetRouteManager;
import data.scripts.managers.AoTDFactionManager;

import java.awt.*;

public class TechHunterDialog extends BaseSliderDialog {
    int costPerMonth = 10000;
    public NovaExplorariaButton button;
    public TechHunterDialog(String headerTitle, int mult, int maxSegments, int currSegment, int minSection,NovaExplorariaButton button) {
        super(headerTitle, mult, maxSegments, currSegment, minSection);
        this.button = button;
    }
    @Override
    public void populateTooltipTop(TooltipMakerAPI tooltip, int effectiveSegment) {
        tooltip.setParaFont(Fonts.ORBITRON_16);
        float days =60;
        String months = Misc.getRoundedValueMaxOneAfterDecimal(days/30);
        tooltip.addPara("Expedition Estimated Time %s +/- %s",2f, Color.ORANGE,(currentSegment*mult)+" months","("+months+" months)");
        tooltip.addPara("Cost of expedition : %s",5f, Color.ORANGE,Misc.getDGSCredits(currentSegment*costPerMonth));
        tooltip.setParaFont(Fonts.ORBITRON_12);
        tooltip.addPara("First half of cost will be payed upfront, while second will be only paid, if expedition is successful",Misc.getTooltipTitleAndLightHighlightColor(),3f);

        ;
    }
    @Override
    public LabelAPI createLabelForBar(TooltipMakerAPI tooltip) {

        return tooltip.addPara("Expedition time : " + this.currentSegment * this.mult + " / " + this.maxSegment * this.mult, Misc.getTooltipTitleAndLightHighlightColor(), 5.0F);

    }

    @Override
    public void populateTooltipBelow(TooltipMakerAPI tooltip, int effectiveSegment) {
        tooltip.setParaFont(Fonts.ORBITRON_16);
        tooltip.addPara("Chance for expedition failure : %s",1f,Misc.getNegativeHighlightColor(),Misc.getRoundedValue(TechHuntersFleetRouteManager.getPercentageForTechHunterFailure(currentSegment)*100)+"%");

        tooltip.setParaFont(Fonts.ORBITRON_12);
        tooltip.addPara("Note: Longer expedition lasts, bigger chance for failure is, but also longer expeditions bring much more valuable loot!",Misc.getTooltipTitleAndLightHighlightColor(),3f);
        tooltip.addPara("Expedition timer starts immediate once fleet leaves system.",Misc.getTooltipTitleAndLightHighlightColor(),3f);

    }

    @Override
    public void applyConfirmScript() {
        MonthlyReport report = SharedData.getData().getCurrentReport();
        MonthlyReport.FDNode marketsNode = report.getNode(MonthlyReport.OUTPOSTS);
        MonthlyReport.FDNode marketsNode2 = report.getNode(marketsNode,"nova_exploraria");
        MonthlyReport.FDNode paymentNode = report.getNode(marketsNode2, "tech");
        marketsNode2.mapEntity = AoTDFactionManager.getInstance().getCapitalMarket().getPrimaryEntity();
        marketsNode2.name = "Nova Exploraria";
        marketsNode2.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
        paymentNode.name = "Tech Hunters Payment";
        //paymentNode.custom = MonthlyReport.EXPORTS;
        //paymentNode.mapEntity = market.getPrimaryEntity();
        paymentNode.upkeep += (costPerMonth*currentSegment);

        paymentNode.tooltipCreator = new PaymentNodeTechTTC();
        paymentNode.mapEntity = AoTDFactionManager.getInstance().getCapitalMarket().getPrimaryEntity();
        paymentNode.icon = Global.getSettings().getSpriteName("income_report", "generic_expense");
        NovaExploraria.getNova().sentTechHunterFleet(currentSegment);
        button.createUI();
    }

    @Override
    public float getBarY() {
        return 95;
    }

    @Override
    public float getBarX() {
        return 5f;
    }
}
