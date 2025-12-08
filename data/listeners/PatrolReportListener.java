package data.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import data.intel.FleetFPReport;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;

import java.util.List;

public class PatrolReportListener implements EconomyTickListener {
    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        if(AoTDFactionPatrolsManager.getInstance().getTotalFpGenerated()>0){
            Global.getSector().getIntelManager().removeAllThatShouldBeRemoved();
            FleetFPReport report = new FleetFPReport();
            report.endAfterDelay(10f);
            Global.getSector().getIntelManager().getIntel().removeIf(x->x instanceof FleetFPReport);
            Global.getSector().getIntelManager().addIntel(report);
        }
    }
}
