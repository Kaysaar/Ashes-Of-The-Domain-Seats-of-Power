package data.ui.patrolfleet.overview.dialogs;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class ArmoryTabDialog extends BasePopUpDialog {
    ArmoryTabContent content = new ArmoryTabContent(1000,550);
    public ArmoryTabDialog() {

        super("Armory of "+ Global.getSector().getPlayerFaction().getDisplayName());
        AshMisc.initPopUpDialog(this,1050,640);
    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        tooltip.addCustom(content.getMainPanel(),0f);
    }

    @Override
    public void onExit() {
        content.clearUI();
        super.onExit();
    }

    @Override
    public void applyConfirmScript() {
        content.confirmRun();
    }
}
