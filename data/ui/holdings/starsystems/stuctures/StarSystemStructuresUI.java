package data.ui.holdings.starsystems.stuctures;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;

public class StarSystemStructuresUI implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel, contentPanel;
    StarSystemAPI system;

    public StarSystemStructuresUI(float width, float height, StarSystemAPI system) {
        this.system = system;
        this.mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
    }

    public void setSystem(StarSystemAPI system) {
        this.system = system;
        createUI();
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        if (contentPanel != null) {
            mainPanel.removeComponent(contentPanel);
        }

        contentPanel = Global.getSettings().createCustom(
                mainPanel.getPosition().getWidth(),
                mainPanel.getPosition().getHeight(),
                null
        );

        TooltipMakerAPI tooltip = contentPanel.createUIElement(
                contentPanel.getPosition().getWidth(),
                contentPanel.getPosition().getHeight() - 30,
                true
        );
        TooltipMakerAPI tooltipBottom = contentPanel.createUIElement(
                contentPanel.getPosition().getWidth(),
                25,
                false
        );
        float separator = 10;              // fixed
        float rowGap = 20;                 // vertical gap between rows (tweak)
        float availableWidth = contentPanel.getPosition().getWidth();

        float widgetW = StableStructureWidget.width;
        float widgetH = StableStructureWidget.height; // <-- use your widget height constant

        List<SectorEntityToken> objs = system.getEntitiesWithTag(Tags.OBJECTIVE);
        int total = objs.size();


        int perRow = (int) Math.floor((availableWidth + separator) / (widgetW + separator));
        perRow = Math.max(1, perRow);

        int rowsPlaced = (total + perRow - 1) / perRow;

        // Place
        for (int i = 0; i < total; i++) {
            int row = i / perRow;
            int col = i % perRow;


            int rowStartIndex = row * perRow;
            int remaining = total - rowStartIndex;
            int itemsThisRow = Math.min(perRow, remaining);

            float rowW = itemsThisRow * widgetW + (itemsThisRow - 1) * separator;
            float startX = Math.round((availableWidth - rowW) * 0.5f);  // centered

            float x = startX + col * (widgetW + separator);
            float y = row * (widgetH + rowGap);

            SectorEntityToken token = objs.get(i);
            StableStructureWidget widget = new StableStructureWidget(token);

            tooltip.addCustom(widget.getMainPanel(), 0f).getPosition().inTL(x, y);
        }


        float neededHeight = rowsPlaced * widgetH + Math.max(0, rowsPlaced - 1) * rowGap;
        tooltip.setHeightSoFar(neededHeight);
        tooltipBottom.setParaFont(Fonts.INSIGNIA_LARGE);
        LabelAPI label = tooltipBottom.addPara("System Structures: %s / %s", 0f, Misc.getGrayColor(), Color.ORANGE, "" + total, "5");
        label.getPosition().inTL(contentPanel.getPosition().getWidth() - label.computeTextWidth(label.getText()) - 5, 1);
        float x = contentPanel.getPosition().getWidth() - label.computeTextWidth(label.getText()) - 5;
        tooltipBottom.setButtonFontOrbitron20();
        ButtonAPI bt = tooltipBottom.addButton("Add System Structure", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, Math.min(x-20, 300), 25, 0f);
        bt.setShortcut(Keyboard.KEY_A, true);
        bt.getPosition().inTL(5, 0);


        contentPanel.addUIElement(tooltip).inTL(0, 0f);
        contentPanel.addUIElement(tooltipBottom).inTL(0, contentPanel.getPosition().getHeight() - 20);
        mainPanel.addComponent(contentPanel).inTL(0f, 0f);
    }


    @Override
    public void clearUI() {

    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
