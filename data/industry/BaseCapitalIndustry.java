package data.industry;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import data.kaysaar.aotd.tot.grandwonders.GrandWonderAPI;
import data.scripts.managers.AoTDFactionManager;
import data.ui.basecomps.ExtendUIPanelPlugin;
import data.ui.overview.capitalbuilding.BaseCapitalButton;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public abstract class BaseCapitalIndustry extends BaseIndustry implements GrandWonderAPI {
    public boolean hasSpecialButton(){
        return true;
    }
    public BaseCapitalButton createButton(float width, float height) {

        return new BaseCapitalButton(width,height);
    }
    public ExtendUIPanelPlugin createPanel(float width, float height) {
        return null;
    }
    @Override
    public boolean isAvailableToBuild() {
        return AoTDFactionManager.getInstance().doesControlCapital();
    }

    @Override
    public String getWonderTypeId() {
        return "base_capital_wonder";
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
        return new LinkedHashSet<>();
    }

    @Override
    public boolean shouldShowInListOfWonders(MarketAPI marketAPI) {
        return false;
    }
}
