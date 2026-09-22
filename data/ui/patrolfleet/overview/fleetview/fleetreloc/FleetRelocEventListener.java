package data.ui.patrolfleet.overview.fleetview.fleetreloc;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import data.scripts.patrolfleet.models.BasePatrolFleet;

public class FleetRelocEventListener implements FleetEventListener {
    BasePatrolFleet fleetData;
    public FleetRelocEventListener(BasePatrolFleet fleetData) {
        this.fleetData = fleetData;
    }
    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if(reason.equals(CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION)){
            fleetData.setInTransit(false);
        }
        else{
            fleetData.forceTransitDays();
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }
}
