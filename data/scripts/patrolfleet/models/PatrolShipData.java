package data.scripts.patrolfleet.models;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;

public class PatrolShipData {
    public String shipId;
    public String modID;
    public boolean isVanillaShip = true;

    public String getShipId() {
        return shipId;
    }


    public PatrolShipData(String shipID) {
        this.shipId = shipID;
        if (isShipPresent()) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(shipId);
            if (spec.getSourceMod() != null) {
                isVanillaShip = false;
                modID = spec.getSourceMod().getId();
            } else {
                modID = "vanilla";
            }
        }
    }

    public boolean isShipPresent() {
        if (shipId == null) return false;
        try {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(shipId);
            return spec != null;

        } catch (Exception e) {
            return false;
        }
    }
}
