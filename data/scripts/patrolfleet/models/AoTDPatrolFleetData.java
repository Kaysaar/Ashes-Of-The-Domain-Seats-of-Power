package data.scripts.patrolfleet.models;

import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;

public class AoTDPatrolFleetData {
    String id;
    FleetFactory.PatrolType type;

    public FleetFactory.PatrolType getType() {
        return type;
    }

    public void setType(FleetFactory.PatrolType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
