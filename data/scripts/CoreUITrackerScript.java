package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.command.CustomProductionPanel;
import data.misc.ProductionUtil;
import data.misc.ReflectionUtilis;
import data.misc.UIDataSop;
import data.scripts.managers.AoTDFactionManager;
import data.ui.FactionPanel;
import data.ui.PatrolTabPanel;
import de.unkrig.commons.nullanalysis.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;

import static data.misc.AoTDSopMisc.tryToGetButtonProd;

public class CoreUITrackerScript implements EveryFrameScript {
    boolean inserted = false;
    boolean insertedOnce = false;
    boolean removed = false;
    boolean pausedMusic = true;
    FactionPanel coreUiTech = null;
    @Override
    public boolean isDone() {
        return false;
    }
    PatrolTabPanel patrolPanel;
    HashMap<ButtonAPI, Object> panelMap = null;
    ButtonAPI currentTab = null;
    String nameOfCurrentTab;
    boolean tunedMusicOnce= false;
    public static boolean sendSignalToOpenCore = false;
    public static final String memFlag = "$aotd_outpost_state";
    public static final String memFlag2 = "$aotd_faction_tab_state";
    public static final String memFlag3 = "$aotd_patrol_tab_state";
    public static void setMemFlag(String value) {
        Global.getSector().getMemory().set(memFlag, value);
    }
    public static void setMemFlagForFactionTab(String value) {
        Global.getSector().getMemory().set(memFlag2, value);
    }
    public static void setMemFlagForPatrolTab(String value) {
        Global.getSector().getMemory().set(memFlag3, value);
    }
    public static String getMemFlagForTechTab(){
        String s = null;
        try {
            s = Global.getSector().getMemory().getString(memFlag2);

        } catch (Exception e) {

        }
        return s;
    }
    public static String getMemFlagForPatrolTab(){
        String s = null;
        try {
            s = Global.getSector().getMemory().getString(memFlag3);

        } catch (Exception e) {

        }
        return s;
    }
    public static String getMemFlag() {
        String s = null;
        try {
            s = Global.getSector().getMemory().getString(memFlag);

        } catch (Exception e) {

        }
        return s;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        if ((Global.getSector().getCampaignUI().getCurrentCoreTab() == null || Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.OUTPOSTS)) {
            inserted = false;
            panelMap = null;
            currentTab = null;
            if(coreUiTech!=null){
                coreUiTech.clearUI(tunedMusicOnce);
                coreUiTech = null;

            }
            if(patrolPanel!=null){
                patrolPanel.clearUI(tunedMusicOnce);
                patrolPanel = null;

            }
            tunedMusicOnce = false;
            removed = false;
            insertedOnce = false;
            return;
        }
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != null) {
            sendSignalToOpenCore = false;
        }
        UIPanelAPI mainParent = ProductionUtil.getCurrentTab();
        if (mainParent == null) return;
        ButtonAPI button = tryToGetButtonProd("income");
        ButtonAPI toRemove2 = tryToGetButtonProd("orders");


