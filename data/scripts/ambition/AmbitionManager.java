package data.scripts.ambition;

import ashlib.data.plugins.coreui.CommandTabMemoryManager;
import ashlib.data.plugins.coreui.CommandTabTracker;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.managers.AoTDFactionManager;
import data.ui.overview.OverviewShortInfoPanel;

public class AmbitionManager implements EveryFrameScript {
    public BaseAmbition currentAmbition;
    IntervalUtil util = new IntervalUtil(0.5f,1f);
    public static String key = "$aotd_amb_manager";
    public boolean showedMessage = false;
    boolean newGameMode = false;
    boolean showedAmbitionUI = false;

    public void setNewGameMode(boolean newGameMode) {
        newGameModeInterval = new IntervalUtil(3f,3f);
        this.newGameMode = newGameMode;
    }

    IntervalUtil newGameModeInterval = new IntervalUtil(1f,1f);
    @Override
    public boolean isDone() {
        return false;
    }

    public void setCurrentAmbition(BaseAmbition currentAmbition) {
        this.currentAmbition = currentAmbition;
    }

    public BaseAmbition getCurrentAmbition() {
        return currentAmbition;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
    public static AmbitionManager getInstance() {
        if(!Global.getSector().getPersistentData().containsKey(key))setInstance();
        return (AmbitionManager) Global.getSector().getPersistentData().get(key);

    }
    public static void setInstance(){
        AmbitionManager instance = new AmbitionManager();
        Global.getSector().getPersistentData().put(key, instance);
        Global.getSector().addScript(instance);
    }

    @Override
    public void advance(float amount) {
//        newGameModeInterval.advance(amount);
//        if(newGameModeInterval.intervalElapsed()){
//            goToAmbitionPanel();
//        }
//        util.advance(amount);
//        if(util.intervalElapsed()){
//
//        }
    }

    private void goToAmbitionPanel() {
        if(currentAmbition!=null){
            showedAmbitionUI = true;
            showedMessage = true;
            return;
        }
        if(Global.getSettings().isDevMode()){
            if(Global.getSector().getCampaignUI()==null)return;
            if(!Global.getSector().getCampaignUI().isShowingDialog()&&Global.getSector().getCampaignUI().getCurrentCoreTab()==null&& !AoTDFactionManager.getMarketsUnderPlayer().isEmpty()){
                if(!showedMessage){

                    Global.getSector().getCampaignUI().showMessageDialog(
                            "Now that we’ve founded our faction, it’s time to define the ultimate goal that will guide our journey. " +
                                    "Every great power in the Sector is driven by a vision — and ours must be no different. " +
                                    "What future will we strive to bring into being? "
                    );
                    newGameModeInterval = new IntervalUtil(0.3f,0.3f);
                    showedMessage = true;
                }
                else if(!showedAmbitionUI){
                    CommandTabMemoryManager.getInstance().setLastCheckedTab("faction");
                    CommandTabMemoryManager.getInstance().getTabStates().put("faction","overview");
                    OverviewShortInfoPanel.showAmbPanel = true;
                    showedAmbitionUI = true;
                    Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS);
                    CommandTabTracker.lockMainPanel();
                }

            }
        }

    }
}
