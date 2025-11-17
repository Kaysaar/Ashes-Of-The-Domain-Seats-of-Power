package data.ui;

import ashlib.data.plugins.coreui.CommandTabMemoryManager;
import ashlib.data.plugins.coreui.CommandTabTracker;
import ashlib.data.plugins.coreui.CommandUIPlugin;
import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;

import data.scripts.managers.AoTDFactionManager;
import data.ui.ambitions.AmbitionPanel;
import data.ui.factionpolicies.FactionPolicyPanel;
import data.ui.overview.OverviewPanel;
import data.ui.timeline.FactionTimelinePanel;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactionPanel extends CommandUIPlugin {

    UILinesRenderer renderer;
    FactionPolicyPanel policyPanel;
    FactionTimelinePanel timelinePanel;
    OverviewPanel overviewPanel;
    AmbitionPanel ambitionPanel;

    public static boolean sentSignalForUpdate = false;

    public FactionPanel(float width, float height) {
        super(width, height);
    }

    @Override
    public boolean doesPlayCustomSoundWhenEnteredEntireTab() {
        return true;
    }

    @Override
    public String getTabStateId() {
        return "faction";
    }
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public void init(String panelToShowcase, Object data) {
        renderer = new UILinesRenderer(0f);
        this.panelForPlugins = mainPanel.createCustomPanel(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight() - 45, null);
        AoTDFactionManager.getInstance().advance(0f);
        if (!AshMisc.isStringValid(panelToShowcase)) {
            panelToShowcase = "timeline";
        }
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
        AoTDFactionManager.getInstance().applyChangesFromUI();
        policyPanel.availablePoliciesListPanel.getPanels().clear();
        policyPanel.currentPoliciesListPanel.getPanels().clear();
        mainPanel.removeComponent(panelForPlugins);
        if (clearMusic) {
            pauseSound();
        }

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
            overviewPanel.createUI();
            timelinePanel.reset();
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
        for (InputEventAPI event : events) {
            if(CommandTabTracker.lockedState){
                if(event.isConsumed())continue;
                if(event.getEventValue()==Keyboard.KEY_ESCAPE&&!event.isMouseEvent()){
                    event.consume();
                }
            }

        }
    }

    @Override
    public void buttonPressed(Object buttonId) {

    }

    @Override
    public void playSound(Object data) {
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
        CustomPanelAPI panelHelper = this.buttonPanel.createCustomPanel(490, 0.5f, renderer);
//        renderer.setPanel(panelHelper);
        TooltipMakerAPI buttonTooltip = buttonPanel.createUIElement(mainPanel.getPosition().getWidth(), 20, false);
        Color base, bg;
        base = Global.getSector().getPlayerFaction().getBaseUIColor();
        bg = Global.getSector().getPlayerFaction().getDarkUIColor();
        customProd = buttonTooltip.addButton("Policies", null, base, bg, Alignment.MID, CutStyle.TOP, 140, 20, 0f);
        research = buttonTooltip.addButton("Timeline", null, base, bg, Alignment.MID, CutStyle.TOP, 140, 20, 0f);
        ;
        sp = buttonTooltip.addButton("Overview", null, base, bg, Alignment.MID, CutStyle.TOP, 140, 20, 0f);
        ;
        ;
        customProd.setShortcut(Keyboard.KEY_R, false);
        research.setShortcut(Keyboard.KEY_T, false);
        sp.setShortcut(Keyboard.KEY_S, false);
        customProd.getPosition().inTL(0, 0);
        research.getPosition().inTL(141, 0);
        sp.getPosition().inTL(282, 0);
        buttonPanel.addUIElement(buttonTooltip).inTL(0, 0);
        buttonPanel.addComponent(panelHelper).inTL(0, 20);
        mainPanel.addComponent(buttonPanel).inTL(0, 10);
        insertPolicyPanel(customProd);
        insertTimeLinePanel(research);
        insertOverviewPanel(sp);
    }

    private void insertPolicyPanel(ButtonAPI tiedButton) {
        if (policyPanel == null) {
            policyPanel = new FactionPolicyPanel(panelForPlugins.getPosition().getWidth(), panelForPlugins.getPosition().getHeight());
        }

        panelMap.put(tiedButton, policyPanel.getMainPanel());
    }



    private void insertTimeLinePanel(ButtonAPI tiedButton) {
        if (timelinePanel == null) {
            timelinePanel = new FactionTimelinePanel(panelForPlugins.getPosition().getWidth(), panelForPlugins.getPosition().getHeight());
        }

        panelMap.put(tiedButton, timelinePanel.getMainPanel());
    }
    private void insertOverviewPanel(ButtonAPI tiedButton) {
        if (overviewPanel == null) {
            overviewPanel = new OverviewPanel(panelForPlugins.getPosition().getWidth(), panelForPlugins.getPosition().getHeight());
        }

        panelMap.put(tiedButton, overviewPanel.getMainPanel());
    }
    public void playSound(ButtonAPI button) {

    }
}
