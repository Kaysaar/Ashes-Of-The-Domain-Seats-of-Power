package data.ui.patrolfleet.overview.fleetview.templatelist;

import ashlib.data.plugins.ui.models.DropDownButton;
import ashlib.data.plugins.ui.plugins.UITableImpl;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.ui.patrolfleet.overview.fleetview.FleetButtonComponent;

public class FleetButtonDrop extends DropDownButton {
    BasePatrolFleet fleet;
    public FleetButtonDrop(UITableImpl tableOfReference, float width, float height, float maxWidth, float maxHeight, BasePatrolFleet fleet) {
        super(tableOfReference, width, height, maxWidth, maxHeight, false);
        this.fleet = fleet;
    }

    @Override
    public void createUIContent() {
        super.createUIContent();
        mainButton = new FleetButtonComponent(width, height, fleet, true);
        mainButton.createUI();
        tooltipOfImpl.addCustom(mainButton.getPanel(), 5f).getPosition().inTL(0, 0);
    }
}

