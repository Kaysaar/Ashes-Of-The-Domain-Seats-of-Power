package data.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.CoreUITrackerScript;
import data.scripts.models.BaseFactionTimelineEvent;

import java.awt.*;

public class EventOccuredIntel extends BaseIntelPlugin {
    public static Object Button_SHIP = new Object();
    public BaseFactionTimelineEvent  event;
    public EventOccuredIntel(BaseFactionTimelineEvent stage) {
        this.event = stage;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        // The location on the map of the intel
        return Global.getSector().getPlayerFaction().getProduction().getGatheringPoint().getPrimaryEntity();
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color title = getTitleColor(mode);

        // Title of the intel
        info.addPara(getName(), title, 0f);
        info.addPara("Notable event : %s",5f,Color.ORANGE,event.getTitleOfEvent());

    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        info.addPara("A new event appeared on timeline!", 5f);
        info.addPara("XP from event : %s",5f,Color.ORANGE,(BaseFactionTimelineEvent.multiplier*event.getPointsForGoal()*10)+"");

        addGenericButton(info,width,"Access Faction Timeline",Button_SHIP);
    }


    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "historian_intel_icon");
    }

    @Override
    protected String getName() {
        return "Notable event : "+event.getTitleOfEvent();
    }

    @Override
    public boolean doesButtonHaveConfirmDialog(Object buttonId) {
        return super.doesButtonHaveConfirmDialog(buttonId);
    }

    @Override
    public boolean shouldRemoveIntel() {
        return isEnded();
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if(buttonId==Button_SHIP){
            CoreUITrackerScript.setMemFlag(CoreUITrackerScript.getStringForCoreTabFaction());
            CoreUITrackerScript.setMemFlagForFactionTab("timeline");
            Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS);
        }

    }
}
