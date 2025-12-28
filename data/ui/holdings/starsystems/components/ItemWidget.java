package data.ui.holdings.starsystems.components;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.*;
import java.util.stream.Collectors;

public class ItemWidget implements ExtendedUIPanelPlugin {

    private CustomPanelAPI mainPanel;
    private CustomPanelAPI contentPanel;
    private final MarketAPI market;

    public ItemWidget(float width, float height, MarketAPI market) {
        this.market = market;
        this.mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    private static final class IconEntry {
        final String id;
        final boolean isCommodity; // true = AI core commodity, false = special item

        IconEntry(String id, boolean isCommodity) {
            this.id = id;
            this.isCommodity = isCommodity;
        }

        String getIconName() {
            return isCommodity
                    ? Global.getSettings().getCommoditySpec(id).getIconName()
                    : Global.getSettings().getSpecialItemSpec(id).getIconName();
        }
    }

    @Override
    public void createUI() {
        if (contentPanel != null) {
            mainPanel.removeComponent(contentPanel);
        }

        float panelW = mainPanel.getPosition().getWidth();
        float panelH = mainPanel.getPosition().getHeight();
        contentPanel = Global.getSettings().createCustom(panelW, panelH, null);
        mainPanel.addComponent(contentPanel).inTL(0f, 0f);

        // Layout knobs
        float maxIconSize = 22f;
        float gapDifferent = 2f;
        float gapSame = -12f; // overlap/stack identical items

        LinkedHashMap<String, Integer> aiCores = collectAndSortAICores(market);
        LinkedHashMap<String, Integer> items = collectAndSortSpecialItems(market);

        List<IconEntry> icons = new ArrayList<>();
        addExpanded(icons, aiCores, true);
        addExpanded(icons, items, false);

        if (icons.isEmpty()) return;

        float iconSize = computeBestIconSize(icons, panelW, panelH, maxIconSize, gapDifferent, gapSame);
        layoutIconsCentered(icons, iconSize, panelW, panelH, gapDifferent, gapSame);
    }

    private void addExpanded(List<IconEntry> out, LinkedHashMap<String, Integer> map, boolean isCommodity) {
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            int count = (e.getValue() == null) ? 0 : e.getValue();
            if (count <= 0) continue;

            for (int i = 0; i < count; i++) {
                out.add(new IconEntry(e.getKey(), isCommodity));
            }
        }
    }

