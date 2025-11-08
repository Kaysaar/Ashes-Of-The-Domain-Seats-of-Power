package data.ui.ambitions;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ambition.AmbitionManager;
import data.scripts.ambition.AmbitionSpec;
import data.scripts.ambition.AmbitionSpecManager;
import data.scripts.ambition.BaseAmbition;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AmbitionPanel implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel, ambitionChoosePanel, ambitionInfoPanel, ambitionInfoComponent;
    AmbitionQuestPanel questPanel;
    UILinesRenderer renderer;
    boolean chooseAmbition = false;
    ButtonAPI chooseAmbitionButton;
    ButtonAPI confirmButton;
    // Replace individual fields with a single list
    private  List<AmbitionButton> buttons = new ArrayList<>();
    AmbitionButton curr;
    // Layout tuning knobs
    private float buttonWidth = 250f;
    private float buttonHeight = 450f;
    private float buttonSeparator = 10f;

    public AmbitionPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        renderer = new UILinesRenderer(-5f);
        renderer.setPanel(mainPanel);
        for (AmbitionSpec value : AmbitionSpecManager.specs.values()) {
            addAmbitionButton(new AmbitionButton(buttonWidth, buttonHeight,value){
                @Override
                public void performActionOnClick(boolean isRightClick) {
                    curr = this;
                    updateHeader();
                }
            });
        }

        createUI();
    }

    /** Public API to add/replace ambition buttons later */
    public void addAmbitionButton(AmbitionButton button) {
        buttons.add(button);
    }
    public void setAmbitionButtons(List<AmbitionButton> newButtons) {
        buttons.clear();
        if (newButtons != null) buttons.addAll(newButtons);
    }
    /** (Optional) control layout externally */
    public void setButtonLayout(float width, float height, float separator) {
        this.buttonWidth = width;
        this.buttonHeight = height;
        this.buttonSeparator = separator;
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        if (ambitionChoosePanel != null) {
            mainPanel.removeComponent(ambitionChoosePanel);
            chooseAmbitionButton = null;
        }

        if(AmbitionManager.getInstance().getCurrentAmbition()==null){

            ambitionChoosePanel = Global.getSettings().createCustom(
                    mainPanel.getPosition().getWidth(),
                    mainPanel.getPosition().getHeight(),
                    null
            );
            TooltipMakerAPI tooltip = ambitionChoosePanel.createUIElement(
                    ambitionChoosePanel.getPosition().getWidth(),
                    ambitionChoosePanel.getPosition().getHeight(),
                    false
            );

            float middleX = ambitionChoosePanel.getPosition().getWidth() / 2f;
            float middleY = ambitionChoosePanel.getPosition().getHeight() / 2f;

            if (!chooseAmbition) {
                tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);
                tooltip.setButtonFontOrbitron20Bold();
                LabelAPI label = tooltip.addPara(
                        "Ambition is ultimate goal our faction tries to achieve, it is what unites us under one banner, a purpose.",
                        3f
                );
                label.setAlignment(Alignment.MID);

                float callToActionHeight = 50f;
                float combinedHeight = 10f + label.computeTextHeight(label.getText()) + callToActionHeight;
                float startY = middleY - combinedHeight;
                label.getPosition().inTL(0, startY);

                chooseAmbitionButton = tooltip.addButton(
                        "Choose faction ambition",
                        null,
                        Misc.getBasePlayerColor(),
                        Misc.getDarkPlayerColor(),
                        Alignment.MID,
                        CutStyle.TL_BR,
                        500f,
                        callToActionHeight,
                        0f
                );
                chooseAmbitionButton.getPosition().inTL(
                        middleX - (chooseAmbitionButton.getPosition().getWidth() / 2f),
                        startY + label.computeTextHeight(label.getText()) + 10f
                );
            } else {
                tooltip.addSectionHeading("Choose your ambition", Alignment.MID, 0f);
                float y = tooltip.getHeightSoFar() + 5f;

                // ---- HORIZONTAL LAYOUT FROM LIST ----
                int n = Math.max(0, buttons.size());
                if (n > 0) {
                    float totalWidth = n * buttonWidth + (n - 1) * buttonSeparator;
                    float startX = middleX - (totalWidth / 2f);

                    float maxRowHeight = 0f;
                    for (int i = 0; i < n; i++) {
                        AmbitionButton btn = buttons.get(i);
                        // Ensure panel exists (if AmbitionButton lazily builds it)
                        CustomPanelAPI panel = btn.getComponentPanel();
                        float x = startX + i * (buttonWidth + buttonSeparator);
                        tooltip.addCustom(panel, 3f).getPosition().inTL(x, y);

                        // Track tallest for spacing below
                        float h = panel.getPosition().getHeight();
                        if (h <= 0f) h = buttonHeight; // fallback if position height isn't set yet
                        if (h > maxRowHeight) maxRowHeight = h;
                    }

                    y += maxRowHeight + 5f;
                }

                float rest = ambitionChoosePanel.getPosition().getHeight() - y;
                if (rest < 0f) rest = 0f;
                ambitionInfoPanel = Global.getSettings().createCustom(
                        ambitionChoosePanel.getPosition().getWidth(),
                        rest,
                        null
                );
                tooltip.addCustom(ambitionInfoPanel, 5f).getPosition().inTL(0f, y);
            }

            ambitionChoosePanel.addUIElement(tooltip).inTL(0, 0);
            mainPanel.addComponent(ambitionChoosePanel).inTL(0, 0);
        }
        else{
            questPanel = new AmbitionQuestPanel(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight());
            questPanel.createUI();
            mainPanel.addComponent(questPanel.getMainPanel()).inTL(0, 0);
        }

    }

    public void updateHeader() {
        if (ambitionInfoComponent != null) {
            ambitionInfoPanel.removeComponent(ambitionInfoComponent);
        }
        AmbitionSpec spec = curr.getAmbitionSpec();
        BaseAmbition amb;
        try {
             amb = spec.getPluginInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ambitionInfoComponent = Global.getSettings().createCustom(
                ambitionInfoPanel.getPosition().getWidth(),
                ambitionInfoPanel.getPosition().getHeight(),
                null
        );
        TooltipMakerAPI tooltip = ambitionInfoComponent.createUIElement(
                ambitionInfoPanel.getPosition().getWidth(),
                ambitionInfoPanel.getPosition().getHeight(),
                false
        );
        tooltip.setParaFont(Fonts.ORBITRON_20AA);
        amb.createIntroductionTooltip(tooltip);
        tooltip.setButtonFontOrbitron20Bold();
        confirmButton = tooltip.addButton(
                "Confirm",
                null,
                Misc.getBasePlayerColor(),
                Misc.getDarkPlayerColor(),
                Alignment.MID,
                CutStyle.TL_BR,
                300,
                35f,
                0f
        );
        confirmButton.getPosition().inTL(
                ambitionInfoComponent.getPosition().getWidth() - 310f,
                ambitionInfoComponent.getPosition().getHeight() - 40f
        );

        ambitionInfoComponent.addUIElement(tooltip).inTL(0, 0);
        ambitionInfoPanel.addComponent(ambitionInfoComponent).inTL(0, 0);
    }

    @Override public void clearUI() {}
    @Override public void positionChanged(PositionAPI position) {}
    @Override public void renderBelow(float alphaMult) {}
    @Override public void render(float alphaMult) { renderer.render(alphaMult); }

    @Override
    public void advance(float amount) {
        if (chooseAmbitionButton != null && chooseAmbitionButton.isChecked()) {
            chooseAmbitionButton.setChecked(false);
            chooseAmbition = !chooseAmbition;
            createUI();
        }
        if(confirmButton!=null&&confirmButton.isChecked()){
            confirmButton.setChecked(false);
            try {
                AmbitionManager.getInstance().setCurrentAmbition(curr.getAmbitionSpec().getPluginInstance());
                createUI();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override public void processInput(List<InputEventAPI> events) {}
    @Override public void buttonPressed(Object buttonId) {}
}
