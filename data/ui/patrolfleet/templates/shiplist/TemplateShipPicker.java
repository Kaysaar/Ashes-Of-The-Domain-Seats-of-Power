package data.ui.patrolfleet.templates.shiplist;

import ashlib.data.plugins.ui.plugins.UITableImpl;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import data.kaysaar.aotd.tot.produciton.specs.AoTDProductionSpec;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionBrowserSection;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionCustomButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TemplateShipPicker extends ProductionBrowserSection {

    public TemplateShipPicker(float width, float height) {
        super(width, height, AoTDProductionSpec.AoTDProductionSpecType.SHIP);
    }

    @Override
    protected List<ColumnDefinition> createColumnDefinitions(AoTDProductionSpec.AoTDProductionSpecType prodType) {
        ArrayList<ColumnDefinition> columns = new ArrayList<>();

        /*
         * Old layout equivalent:
         *
         * WIDTH_OF_NAME = WIDTH_OF_OPTIONS * 0.36f;
         * WIDTH_OF_SIZE = WIDTH_OF_OPTIONS * 0.12f;
         * WIDTH_OF_TYPE = WIDTH_OF_OPTIONS * 0.12f;
         * WIDTH_OF_DESIGN_TYPE = (WIDTH_OF_OPTIONS * 0.40f) - 80;
         * WIDTH_OF_FP = 80;
         *
         * ColumnLayout works with ratios, but ratio values do not need to sum to 1.
         * So here we feed it pixel-like weights based on current width.
         */
        float width = mainPanel == null ? 1f : mainPanel.getPosition().getWidth();
        float usableWidth = Math.max(1f, width - 10f);

        float fpWidth = 80f;
        float nameWidth = usableWidth * 0.36f;
        float sizeWidth = usableWidth * 0.12f;
        float typeWidth = usableWidth * 0.12f;
        float designTypeWidth = Math.max(1f, usableWidth * 0.40f - fpWidth);

        columns.add(new ColumnDefinition(
                "name",
                "Name",
                nameWidth,
                Comparator.comparing(o -> o.getSpec().getName())
        ));

        columns.add(new ColumnDefinition(
                "size",
                "Size",
                sizeWidth,
                Comparator.comparingInt(o -> {
                    ShipHullSpecAPI ship = getShipSpec(o.getSpec());
                    if (ship == null) return Integer.MAX_VALUE;
                    return hullSizeRank(ship.getHullSize());
                })
        ));

        columns.add(new ColumnDefinition(
                "type",
                "Type",
                typeWidth,
                Comparator.comparing(o -> o.getSpec().getTypeString())
        ));

        columns.add(new ColumnDefinition(
                "design",
                "Design type",
                designTypeWidth,
                Comparator.comparing(o -> o.getSpec().getManufacturer())
        ));

        columns.add(new ColumnDefinition(
                "fp",
                "FP",
                fpWidth,
                new Comparator<ProductionCustomButton>() {
                    @Override
                    public int compare(ProductionCustomButton o1, ProductionCustomButton o2) {
                        ShipHullSpecAPI ship1 = getShipSpec(o1.getSpec());
                        ShipHullSpecAPI ship2 = getShipSpec(o2.getSpec());

                        float price1 = ship1 == null ? 0f : ship1.getFleetPoints();
                        float price2 = ship2 == null ? 0f : ship2.getFleetPoints();

                        return Float.compare(price2, price1);
                    }
                }
        ));

        return columns;
    }

    @Override
    protected String getInitialSortColumnId() {
        return "fp";
    }

    @Override
    protected UITableImpl.SortingState getInitialSortStateBeforeFirstToggle() {
        /*
         * If your AshMisc.switchState(NON_INITIALIZED) returns ASCENDING,
         * this makes initial FP sort low -> high.
         *
         * If your local switchState behaves differently, change this back to ASCENDING.
         */
        return UITableImpl.SortingState.NON_INITIALIZED;
    }

    @Override
    protected boolean shouldShowTypeChooser() {
        return true;
    }

    @Override
    protected boolean shouldShowSizeChooser() {
        return true;
    }


    @Override
    protected float getManufacturerPanelHeight() {
        return 100;
    }

    @Override
    protected ProductionCustomButton createProductionButtonForType(
            AoTDProductionSpec.AoTDProductionSpecType type,
            float width,
            float height,
            AoTDProductionSpec spec
    ) {
        return new TemplateShipPickerButton(width, height, spec, getColumnLayout());
    }

    protected ShipHullSpecAPI getShipSpec(AoTDProductionSpec spec) {
        if (spec == null) return null;

        if (spec.getUnderlyingSpec() instanceof ShipHullSpecAPI) {
            return (ShipHullSpecAPI) spec.getUnderlyingSpec();
        }

        return null;
    }
}