    private float computeBestIconSize(
            List<IconEntry> icons,
            float width,
            float height,
            float maxSize,
            float gapDifferent,
            float gapSame
    ) {
        // Fast-path: max fits
        if (fits(icons, width, height, maxSize, gapDifferent, gapSame)) return maxSize;

        // Binary search for the largest size that fits
        float lo = 6f;
        float hi = maxSize;

        for (int i = 0; i < 18; i++) {
            float mid = (lo + hi) * 0.5f;
            if (fits(icons, width, height, mid, gapDifferent, gapSame)) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    private boolean fits(
            List<IconEntry> icons,
            float width,
            float height,
            float size,
            float gapDifferent,
            float gapSame
    ) {
        if (size > width || size > height) return false;

        float x = 0f;
        float y = 0f;

        String prevId = null;

        for (IconEntry icon : icons) {
            float gap = computeGap(x, prevId, icon.id, gapDifferent, gapSame);

            // wrap
            if (x > 0f && x + gap + size > width) {
                x = 0f;
                y += size + gapDifferent; // row gap
                gap = 0f;
            }

            if (y + size > height) return false;

            x += gap + size;
            prevId = icon.id;
        }

        return true;
    }
    private void layoutIconsCentered(
            List<IconEntry> icons,
            float size,
            float width,
            float height,
            float gapDifferent,
            float gapSame
    ) {
        // ---- Pass A: build rows + row widths ----
        class Row {
            final List<IconEntry> entries = new ArrayList<>();
            float rowWidth = 0f; // measured as "x after last icon" (size + gaps)
        }

        List<Row> rows = new ArrayList<>();
        Row current = new Row();

        float x = 0f;
        String prevId = null;

        for (IconEntry icon : icons) {
            float gap = computeGap(x, prevId, icon.id, gapDifferent, gapSame);

            // wrap if this icon would exceed width
            if (x > 0f && x + gap + size > width) {
                current.rowWidth = x;
                rows.add(current);

                current = new Row();
                x = 0f;
                prevId = null;
                gap = 0f; // first in row
            }

            current.entries.add(icon);
            x += gap + size;
            prevId = icon.id;
        }

        if (!current.entries.isEmpty()) {
            current.rowWidth = x;
            rows.add(current);
        }

        // ---- Pass B: place each row centered ----
        float y = 0f;
        for (Row row : rows) {
            if (y + size > height) break;

            float startX = (width - row.rowWidth) * 0.5f;
            // Should be >= 0 because we "fit()", but keep it safe:
            if (startX < 0f) startX = 0f;

            float rowX = 0f;
            String rowPrevId = null;

            for (IconEntry icon : row.entries) {
                float gap = computeGap(rowX, rowPrevId, icon.id, gapDifferent, gapSame);

                ImageViewer viewer = new ImageViewer(size, size, icon.getIconName());
                contentPanel.addComponent(viewer.getComponentPanel()).inTL(startX + rowX + gap, y);

                rowX += gap + size;
                rowPrevId = icon.id;
            }

            y += size + gapDifferent; // row spacing
        }
    }

    private void layoutIcons(
            List<IconEntry> icons,
            float size,
            float width,
            float height,
            float gapDifferent,
            float gapSame
    ) {
        float x = 0f;
        float y = 0f;

        String prevId = null;

        for (IconEntry icon : icons) {
            float gap = computeGap(x, prevId, icon.id, gapDifferent, gapSame);

            // wrap
            if (x > 0f && x + gap + size > width) {
                x = 0f;
                y += size + gapDifferent;
                gap = 0f;
            }

            if (y + size > height) break; // should not happen if size came from computeBestIconSize()

            ImageViewer viewer = new ImageViewer(size, size, icon.getIconName());
            contentPanel.addComponent(viewer.getComponentPanel()).inTL(x + gap, y);

            x += gap + size;
            prevId = icon.id;
        }
    }

    private float computeGap(float x, String prevId, String id, float gapDifferent, float gapSame) {
        if (x <= 0f) return 0f; // first in row
        if (prevId == null) return gapDifferent;
        return prevId.equals(id) ? gapSame : gapDifferent;
    }

    @Override
    public void clearUI() {
    }

    public LinkedHashMap<String, Integer> collectAndSortAICores(MarketAPI market) {
        Map<String, Integer> counts = new HashMap<>();

        for (Industry industry : market.getIndustries()) {
            String aiCoreId = industry.getAICoreId();
            if (AshMisc.isStringValid(aiCoreId)) {
                counts.merge(aiCoreId, 1, Integer::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted((a, b) -> {
                    CommoditySpecAPI specA = Global.getSettings().getCommoditySpec(a.getKey());
                    CommoditySpecAPI specB = Global.getSettings().getCommoditySpec(b.getKey());
                    float orderA = specA != null ? specA.getOrder() : 0f;
                    float orderB = specB != null ? specB.getOrder() : 0f;
                    return Float.compare(orderB, orderA);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public LinkedHashMap<String, Integer> collectAndSortSpecialItems(MarketAPI market) {
        Map<String, Integer> counts = new HashMap<>();

        for (Industry industry : market.getIndustries()) {
            SpecialItemData data = industry.getSpecialItem();
            if (data == null) continue;

            String id = data.getId();
            if (id == null || id.isEmpty()) continue;

            counts.merge(id, 1, Integer::sum);
        }

        return counts.entrySet().stream()
                .sorted((a, b) -> {
                    SpecialItemSpecAPI specA = Global.getSettings().getSpecialItemSpec(a.getKey());
                    SpecialItemSpecAPI specB = Global.getSettings().getSpecialItemSpec(b.getKey());
                    float orderA = specA != null ? specA.getOrder() : 0f;
                    float orderB = specB != null ? specB.getOrder() : 0f;
                    return Float.compare(orderB, orderA);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override public void positionChanged(PositionAPI position) {}
    @Override public void renderBelow(float alphaMult) {}
    @Override public void render(float alphaMult) {}
    @Override public void advance(float amount) {}
    @Override public void processInput(List<InputEventAPI> events) {}
    @Override public void buttonPressed(Object buttonId) {}
}
