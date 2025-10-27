package data.ui.patrolfleet.templates.shiplist.components;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ShipPanelSorter {
    public static ArrayList<ShipHullSpecAPI> getShipPackagesBasedOnTags(ArrayList<String> manufacturues, ArrayList<String> sizes, ArrayList<String> types) {
        boolean allMan = ShipPanelData.arrayContains(manufacturues, "All designs") || manufacturues.isEmpty();
        boolean allSizes = ShipPanelData.arrayContains(sizes, "All sizes") || sizes.isEmpty();
        boolean allTypes = ShipPanelData.arrayContains(types, "All types") || types.isEmpty();
        ArrayList<ShipHullSpecAPI> options = new ArrayList<>();
        if (allMan && allSizes && allTypes)
            return getShipPackagesBasedOnData("Cost", SortingState.ASCENDING, ShipPanelData.learnedShips);
        for (ShipHullSpecAPI learnedShipPackage : getShipPackagesBasedOnData("Cost", SortingState.ASCENDING, ShipPanelData.learnedShips)) {
            boolean valid = true;
            if (!allMan) {
                valid = false;
                for (String manufacturue : manufacturues) {
                    if (learnedShipPackage.getManufacturer().equals(manufacturue)) {
                        valid = true;
                        break;
                    }
                }
            }
            if (!valid) continue;
            if (!allSizes) {
                valid = false;
                for (String s : sizes) {
                    if (Misc.getHullSizeStr(learnedShipPackage.getHullSize()).equals(s)) {
                        valid = true;
                        break;
                    }
                }
            }
            if (!valid) continue;
            if (!allTypes) {
                valid = false;
                for (String s : types) {
                    if (ShipPanelData.getType(learnedShipPackage).equals(s)) {
                        valid = true;
                        break;
                    }
                }
            }
            if (valid) {
                options.add(learnedShipPackage);
            }

        }
        return options;
    }


    public static ArrayList<ShipHullSpecAPI> getShipPackagesBasedOnData(String nameOfSort, SortingState sortingState, ArrayList<ShipHullSpecAPI> temp) {
        ArrayList<ShipHullSpecAPI> packages = new ArrayList<>(temp);
        Comparator<ShipHullSpecAPI> comparator = null;
        if (nameOfSort.equals("Name")) {
            comparator = new Comparator<ShipHullSpecAPI>() {
                @Override
                public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                    String s1 = o1.getHullName();
                    String s2 = o2.getHullName();
                    return s1.compareTo(s2);
                }
            };
        }
        if (nameOfSort.equals("Size")) {
            comparator = new Comparator<ShipHullSpecAPI>() {
                @Override
                public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                    String s1 = Misc.getHullSizeStr(o1.getHullSize());
                    String s2 = Misc.getHullSizeStr(o2.getHullSize());
                    return s1.compareTo(s2);
                }
            };
        }
        if (nameOfSort.equals("Type")) {
            comparator = new Comparator<ShipHullSpecAPI>() {
                @Override
                public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                    String s1 = ShipPanelData.getType(o1);
                    String s2 = ShipPanelData.getType(o2);
                    return s1.compareTo(s2);
                }
            };
        }
        if (nameOfSort.equals("Design Type")) {
            comparator = new Comparator<ShipHullSpecAPI>() {
                @Override
                public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                    String s1 = o1.getManufacturer();
                    String s2 = o2.getManufacturer();
                    s1 = ShipPanelData.ensureManBeingNotNull(s1);
                    s2 = ShipPanelData.ensureManBeingNotNull(s2);
                    return s1.compareTo(s2);
                }
            };
        }
        if (nameOfSort.equals("Cost")) {
            comparator = new Comparator<ShipHullSpecAPI>() {
                @Override
                public int compare(ShipHullSpecAPI o1, ShipHullSpecAPI o2) {
                    float price1 = o1.getBaseValue();
                    float price2 = o2.getBaseValue();
                    return Float.compare(price1, price2);
                }
            };
        }
        if (sortingState == SortingState.DESCENDING) {
            Collections.sort(packages, comparator);
        }
        if (sortingState == SortingState.ASCENDING) {
            Collections.sort(packages, Collections.reverseOrder(comparator));
        }
        return packages;
    }
    public static ArrayList<ShipHullSpecAPI> getMatchingShipGps(String value) {
        ArrayList<ShipHullSpecAPI> options = new ArrayList<>();
        int threshold = 2; // Adjust the threshold based on your tolerance for misspellings
        ShipSearchBarStringComparator comparator = new ShipSearchBarStringComparator(value, threshold);
        for (ShipHullSpecAPI learnedShipPackage : ShipPanelData.learnedShips) {
            if (comparator.isValid(learnedShipPackage.getHullName())) {
                options.add(learnedShipPackage);
            }
        }
        Collections.sort(options, comparator);
        return options;
    }
}
