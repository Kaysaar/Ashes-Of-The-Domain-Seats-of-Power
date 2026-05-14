package data.industry;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.kaysaar.aotd.tot.grandwonders.GrandWonderAPI;
import data.listeners.LuddicShrineCounterListener;
import data.scripts.managers.AoTDFactionManager;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class ShrineOfGilead extends BaseIndustry implements GrandWonderAPI {
    public static int CREDIT_PER_PILGRIM = 3;

    @Override
    public void apply() {
        super.apply(true);
        boolean eligable = true;
        if (market.getFaction().isPlayerFaction()) {
            if (AoTDFactionManager.getInstance().doesHavePolicyEnabled("aotd_ai_legalization")) {
                eligable = false;
            }
        }
        if (eligable) {
            float weight = LuddicShrineCounterListener.getListenerInstance().getWeightForFaction(market.getFaction());
            getIncome().modifyFlat("faithful_tax", Math.round(weight * CREDIT_PER_PILGRIM), "Tithe from faithful");
        }
        else{
            /// TODO  Add income bonuses later
        }


    }

    @Override
    public void unapply() {
        super.unapply();
        getIncome().unmodifyFlat("faithful_tax");
    }

    @Override
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        boolean eligable = true;
        if (market.getFaction().isPlayerFaction()) {
            if (AoTDFactionManager.getInstance().doesHavePolicyEnabled("aotd_ai_legalization")) {
                eligable = false;
            }
        }
        if (eligable) {
            tooltip.addSectionHeading("Center of Faith", market.getFaction().getBaseUIColor(), market.getFaction().getDarkUIColor(), Alignment.MID, 5f);
            tooltip.addPara(
                    "This wonder stands as a center of Luddic faith, drawing pilgrims and collecting tithes from the faithful.",
                    3f
            );

            tooltip.addPara(
                    "Its influence grows with each world under faction control that has %s, but rival or independent worlds with %s diminish the flow of tithes.",
                    3f,
                    Color.ORANGE,
                    "Luddic Majority",
                    "Luddic Majority"
            );
        } else {
            tooltip.addSectionHeading("Blasphemous Exhibition",
                    market.getFaction().getBaseUIColor(),
                    market.getFaction().getDarkUIColor(),
                    Alignment.MID,
                    5f);

            tooltip.addPara(
                    "This site was once a revered center of Luddic faith, but its sanctity has been utterly defiled.",
                    3f
            );

            tooltip.addPara(
                    "Now controlled by followers of Moloch, its sacred grounds have been repurposed through the use of forbidden AI, turning it into a hollow spectacle.",
                    3f
            );

            tooltip.addPara(
                    "Visitors still come, but not as pilgrims. The faithful shun this place, and no tithes flow from what is now seen as a blasphemous mockery.",
                    3f
            );

        }


    }

    @Override
    public boolean isImproved() {
        return false;
    }

    @Override
    public boolean canImprove() {
        return false;
    }


    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    @Override
    public boolean canShutDown() {
        return false;
    }

    @Override
    public boolean showShutDown() {
        return false;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }

    @Override
    public LinkedHashMap<String, Integer> getDemandCostForRestoration() {
        LinkedHashMap<String, Integer> resources = new LinkedHashMap<>();
        resources.put(Commodities.HEAVY_MACHINERY, 10);
        resources.put(Commodities.METALS, 15);
        resources.put(Commodities.SUPPLIES, 10);
        return resources;
    }

    @Override
    public void finishedConstruction(MarketAPI marketAPI) {

    }

    @Override
    public String getWonderTypeId() {
        return "aotd_religion_wonder";
    }

    @Override
    public void addToCustomSectionInTooltip(TooltipMakerAPI tooltipMakerAPI) {

    }

    @Override
    public LinkedHashMap<String, String> getRequirementsToBuildWonder() {
        return new LinkedHashMap<>();
    }

    @Override
    public boolean hasReqBeenMetOnMarket(String s) {
        return true;
    }

    @Override
    public LinkedHashSet<String> getIndustriesToPreventFromAppearingInMenu(MarketAPI marketAPI) {
        return null;
    }

    @Override
    public boolean shouldShowInListOfWonders(MarketAPI marketAPI) {
        return false;
    }

}
