package data.ui;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import data.misc.UIDataSop;
import data.scripts.CoreUITrackerScript;
import data.scripts.managers.AoTDFactionManager;
import data.ui.factionpolicies.FactionPolicyPanel;
import data.ui.overview.OverviewPanel;
import data.ui.timeline.FactionTimelinePanel;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactionPanel implements CustomUIPanelPlugin,SoundUIManager {
    CustomPanelAPI mainPanel;
    CustomPanelAPI panelForPlugins = null;
    CustomPanelAPI buttonPanel = null;
    ButtonAPI currentlyChosen;
    SoundUIManager manager;
    UILinesRenderer renderer;
    HashMap<ButtonAPI, CustomPanelAPI> panelMap = new HashMap<>();
    boolean pausedMusic = true;
    FactionPolicyPanel policyPanel;
    FactionTimelinePanel timelinePanel;
    OverviewPanel overviewPanel;
    public static boolean sentSignalForUpdate = false;
    Object outpostPanel;
    public HashMap<ButtonAPI, CustomPanelAPI> getPanelMap() {
        return panelMap;
    }

    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public void init(CustomPanelAPI mainPanel, String panelToShowcase, Object data) {
        this.mainPanel = mainPanel;
        renderer = new UILinesRenderer(0f);
        this.panelForPlugins = mainPanel.createCustomPanel(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight() - 45, null);
        AoTDFactionManager.getInstance().advance(0f);
        if (!AshMisc.isStringValid(panelToShowcase)) {
            panelToShowcase = "timeline";
        }
        this.outpostPanel = data;
        UIDataSop.WIDTH = panelForPlugins.getPosition().getWidth();
        UIDataSop.HEIGHT = panelForPlugins.getPosition().getHeight();
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
                    CoreUITrackerScript.setMemFlagForFactionTab(entry.getKey().getText().toLowerCase());
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
