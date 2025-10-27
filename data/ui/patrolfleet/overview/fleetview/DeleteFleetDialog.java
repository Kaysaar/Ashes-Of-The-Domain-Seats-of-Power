package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.FactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;

import java.awt.*;

public class DeleteFleetDialog extends BasePopUpDialog {
    BasePatrolFleet fleetToDecom;

    public DeleteFleetDialog(BasePatrolFleet fleetToDecom) {
        super("De-commission fleet");
        this.fleetToDecom = fleetToDecom;
    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);
        tooltip.addPara("You are about to de-commission %s, which will leave %s, less defended.",3f, Color.ORANGE,fleetToDecom.getNameOfFleet(),fleetToDecom.getTiedTo().getName());
        tooltip.addPara("Depending on current state of patrol, de-commissioning might take up to 45 days!", Misc.getTooltipTitleAndLightHighlightColor(),5f);
        tooltip.addPara("Do you want to proceed?",15f).setAlignment(Alignment.MID);
    }

    @Override
    public void applyConfirmScript() {
        super.applyConfirmScript();
        FactionPatrolsManager.getInstance().removeFleet(fleetToDecom.getId());
        fleetToDecom.data.clear();
        fleetToDecom.assignedShipsThatShouldSpawn.clear();
        OverviewPatrolPanel.forceRequestUpdate = true;
    }
}
