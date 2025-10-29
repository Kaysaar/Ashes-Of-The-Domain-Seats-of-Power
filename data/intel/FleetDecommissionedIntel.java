package data.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.models.BasePatrolFleet;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public class FleetDecommissionedIntel extends BaseIntelPlugin {
    ArrayList<BasePatrolFleet>fleetsDeleted=  new ArrayList<>();
    public FleetDecommissionedIntel(ArrayList<BasePatrolFleet>fleetsDeleted) {
        this.fleetsDeleted = fleetsDeleted;


    }
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MILITARY);
        return tags;
    }
    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara("Those fleets were decommissioned / destroyed in action",2f).setAlignment(Alignment.MID);
        info.setBulletedListMode(BaseIntelPlugin.BULLET);
        for (BasePatrolFleet fleet : fleetsDeleted) {
            info.addPara(fleet.getNameOfFleet()+" - Used FP: %s",3f, Color.ORANGE,""+fleet.getFPTakenIgnoreDecom());
        }
        info.setBulletedListMode(null);

    }
    @Override
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color title = getTitleColor(mode);

        // Title of the intel
        info.addPara(getName(), title, 0f);
        info.addPara("Fleet Decommission Log", Misc.getTooltipTitleAndLightHighlightColor(),5f);

    }
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }
    @Override
    protected String getName() {
        return "Decommissioned Fleets : "+ Global.getSector().getClock().getShortDate();
    }

    @Override
    protected void notifyEnded() {
        fleetsDeleted.forEach(BasePatrolFleet::clear);
        fleetsDeleted.clear();
    }
}
