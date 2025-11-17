package data.ui;

import ashlib.data.plugins.coreui.CommandTabMemoryManager;
import ashlib.data.plugins.coreui.CommandUIPlugin;
import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.templates.TemplatePanel;
import data.ui.patrolfleet.templates.shiplist.components.ShipPanelData;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatrolTabPanel  extends CommandUIPlugin {
    UILinesRenderer renderer;
    TemplatePanel panelForTemplates;
    OverviewPatrolPanel patrolPanel;
    boolean pausedMusic = true;
    public static boolean sentSignalForUpdate = false;
    Object outpostPanel;

    public PatrolTabPanel(float width, float height) {
        super(width, height);
    }

    public HashMap<ButtonAPI, CustomPanelAPI> getPanelMap() {
        return panelMap;
    }

    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {

    }

    @Override
    public void clearUI() {

    }

    public void init(String panelToShowcase, Object data) {
        renderer = new UILinesRenderer(0f);
        this.panelForPlugins = mainPanel.createCustomPanel(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight() - 45, null);
        if (!AshMisc.isStringValid(panelToShowcase)) {
            panelToShowcase = "overview";
        }
        this.outpostPanel = data;
        createButtonsAndMainPanels();
        for (Map.Entry<ButtonAPI, CustomPanelAPI> buttons : panelMap.entrySet()) {
            if (buttons.getKey().getText().toLowerCase().contains(panelToShowcase)) {
                currentlyChosen = buttons.getKey();
                break;
            }
        }


        if (currentlyChosen != null) {
            panelForPlugins.addComponent(panelMap.get(currentlyChosen)).inTL(0, 0);
        }

        this.mainPanel.addComponent(panelForPlugins).inTL(0, 35);
        renderer.setPanel(panelForPlugins);
    }

    public void clearUI(boolean clearMusic) {
        ;
        panelMap.clear();

        mainPanel.removeComponent(panelForPlugins);
        if (clearMusic) {
            pauseSound();
        }

    }

    @Override
    public String getTabStateId() {
        return "military";
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
        if(sentSignalForUpdate){
            sentSignalForUpdate = false;
        }
        for (Map.Entry<ButtonAPI, CustomPanelAPI> entry : panelMap.entrySet()) {
            entry.getKey().unhighlight();
            if (entry.getKey().isChecked()) {
                entry.getKey().setChecked(false);
                if (!entry.getKey().equals(currentlyChosen)) {
                    resetCurrentPlugin(entry.getKey());
                    CommandTabMemoryManager.getInstance().getTabStates().put(getTabStateId(),entry.getKey().getText().toLowerCase());
                }


                break;
            }
        }
        if (currentlyChosen != null) {
            currentlyChosen.highlight();
        }
    }

    public void resetCurrentPlugin(ButtonAPI newButton) {
        if (currentlyChosen != null) {
            this.panelForPlugins.removeComponent(panelMap.get(currentlyChosen));
        }
        currentlyChosen = newButton;
        this.panelForPlugins.addComponent(panelMap.get(currentlyChosen)).inTL(0, 0);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }

    @Override
    public void playSound() {
        Global.getSoundPlayer().playCustomMusic(1, 1, "aotd_faction", true);

    }

    public void pauseSound() {
        Global.getSoundPlayer().pauseCustomMusic();
        Global.getSoundPlayer().restartCurrentMusic();
        pausedMusic = true;
    }

    public void createButtonsAndMainPanels() {
        ButtonAPI research, megastructures, customProd, sp;
        this.buttonPanel = this.mainPanel.createCustomPanel(mainPanel.getPosition().getWidth(), 25, null);
        UILinesRenderer renderer = new UILinesRenderer(0f);
//        renderer.setPanel(panelHelper);
        TooltipMakerAPI buttonTooltip = buttonPanel.createUIElement(mainPanel.getPosition().getWidth(), 20, false);
        Color base, bg;
        base = Global.getSector().getPlayerFaction().getBaseUIColor();
        bg = Global.getSector().getPlayerFaction().getDarkUIColor();
        customProd = buttonTooltip.addButton("Overview", null, base, bg, Alignment.MID, CutStyle.TOP, 140, 20, 0f);
        research = buttonTooltip.addButton("Templates", null, base, bg, Alignment.MID, CutStyle.TOP, 140, 20, 0f);
        ;
        sp = buttonTooltip.addButton("Armory", null, base, bg, Alignment.MID, CutStyle.TOP, 140, 20, 0f);
        ;
        sp.setEnabled(false);
        customProd.setShortcut(Keyboard.KEY_R, false);
        research.setShortcut(Keyboard.KEY_T, false);
        sp.setShortcut(Keyboard.KEY_A, false);
        customProd.getPosition().inTL(0, 0);
        research.getPosition().inTL(141, 0);
        sp.getPosition().inTL(282, 0);
        ShipPanelData.updateList();
        ShipPanelData.populateShipInfo();
        ShipPanelData.populateShipSizeInfo();
        ShipPanelData.populateShipTypeInfo();
        buttonPanel.addUIElement(buttonTooltip).inTL(0, 0);
        mainPanel.addComponent(buttonPanel).inTL(0, 10);
        insertPatrolTemplatePanel(research);
        insertOverviewPatrol(customProd);
    }

    private void insertPatrolTemplatePanel(ButtonAPI tiedButton) {
        if (panelForTemplates == null) {
            panelForTemplates = new TemplatePanel(panelForPlugins.getPosition().getWidth()-10, panelForPlugins.getPosition().getHeight());
        }

        panelMap.put(tiedButton, panelForTemplates.getMainPanel());
    }
    private void insertOverviewPatrol(ButtonAPI tiedButton) {
        if (patrolPanel == null) {
            patrolPanel = new OverviewPatrolPanel(panelForPlugins.getPosition().getWidth()-10, panelForPlugins.getPosition().getHeight());
        }

        panelMap.put(tiedButton, patrolPanel.getMainPanel());
    }

    public void playSound(ButtonAPI button) {

    }
}
