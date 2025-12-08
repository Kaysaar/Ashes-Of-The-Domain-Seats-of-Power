package data.ui.patrolfleet.overview.fleetview.templatelist;

import ashlib.data.plugins.ui.models.DropDownButton;
import com.fs.starfarer.api.Global;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;
import data.ui.patrolfleet.overview.components.HoldingsDropDownButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FleetTableUtilis {
    public static void sortDropDownButtonsByName(ArrayList<DropDownButton> buttons, final boolean ascending) {
        Collections.sort(buttons, new Comparator<DropDownButton>() {
            @Override
            public int compare(DropDownButton button1, DropDownButton button2) {
                String name1 = getButtonName(button1);
                String name2 = getButtonName(button2);
                return ascending ? name1.compareToIgnoreCase(name2) : name2.compareToIgnoreCase(name1);
            }
        });
    }
    private static String getButtonName(DropDownButton button) {
        if(button instanceof FleetButtonDrop hl){
            return hl.fleet.getNameOfFleet();
        }
        return "";
    }
    public static void sortDropDownButtonsByFPConsumed(ArrayList<DropDownButton> buttons, final boolean ascending) {
        Collections.sort(buttons, new Comparator<DropDownButton>() {
            @Override
            public int compare(DropDownButton button1, DropDownButton button2) {
                float days1 = calculateFPTaken(button1);
                float days2 = calculateFPTaken(button2);
                return ascending ? Float.compare(days1, days2) : Float.compare(days2, days1);
            }
        });
    }
    private static float calculateFPTaken(DropDownButton button) {
        if(button instanceof FleetButtonDrop hl){
            return hl.fleet.getFPTaken();
        }
        return 0f;
    }
}
