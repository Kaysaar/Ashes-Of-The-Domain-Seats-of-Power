package data.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import data.intel.FleetFPReport;
import data.scripts.managers.AoTDFactionManager;

public class PatrolReportListener implements EconomyTickListener {
    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        if(!AoTDFactionManager.getMarketsUnderPlayer().isEmpty()){
            Global.getSector().getIntelManager().removeAllThatShouldBeRemoved();
            FleetFPReport report = new FleetFPReport();
            report.endAfterDelay(10f);
            Global.getSector().getIntelManager().addIntel(report);
        }
    }
}
