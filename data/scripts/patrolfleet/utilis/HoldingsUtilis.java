package data.scripts.patrolfleet.utilis;

import ashlib.data.plugins.ui.models.DropDownButton;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.ui.holdings.starsystems.components.StarSystemHoldingDropDown;
import data.ui.patrolfleet.overview.components.PatrolFleetHoldingsDropDown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HoldingsUtilis {
    public static ArrayList<StarSystemAPI> getSystemsWithPlayerFactionColonies() {
        ArrayList<StarSystemAPI> systems = new ArrayList<>();
        for (StarSystemAPI starSystem : Global.getSector().getStarSystems()) {
            if (Global.getSector().getEconomy().getMarkets(starSystem.getCenter().getContainingLocation()).stream().anyMatch(x -> x.getFaction() != null && x.getFaction().isPlayerFaction())) {
                systems.add(starSystem);
            }
        }
        return systems;
    }

    public static ArrayList<MarketAPI> getFactionMarketsInSystem(FactionAPI faction, StarSystemAPI system) {
        ArrayList<MarketAPI> systems = new ArrayList<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
            if (market.getFaction() != null && market.getFaction().getId().equals(faction.getId())) {
                systems.add(market);
            }
        }
        return systems;
    }

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


    // Sort by Build Time (Days)
    public static void sortDropDownButtonsFPGenerated(ArrayList<DropDownButton> buttons, final boolean ascending) {
        Collections.sort(buttons, new Comparator<DropDownButton>() {
            @Override
            public int compare(DropDownButton button1, DropDownButton button2) {
                float days1 = calculateFPGenerated(button1);
                float days2 = calculateFPGenerated(button2);
                return ascending ? Float.compare(days1, days2) : Float.compare(days2, days1);
            }
        });
    }
    public static void sortDropDownButtonsIncome(ArrayList<DropDownButton> buttons, final boolean ascending) {
        Collections.sort(buttons, new Comparator<DropDownButton>() {
            @Override
            public int compare(DropDownButton button1, DropDownButton button2) {
                float days1 = calculateIncome(button1);
                float days2 = calculateIncome(button2);
                return ascending ? Float.compare(days1, days2) : Float.compare(days2, days1);
            }
        });
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
    private static String getButtonName(DropDownButton button) {
        if(button instanceof PatrolFleetHoldingsDropDown hl){
            return hl.getStarSystem().getName();
        }
        if(button instanceof StarSystemHoldingDropDown hl){
            return hl.getStarSystem().getName();
        }
        return "";
    }
    private static float calculateFPGenerated(DropDownButton button) {
        if(button instanceof PatrolFleetHoldingsDropDown hl){
            return FleetPointUtilis.getFleetPointsGeneratedByStarSystem(hl.getStarSystem(),Global.getSector().getPlayerFaction());
        }
        return 0f;
    }
    private static float calculateIncome(DropDownButton button) {
        float income = 0f;
        if(button instanceof StarSystemHoldingDropDown hl){
            for (MarketAPI market : hl.getMarkets()) {
                income+=market.getNetIncome();
            }
        }
        return income;
    }
    private static float calculateFPTaken(DropDownButton button) {
        if(button instanceof PatrolFleetHoldingsDropDown hl){
            return FleetPointUtilis.getFleetPointsTakenByStarSystem(hl.getStarSystem(),Global.getSector().getPlayerFaction());
        }
        return 0f;
    }
}
