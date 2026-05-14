package data.industry.grandwonder;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.kaysaar.aotd.tot.grandwonders.GrandWonderManager;
import data.kaysaar.aotd.tot.grandwonders.GrandWonderTypeSpecAPI;

import java.awt.*;

public class SeatOfPowerProjects  implements GrandWonderTypeSpecAPI {
    @Override
    public String getId() {
        return "base_capital_wonder";
    }

    @Override
    public String getName() {
        return "Seat of Power Directorates";
    }

    @Override
    public Color getColor() {
        return new Color(130, 93, 234);
    }

    @Override
    public boolean isUniqueViaCategory() {
        return false;
    }

    @Override
    public boolean showTypeSeparate() {
        return true;
    }

    @Override
    public boolean canBuildAdditionalWonderOfType(String s, MarketAPI marketAPI) {
        return GrandWonderManager.getInstance().getBuiltSoFar(s)<getMaxAmountOfWonderOfType(s,marketAPI);
    }

    @Override
    public int getMaxAmountOfWonderOfType(String s, MarketAPI marketAPI) {
        return 1;
    }

    @Override
    public void createTooltipForTypeOfWonder(TooltipMakerAPI tooltipMakerAPI, MarketAPI marketAPI) {

    }
}
