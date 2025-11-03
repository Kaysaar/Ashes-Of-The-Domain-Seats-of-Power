package data.scripts.patrolfleet.everyframe;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.util.IntervalUtil;
import data.intel.FleetDecommissionedIntel;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;

import java.util.ArrayList;
import java.util.Iterator;

public class AoTDPatrolManagerMover implements EveryFrameScript {
    IntervalUtil util = new IntervalUtil(Global.getSector().getClock().getSecondsPerDay(),Global.getSector().getClock().getSecondsPerDay());

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        util.advance(amount);
        if(util.intervalElapsed()){
            AoTDFactionPatrolsManager.getInstance().advanceFleets(util.getElapsed());
            ArrayList<BasePatrolFleet> fleets = new ArrayList<>(AoTDFactionPatrolsManager.getInstance().fleetsCurrentlyInField.values().stream().filter(BasePatrolFleet::shouldRemove).toList());

            for (BasePatrolFleet fleet : fleets) {
                AoTDFactionPatrolsManager.getInstance().removeFleet(fleet.getId());

            }
            if(!fleets.isEmpty()){
                FleetDecommissionedIntel intel = new FleetDecommissionedIntel(fleets);
                Global.getSector().getIntelManager().addIntel(intel);
                intel.endAfterDelay(10f);
            }
            Iterator iter = Global.getSector().getIntelManager().getIntel(FleetDecommissionedIntel.class).iterator();
            ArrayList<BaseIntelPlugin>temp = new ArrayList<>();
            while (iter.hasNext()) {
                BaseIntelPlugin plugin = (BaseIntelPlugin) iter.next();
                plugin.advance(util.getElapsed());
                if(plugin.isEnded()){
                    temp.add(plugin);
                }
            }
            temp.forEach(x->Global.getSector().getIntelManager().removeIntel(x));
            temp.clear();
            AoTDFactionPatrolsManager.getInstance().advanceAfterFleets(amount);


        }
        }

}
