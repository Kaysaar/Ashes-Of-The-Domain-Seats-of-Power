package data.ui.patrolfleet.overview.dialogs;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.plugins.UITableImpl;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.tot.produciton.AoTDProductionUIData;
import data.kaysaar.aotd.tot.produciton.specs.AoTDProductionSpec;
import data.kaysaar.aotd.tot.ui.customprod.common.ChooseManufacturerPanel;
import data.kaysaar.aotd.tot.ui.customprod.common.ProductionOptionList;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionBrowserSection;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionCustomButton;
import data.kaysaar.aotd.vok.misc.AoTDMisc;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArmoryBrowserSection extends ProductionBrowserSection {

    protected final LinkedHashMap<String, Boolean> priorityCache = new LinkedHashMap<>();

    protected ButtonAPI selectAllMatchingButton;
    protected ButtonAPI unselectAllMatchingButton;
    protected ButtonAPI selectAllButton;
    protected ButtonAPI unselectAllButton;

    protected static final float BULK_BUTTON_WIDTH = 200f;
    protected static final float BULK_BUTTON_HEIGHT = 20f;
    protected static final float BULK_BUTTON_GAP = 5f;
    protected static final float BULK_BUTTON_SECTION_HEIGHT = 25f;
    protected static final float PRIORITY_COLUMN_WIDTH = 150f;

    public ArmoryBrowserSection(float width, float height, AoTDProductionSpec.AoTDProductionSpecType prodType) {
        super(width, height, prodType);
    }

    @Override
    protected ProductionOptionList createProductionOptionList(List<AoTDProductionSpec> specs, float width, float height, AoTDProductionSpec.AoTDProductionSpecType prodType) {
        final ArmoryBrowserSection section = this;
        return new ProductionOptionList(specs, width, height,27, prodType) {
            public ProductionCustomButton createButtonForType(AoTDProductionSpec.AoTDProductionSpecType type, float width, float height, AoTDProductionSpec spec) {
                return section.createProductionButtonForType(type, width, height, spec);
            }
        };
    }

    @Override
    protected List<ColumnDefinition> createColumnDefinitions(AoTDProductionSpec.AoTDProductionSpecType prodType) {
        ArrayList<ColumnDefinition> columns = new ArrayList<>();

        /*
         * Around 1000px target:
         * Name      ~31%
         * Size      ~10%
         * Type      ~12%
         * Priority  ~150px
         * Design    remaining space
         */
        float width = mainPanel == null ? 1000f : mainPanel.getPosition().getWidth();
        float usableWidth = Math.max(1f, width - 10f);

        float priorityWidth = PRIORITY_COLUMN_WIDTH;
        float nameWidth = usableWidth * 0.31f;
        float sizeWidth = usableWidth * 0.10f;
        float typeWidth = usableWidth * 0.12f;
        float designWidth = Math.max(1f, usableWidth - nameWidth - sizeWidth - typeWidth - priorityWidth);

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
                Comparator.comparingInt(o -> getSizeSortRank(o.getSpec()))
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
                designWidth,
                Comparator.comparing(o -> o.getSpec().getManufacturer())
        ));

        columns.add(new ColumnDefinition(
                "priority",
                "Priority",
                priorityWidth,
                new Comparator<ProductionCustomButton>() {
                    @Override
                    public int compare(ProductionCustomButton o1, ProductionCustomButton o2) {
                        boolean p1 = isPriorityCached(o1.getSpec());
                        boolean p2 = isPriorityCached(o2.getSpec());

                        return Boolean.compare(p2, p1);
                    }
                }
        ));

        return columns;
    }

    @Override
    protected String getInitialSortColumnId() {
        return "priority";
    }

    @Override
    protected UITableImpl.SortingState getInitialSortStateBeforeFirstToggle() {
        return UITableImpl.SortingState.NON_INITIALIZED;
    }

    @Override
    protected float getManufacturerPanelHeight() {
        return 120 + BULK_BUTTON_SECTION_HEIGHT;
    }

    @Override
    protected void createManufacturerPanel(float sectionsHeight, float heightOfManus) {
        createBulkSelectionButtons(sectionsHeight);

        float manufacturerY = 55f + sectionsHeight + 5f + BULK_BUTTON_SECTION_HEIGHT;
        float manufacturerHeight = Math.max(0f, heightOfManus - BULK_BUTTON_SECTION_HEIGHT);

        chooseManufacturerPanel = new ChooseManufacturerPanel(
                contentPanel.getPosition().getWidth(),
                manufacturerHeight,
                AoTDProductionUIData.getManInfoBasedOnType(prodType)
        );

        contentPanel.addComponent(chooseManufacturerPanel.getMainPanel()).inTL(0, manufacturerY);
    }

    protected void createBulkSelectionButtons(float sectionsHeight) {
        float buttonY = 55f + sectionsHeight + 5f;

        TooltipMakerAPI tooltip = contentPanel.createUIElement(
                contentPanel.getPosition().getWidth(),
                BULK_BUTTON_SECTION_HEIGHT,
                false
        );

        selectAllMatchingButton = tooltip.addButton(
                "Select All Matching",
                "select_all_matching",
                Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(),
                Alignment.MID,
                CutStyle.BOTTOM,
                BULK_BUTTON_WIDTH,
                BULK_BUTTON_HEIGHT,
                0f
        );
        selectAllMatchingButton.setShortcut(Keyboard.KEY_S,false);

        selectAllMatchingButton.getPosition().inTL(0, 0);

        unselectAllMatchingButton = tooltip.addButton(
                "Unselect All Matching",
                "unselect_all_matching",
                Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(),
                Alignment.MID,
                CutStyle.BOTTOM,
                BULK_BUTTON_WIDTH,
                BULK_BUTTON_HEIGHT,
                0f
        );
        unselectAllMatchingButton.setShortcut(Keyboard.KEY_D,false);
        unselectAllMatchingButton.getPosition().rightOfMid(selectAllMatchingButton, BULK_BUTTON_GAP);

        selectAllButton = tooltip.addButton(
                "Select All",
                "select_all",
                Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(),
                Alignment.MID,
                CutStyle.BOTTOM,
                BULK_BUTTON_WIDTH,
                BULK_BUTTON_HEIGHT,
                0f
        );
        selectAllButton.getPosition().rightOfMid(unselectAllMatchingButton, BULK_BUTTON_GAP);

        unselectAllButton = tooltip.addButton(
                "Unselect All",
                "unselect_all",
                Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(),
                Alignment.MID,
                CutStyle.BOTTOM,
                BULK_BUTTON_WIDTH,
                BULK_BUTTON_HEIGHT,
                0f
        );
        unselectAllButton.getPosition().rightOfMid(selectAllButton, BULK_BUTTON_GAP);

        contentPanel.addUIElement(tooltip).inTL(0, buttonY);
    }

    @Override
    protected ProductionCustomButton createProductionButtonForType(
            AoTDProductionSpec.AoTDProductionSpecType type,
            float width,
            float height,
            AoTDProductionSpec spec
    ) {
        return new ArmoryTabButton(width, height, spec, getColumnLayout(), this);
    }

    @Override
    public CustomButton.ButtonEventListener createListenerForButton(ProductionCustomButton customButton) {
        return new CustomButton.ButtonEventListener() {
            @Override
            public void onButtonClicked() {
                togglePriorityCached(customButton.getSpec());
                customButton.createUI();
            }
        };
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        handleBulkButtons();
    }

    protected void handleBulkButtons() {
        if (selectAllMatchingButton != null && selectAllMatchingButton.isChecked()) {
            selectAllMatchingButton.setChecked(false);
            setPriorityForSpecs(getMatchingSpecs(), true);
            recreateList();
        }

        if (unselectAllMatchingButton != null && unselectAllMatchingButton.isChecked()) {
            unselectAllMatchingButton.setChecked(false);
            setPriorityForSpecs(getMatchingSpecs(), false);
            recreateList();
        }

        if (selectAllButton != null && selectAllButton.isChecked()) {
            selectAllButton.setChecked(false);
            setPriorityForSpecs(getSpecsToShow(), true);
            recreateList();
        }

        if (unselectAllButton != null && unselectAllButton.isChecked()) {
            unselectAllButton.setChecked(false);
            setPriorityForSpecs(getSpecsToShow(), false);
            recreateList();
        }
    }

    protected void recreateList() {
        list.getButtonsStorage().forEach(CustomButton::createUI);
    }

    protected List<AoTDProductionSpec> getMatchingSpecs() {
        ArrayList<AoTDProductionSpec> result = new ArrayList<>();

        String rawSearch = textField == null ? null : textField.getText();

        if (AshMisc.isStringValid(rawSearch)) {
            String searchString = rawSearch.toLowerCase().trim();
            int threshold = getSearchLevenshteinThreshold();

            for (AoTDProductionSpec spec : getSpecsToShow()) {
                String candidate = getSearchString(spec);
                if (candidate == null) continue;

                String normalized = candidate.toLowerCase();
                int distance = AoTDMisc.levenshteinDistance(searchString, normalized);

                if (distance <= threshold || normalized.contains(searchString)) {
                    result.add(spec);
                }
            }

            return result;
        }

        for (AoTDProductionSpec spec : getSpecsToShow()) {
            if (isPassingFilterPanels(spec)) {
                result.add(spec);
            }
        }

        return result;
    }

    protected boolean isPassingFilterPanels(AoTDProductionSpec spec) {
        if (spec == null) return false;

        if (chooseManufacturerPanel != null && !chooseManufacturerPanel.isManufacturerChosen(spec.getManufacturer())) {
            return false;
        }

        if (chooseTypeInfo != null && !chooseTypeInfo.isTypeChosen(spec.getTypeString())) {
            return false;
        }

        if (chooseSizePanel != null && !chooseSizePanel.isSizeChosen(spec.getSize())) {
            return false;
        }

        return true;
    }

    protected String getSearchString(AoTDProductionSpec spec) {
        if (spec == null) return null;
        return spec.getName();
    }

    protected void setPriorityForSpecs(List<AoTDProductionSpec> specs, boolean priority) {
        if (specs == null) return;

        for (AoTDProductionSpec spec : specs) {
            setPriorityCached(spec, priority);
        }
    }

    public boolean isPriorityCached(AoTDProductionSpec spec) {
        if (spec == null) return false;

        String key = getCacheKey(spec);
        if (!priorityCache.containsKey(key)) {
            priorityCache.put(key, isPriorityOnFaction(Global.getSector().getPlayerFaction(), spec));
        }

        return Boolean.TRUE.equals(priorityCache.get(key));
    }

    public void setPriorityCached(AoTDProductionSpec spec, boolean priority) {
        if (spec == null) return;

        priorityCache.put(getCacheKey(spec), priority);
    }

    public void togglePriorityCached(AoTDProductionSpec spec) {
        if (spec == null) return;

        setPriorityCached(spec, !isPriorityCached(spec));
    }

    protected String getCacheKey(AoTDProductionSpec spec) {
        return spec.getProductionType().name() + "|" + spec.getId();
    }

    protected boolean isPriorityOnFaction(FactionAPI faction, AoTDProductionSpec spec) {
        if (faction == null || spec == null) return false;

        switch (spec.getProductionType()) {
            case SHIP:
                return faction.isShipPriority(spec.getId());
            case WEAPON:
                return faction.isWeaponPriority(spec.getId());
            case FIGHTER:
                return faction.isFighterPriority(spec.getId());
            default:
                return false;
        }
    }

    /**
     * Call this from your dialog confirm button.
     *
     * Until this runs, all changes are UI/cache-only.
     */
    public void confirmRun() {
        FactionAPI faction = Global.getSector().getPlayerFaction();
        if (faction == null) return;

        for (Map.Entry<String, Boolean> entry : priorityCache.entrySet()) {
            String key = entry.getKey();
            Boolean priority = entry.getValue();

            if (key == null || priority == null) continue;

            String[] split = key.split("\\|", 2);
            if (split.length != 2) continue;

            AoTDProductionSpec.AoTDProductionSpecType type;
            try {
                type = AoTDProductionSpec.AoTDProductionSpecType.valueOf(split[0]);
            } catch (Exception ex) {
                continue;
            }

            String id = split[1];
            applyPriorityToFaction(faction, type, id, priority);
        }
    }

    protected void applyPriorityToFaction(
            FactionAPI faction,
            AoTDProductionSpec.AoTDProductionSpecType type,
            String id,
            boolean priority
    ) {
        if (faction == null || type == null || id == null) return;

        switch (type) {
            case SHIP:
                if (priority) {
                    faction.addPriorityShip(id);
                } else {
                    faction.removePriorityShip(id);
                }
                break;

            case WEAPON:
                if (priority) {
                    faction.addPriorityWeapon(id);
                } else {
                    faction.removePriorityWeapon(id);
                }
                break;

            case FIGHTER:
                if (priority) {
                    faction.addPriorityFighter(id);
                } else {
                    faction.removePriorityFighter(id);
                }
                break;

            default:
                break;
        }
    }

    protected int getSizeSortRank(AoTDProductionSpec spec) {
        if (spec == null) return Integer.MAX_VALUE;

        Object underlying = spec.getUnderlyingSpec();

        if (underlying instanceof ShipHullSpecAPI) {
            return hullSizeRank(((ShipHullSpecAPI) underlying).getHullSize());
        }

        if (underlying instanceof WeaponSpecAPI) {
            return weaponSizeRank(((WeaponSpecAPI) underlying).getSize());
        }
        return Integer.MAX_VALUE;
    }

}