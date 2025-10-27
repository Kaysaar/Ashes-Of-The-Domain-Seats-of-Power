package data.ui.patrolfleet.templates;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplateShowcaseList implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel, componentPanel;


    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public TemplateShowcaseList(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();

    }

    @Override
    public void createUI() {
        createUIImpl(PatrolTemplateManager.templates);

    }

    public void createUIImpl(LinkedHashMap<String, BasePatrolFleetTemplate> filtered) {
        if (componentPanel != null) {
            mainPanel.removeComponent(componentPanel);
        }
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), null);
        TooltipMakerAPI list = componentPanel.createUIElement(componentPanel.getPosition().getWidth(), componentPanel.getPosition().getHeight(), true);
        float opad = 1f;
        float width = Math.max(610, (componentPanel.getPosition().getWidth() - 10) / 2);
        int columns = (int) Math.floor((componentPanel.getPosition().getWidth() - 10) / width);
        if (columns == 1) {
            width = componentPanel.getPosition().getWidth() - 10;
            for (Map.Entry<String, BasePatrolFleetTemplate> entry : filtered.entrySet()) {
                TemplateShowcase showcase = new TemplateShowcase(entry.getValue(), width, 185, this);
                showcase.createUI();
                list.addCustom(showcase.getMainPanel(), opad);
                opad = 5;
            }
        } else {
            float availW = componentPanel.getPosition().getWidth() - 8f;
            float cardH = 185f;
            columns = Math.max(1, columns);
            float cardW = (availW - (columns)) / columns;         // base width if we don't care about equal gaps
            float usedW = cardW * columns;
            float remainW = Math.max(0f, availW - usedW);
            float gap = columns > 1 ? (remainW / (columns - 1)) : 3f;
            int col = 0;
            CustomPanelAPI row = null;

            for (Map.Entry<String, BasePatrolFleetTemplate> e : filtered.entrySet()) {

                // Start a new row when needed
                if (row == null) {
                    row = Global.getSettings().createCustom(availW, cardH, null);
                }

                // Build the showcase panel (one card)
                TemplateShowcase showcase = new TemplateShowcase(e.getValue(), cardW, cardH, this);
                showcase.createUI();
                CustomPanelAPI card = showcase.getMainPanel();

                // Position within the row
                float x = col * (cardW + gap);
                row.addComponent(card).inTL(x, 0f);

                col++;

                // If row is full, add it to the tooltip and reset
                if (col >= columns) {
                    list.addCustom(row, opad);
                    opad = 5f; // subsequent rows tighter
                    row = null;
                    col = 0;
                }
            }
            if (row != null) {
                list.addCustom(row, opad);
                // opad = 5f; // not strictly needed after the last add
            }

        }

        componentPanel.addUIElement(list).inTL(0, 0);
        mainPanel.addComponent(componentPanel).inTL(0, 0);

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
