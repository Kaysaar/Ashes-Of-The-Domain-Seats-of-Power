package data.scripts.models;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.BaseCampaignObjectivePlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseSpaceStructureObject extends BaseCampaignObjectivePlugin {
    BaseSpaceStructure struct;

    @Override
    public void init(SectorEntityToken entity, Object pluginParams) {
        super.init(entity, pluginParams);
        this.struct = (BaseSpaceStructure) pluginParams;
    }
}