        if (button == null){
            return;
        }
        if(toRemove2!=null){


            insertButton(tryToGetButtonProd("colonies"), mainParent, "Faction", new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return 500;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addSectionHeading("Ashes of the Domain : Seats Of Power",Alignment.MID,0f);
                    tooltip.addPara("In this tab,your empire timeline and can govern your faction by changing policies.",5f);
                }
            }, tryToGetButtonProd("colonies"), toRemove2.getPosition().getWidth(), Keyboard.KEY_2, false);
            float x =tryToGetButtonProd("orders").getPosition().getX()-10;
            mainParent.removeComponent(toRemove2);
            tryToGetButtonProd("income").getPosition().rightOfMid(tryToGetButtonProd("faction"),1f);
            tryToGetButtonProd("faction").setEnabled(!AoTDFactionManager.getMarketsUnderPlayer().isEmpty());
            mainParent.removeComponent(tryToGetButtonProd("doctrine & blueprints"));
            insertButton(button, mainParent, "Military", new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return 500f;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addSectionHeading("Ashes of the Domain : Seats of Power", Alignment.MID,0f);
                    tooltip.addPara("In this tabs you will be able to design patrol fleets and control your forces, to guard your assets.",5f);
                }
            }, tryToGetButtonProd("colonies"), 150, Keyboard.KEY_4, false);
            tryToGetButtonProd("custom production").getPosition().rightOfMid(tryToGetButtonProd("military"),1f);

        }

        if (shouldHandleReset()) {
            removed = false;
            if (panelMap != null) {
                panelMap.clear();
            }

            insertedOnce = false;
            currentTab = null;
            panelMap = null;
        }

        if (panelMap == null) {
            panelMap = new HashMap<>();
            panelMap.putAll(getPanelMap(mainParent));
        }
        if (panelMap == null) {
            return;
        }


        float y = button.getPosition().getY();
        float x = button.getPosition().getX();
        if (y < 0) {
            y *= -1;
        }

        if (!removed) {
            removed = true;
            boolean found = false;
            if (!insertedOnce) {
                UIComponentAPI componentToReplace = (UIComponentAPI) panelMap.get(button);
                UIDataSop.WIDTH = Global.getSettings().getScreenWidth() - tryToGetButtonProd("colonies").getPosition().getX();
                UIDataSop.HEIGHT = componentToReplace.getPosition().getHeight();

            }
            removePanels((ArrayList<UIComponentAPI>) ReflectionUtilis.getChildrenCopy(mainParent), mainParent, null);
            insertNewPanel(tryToGetButtonProd(getStringForCoreTabFaction()));
            insertNewPatrolTab(tryToGetButtonProd(getStringForPatrolFleet()));


        }
        if (currentTab == null && getMemFlag() == null) {
            for (ButtonAPI buttonAPI : panelMap.keySet()) {
                if (buttonAPI.isHighlighted()) {
                    currentTab = buttonAPI;
                    setMemFlag(currentTab.getText().toLowerCase());
                    break;
                }
            }
        }
        if (currentTab == null && getMemFlag() != null) {
            for (ButtonAPI buttonAPI : panelMap.keySet()) {
                if (buttonAPI.getText().toLowerCase().contains(getMemFlag())) {
                    currentTab = buttonAPI;
                }
            }
        }
        if(currentTab.getText().toLowerCase().contains(getStringForCoreTabFaction())){
            if(!tunedMusicOnce){
                tunedMusicOnce = true;
                coreUiTech.playSound();

            }
        }
        else{
            tunedMusicOnce = false;
        }
        if (!hasComponentPresent((UIComponentAPI) panelMap.get(currentTab))) {
            removePanels((ArrayList<UIComponentAPI>) ReflectionUtilis.getChildrenCopy(mainParent), mainParent, null);
            mainParent.addComponent((UIComponentAPI) panelMap.get(currentTab));
            setMemFlag(currentTab.getText().toLowerCase());


            ;
        }
        handleButtonsHighlight();
        handleButtons();


    }

    public static @NotNull String getStringForCoreTabFaction() {
        return "faction";
    }
    public static @NotNull String getStringForPatrolFleet() {
        return "military";
    }
    private static void removePanels(ArrayList<UIComponentAPI> componentAPIS, UIPanelAPI mainParent, UIComponentAPI panelToIgnore) {
        for (UIComponentAPI componentAPI : componentAPIS) {
            if (componentAPI instanceof ButtonAPI) continue;

            if (componentAPI.equals(panelToIgnore)) continue;
            mainParent.removeComponent(componentAPI);
        }
    }

    private boolean hasComponentPresent(UIComponentAPI component) {
        for (UIComponentAPI buttonAPI : ReflectionUtilis.getChildrenCopy(ProductionUtil.getCurrentTab())) {
            if (component.equals(buttonAPI)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldHandleReset() {
        for (UIComponentAPI buttonAPI : ReflectionUtilis.getChildrenCopy(ProductionUtil.getCurrentTab())) {
            if (buttonAPI instanceof CustomProductionPanel) {
                return true;
            }
        }
        return false;
    }

    private void handleButtonsHighlight() {
        for (ButtonAPI buttonAPI : panelMap.keySet()) {
            if (!buttonAPI.equals(currentTab)) {
                buttonAPI.unhighlight();
            } else {
                buttonAPI.highlight();
            }
        }

    }

    private void handleButtons() {
        for (ButtonAPI buttonAPI : panelMap.keySet()) {
            if (buttonAPI.isChecked()) {
                buttonAPI.setChecked(false);
                if (!currentTab.equals(buttonAPI)) {
                    ProductionUtil.getCurrentTab().removeComponent((UIComponentAPI) panelMap.get(currentTab));
                    if(buttonAPI.getText().toLowerCase().contains(getStringForCoreTabFaction())){
//                        if(coreUiTech.getCurrentlyChosen()!=null){
//                            coreUiTech.playSound(coreUiTech.getCurrentlyChosen());
//                        }
                    } else if (currentTab.getText().toLowerCase().contains(getStringForCoreTabFaction())) {
//                        coreUiTech.pauseSound();
                    }
                    currentTab = buttonAPI;
                    setMemFlag(currentTab.getText().toLowerCase());
                }


            }
        }
    }

    private HashMap<ButtonAPI, Object> getPanelMap(UIComponentAPI mainParent) {
        HashMap<ButtonAPI, Object> map = (HashMap<ButtonAPI, Object>) ReflectionUtilis.invokeMethod("getButtonToTab", mainParent);
        return map;
    }

    private void insertButton(ButtonAPI button, UIPanelAPI mainParent, String name, TooltipMakerAPI.TooltipCreator creator, ButtonAPI button2, float size, int keyBind, boolean dissabled) {
        ButtonAPI newButton = createPanelButton(name, size, button.getPosition().getHeight(), keyBind, dissabled, creator).two;

        mainParent.addComponent(newButton).inTL(button.getPosition().getX() + button.getPosition().getWidth() - button2.getPosition().getX() + 1, 0);
        mainParent.bringComponentToTop(newButton);
    }

    private Pair<CustomPanelAPI, ButtonAPI> createPanelButton(String buttonName, float width, float height, int bindingValue, boolean dissabled, TooltipMakerAPI.TooltipCreator onHoverTooltip) {
        CustomPanelAPI panel = Global.getSettings().createCustom(width, height, null);
        TooltipMakerAPI tooltipMakerAPI = panel.createUIElement(width, height, false);
        ButtonAPI button = tooltipMakerAPI.addButton(buttonName, null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TOP, width, height, 0f);
        button.setShortcut(bindingValue, false);
        button.setEnabled(!dissabled);
        if (onHoverTooltip != null) {
            tooltipMakerAPI.addTooltipToPrevious(onHoverTooltip, TooltipMakerAPI.TooltipLocation.BELOW);

        }
        panel.addUIElement(tooltipMakerAPI).inTL(0, 0);
        return new Pair(panel, button);
    }
    private void insertNewPanel(ButtonAPI tiedButton) {
        if (coreUiTech == null) {
            coreUiTech = new FactionPanel();
            coreUiTech.init(Global.getSettings().createCustom(UIDataSop.WIDTH, UIDataSop.HEIGHT, coreUiTech), getMemFlagForTechTab(), null);
        }

        panelMap.put(tiedButton, coreUiTech.getMainPanel());
    }
    private void insertNewPatrolTab(ButtonAPI tiedButton) {
        if (patrolPanel == null) {
            patrolPanel = new PatrolTabPanel();
            float width = UIDataSop.WIDTH;
            float height = UIDataSop.HEIGHT;
            patrolPanel.init(Global.getSettings().createCustom(width, height, patrolPanel), getMemFlagForPatrolTab(), panelMap.get(tryToGetButtonProd("colonies")));
        }

        panelMap.put(tiedButton, patrolPanel.getMainPanel());
    }
}
