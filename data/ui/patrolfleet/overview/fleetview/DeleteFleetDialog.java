package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;

import java.awt.*;

public class DeleteFleetDialog extends BasePopUpDialog {
    BasePatrolFleet fleetToDecom;
    boolean isDecom;

    public DeleteFleetDialog(BasePatrolFleet fleetToDecom) {
        super("De-commision fleet");
        this.fleetToDecom = fleetToDecom;
    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);
        float days = fleetToDecom.getFPTaken()/AoTDFactionPatrolsManager.getInstance().getDaysPerFP().getModifiedValue();

        tooltip.addPara("You are about to de-commission %s, which will leave %s, less defended.", 3f, Color.ORANGE, fleetToDecom.getNameOfFleet(), fleetToDecom.getTiedTo().getName());
        tooltip.addPara("Depending on current state of patrol decommission will take around %s. FP will be slowly refunded as de-commission progresses.", 5f,Color.ORANGE, AshMisc.convertDaysToString(Math.round(days)));
        tooltip.addPara("This procedure can not be canceled! Do you want to proceed?", 15f).setAlignment(Alignment.MID);


    }

    @Override
    public void applyConfirmScript() {
        super.applyConfirmScript();
        fleetToDecom.setDecomisioned(true);
        if (!AoTDMilitaryBase.isPatroling(fleetToDecom.getId(), fleetToDecom.getTiedTo())) {
            fleetToDecom.getShipsForReplacementWhenInPrep().clear();
            float days = fleetToDecom.getFPTaken()/AoTDFactionPatrolsManager.getInstance().getDaysPerFP().getModifiedValue();
            fleetToDecom.startProcessOfDecom(days);
        }

        OverviewPatrolPanel.forceRequestUpdate = true;
    }
}
