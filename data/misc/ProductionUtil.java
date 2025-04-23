package data.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public class ProductionUtil {
    public static UIPanelAPI getCoreUI() {
        CampaignUIAPI campaignUI;
        campaignUI = Global.getSector().getCampaignUI();
        InteractionDialogAPI dialog = campaignUI.getCurrentInteractionDialog();

        CoreUIAPI core;
        if (dialog == null) {
            core = (CoreUIAPI) ReflectionUtilis.invokeMethod("getCore",campaignUI);
        }
        else {
            core = (CoreUIAPI) ReflectionUtilis.invokeMethod( "getCoreUI",dialog);
        }
        return core == null ? null : (UIPanelAPI) core;
    }

    public static UIPanelAPI getCurrentTab() {
        UIPanelAPI coreUltimate = getCoreUI();
        UIPanelAPI core = (UIPanelAPI) ReflectionUtilis.invokeMethod("getCurrentTab",coreUltimate);
        return core == null ? null : (UIPanelAPI) core;
    }


}
