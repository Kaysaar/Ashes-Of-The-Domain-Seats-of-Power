package data.intel;

import ashlib.data.plugins.coreui.CommandTabMemoryManager;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;

import java.awt.*;
import java.util.Set;

public class FleetFPReport extends BaseIntelPlugin {
    public static Object Button_Fleet = new Object();
    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_MILITARY);
        return tags;
    }
    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        int total = AoTDFactionPatrolsManager.getInstance().getTotalFpGenerated();
        int available = AoTDFactionPatrolsManager.getInstance().getAvailableFP();
        String percentage = Misc.getRoundedValue((available/total)*100f)+"%";
        info.addPara("Over %s of FP ( %s ) is not assigned!",2f,new Color[]{Color.ORANGE,Misc.getNegativeHighlightColor()},percentage,""+available).setAlignment(Alignment.MID);
        info.addPara("Access military overview to assign FP to patrols",3f).setAlignment(Alignment.MID);
        addGenericButton(info,width,"Access Military Tab", Button_Fleet);
    }
    @Override
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color title = Global.getSector().getPlayerFaction().getBaseUIColor();

        // Title of the intel
        info.addPara(getName(), title, 0f);

    }
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }
    @Override
    protected String getName() {
        return "Military report : "+ Global.getSector().getClock().getShortDate();
    }
    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if(buttonId== Button_Fleet){
            CommandTabMemoryManager.getInstance().setLastCheckedTab("military");
            CommandTabMemoryManager.getInstance().getTabStates().put("military","overview");
            Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS);
        }

    }


    @Override
    protected void notifyEnded() {

    }
}
