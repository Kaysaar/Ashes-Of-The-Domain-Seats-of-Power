package data.industry.grandwonder;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.kaysaar.aotd.tot.grandwonders.GrandWonderManager;
import data.kaysaar.aotd.tot.grandwonders.GrandWonderTypeSpecAPI;

import java.awt.*;

public class ReligionWonderType implements GrandWonderTypeSpecAPI {
    @Override
    public String getId() {
        return "aotd_religion_wonder";
    }

    @Override
    public String getName() {
        return "Religious";
    }

    @Override
    public Color getColor() {
        return Global.getSettings().getFactionSpec(Factions.LUDDIC_CHURCH).getBaseUIColor();
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
        return GrandWonderManager.getInstance().getAmountOfWondersOfSameType(this.getId())<getMaxAmountOfWonderOfType(s,marketAPI);
    }

    @Override
    public int getMaxAmountOfWonderOfType(String s, MarketAPI marketAPI) {
        return 1;
    }

    @Override
    public void createTooltipForTypeOfWonder(TooltipMakerAPI tooltipMakerAPI, MarketAPI marketAPI) {

    }
}
