package data.ui.factionpolicies;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.misc.ProductionUtil;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.managers.FactionPolicySpecManager;
import data.scripts.models.BaseFactionPolicy;
import data.ui.basecomps.ExtendUIPanelPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FactionCurrentPoliciesListPanel implements ExtendUIPanelPlugin {
    ArrayList<PolicyPanel> panels = new ArrayList<>();
    CustomPanelAPI mainPanel;
    CustomPanelAPI tooltipPanel;
    CustomPanelAPI headerPanel;
    TooltipMakerAPI tooltip;

    public ArrayList<PolicyPanel> getPanels() {
        return panels;
    }

    float offset = 0f;
    boolean changeRequired = false;

    public void setChangeRequired(boolean changeRequired) {
        this.changeRequired = changeRequired;
    }

    public FactionCurrentPoliciesListPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
    }

    public void createUI() {
        panels.clear();

        if (tooltipPanel != null) {
            mainPanel.removeComponent(tooltipPanel);
            mainPanel.removeComponent(headerPanel);
            tooltip = null;
        }
        AoTDFactionManager manager = AoTDFactionManager.getInstance();
        List<BaseFactionPolicy> chosenPolicies = new ArrayList<>(manager.getCurrentFactionPolicies());
        chosenPolicies.removeIf(x->!manager.getCopyOfPolicies().contains(x.getSpec().getId()));
        manager.getCopyOfPolicies().forEach(x->{
            if(chosenPolicies.stream().noneMatch(y->y.getSpec().getId().equals(x))){
                chosenPolicies.add(FactionPolicySpecManager.getSpec(x).getPlugin());
            }
        });
        int policiesChosen = chosenPolicies.size();
        float panelWidth = mainPanel.getPosition().getWidth();
        float panelHeight = mainPanel.getPosition().getHeight();

        headerPanel = Global.getSettings().createCustom(panelWidth,30,null);
        TooltipMakerAPI tooltipHeader = headerPanel.createUIElement(panelWidth,30,false);
        tooltipHeader.setParaFont(Fonts.ORBITRON_24AABOLD);
        tooltipHeader.addPara("Faction Policies ( %s / %s )",2f, Misc.getTooltipTitleAndLightHighlightColor(), Color.ORANGE,""+chosenPolicies.size(),""+manager.getAvailablePolicies().getModifiedInt()).setAlignment(Alignment.MID);

        tooltipPanel = Global.getSettings().createCustom(panelWidth, panelHeight-30, null);
        tooltip = tooltipPanel.createUIElement(panelWidth, panelHeight-30, true);

      ;


        int amountOfItems = Math.max(manager.getAvailablePolicies().getModifiedInt(), policiesChosen);
        int amountOfPlusItems = amountOfItems - policiesChosen;

// Fill the remaining items with `null` to represent empty placeholders
        for (int i = 0; i < amountOfPlusItems; i++) {
            chosenPolicies.add(null); // `null` represents an empty policy slot
        }

        float separator = 5f;
        int itemsPerRow = calculateMaxAmountOfItems(panelWidth, separator);

        int created = 0;
        while (created < amountOfItems) {
            int itemsThisRow = Math.min(itemsPerRow, amountOfItems - created);
            float currX = 0;

            float widthT = calculateTotalWidthOfItems(itemsThisRow, separator);
            CustomPanelAPI container = Global.getSettings().createCustom(panelWidth, PolicyPanel.HEIGHT, null);
            CustomPanelAPI row = Global.getSettings().createCustom(widthT, PolicyPanel.HEIGHT, null);

            for (int i = 0; i < itemsThisRow; i++) {
                BaseFactionPolicy policy = chosenPolicies.get(created); // Chosen or null
                PolicyPanel item = new PolicyPanel(policy==null, policy);
                if(policy!=null){
                    item.getButton().setClickable(policy.canBeRemoved());
                    this.panels.add(item);
                }
                row.addComponent(item.getMainPanel()).inTL(currX, 0);
                currX += PolicyPanel.WIDTH + separator;
                created++;
            }

            container.addComponent(row).inTL((panelWidth - widthT) / 2, 0f);
            tooltip.addCustom(container, 0f);
            tooltip.addSpacer(10f);
        }
        chosenPolicies.clear();

        tooltipPanel.addUIElement(tooltip).inTL(0, 0);
        headerPanel.addUIElement(tooltipHeader).inTL(0,0);
        if(tooltip.getExternalScroller()!=null){
            if(tooltip.getHeightSoFar()>=tooltipPanel.getPosition().getHeight()){
                if((tooltip.getHeightSoFar()-tooltipPanel.getPosition().getHeight())-offset<=0){
                    offset = tooltip.getHeightSoFar()-tooltipPanel.getPosition().getHeight();
                }
            }

            tooltip.getExternalScroller().setYOffset(offset);
        }


        tooltip.getExternalScroller().setYOffset(offset);
        mainPanel.addComponent(tooltipPanel).inTL(0, 30);
        mainPanel.addComponent(headerPanel).inTL(0,0);
    }


    public static int calculateMaxAmountOfItems(float width, float separator) {
        float objectWidth = PolicyPanel.WIDTH;

        // One item takes up its width, and every item after the first needs a separator
        int count = 0;
        float totalUsed = 0;

        while (true) {
            float nextItemWidth = objectWidth;
            if (count > 0) {
                nextItemWidth += separator; // separator only after the first item
            }

            if (totalUsed + nextItemWidth > width) {
                break;
            }

            totalUsed += nextItemWidth;
            count++;
        }

        return count;
    }
    public static float calculateTotalWidthOfItems(int itemCount, float separator) {
        if (itemCount <= 0) return 0;

        float objectWidth = PolicyPanel.WIDTH;

        // Total width is the sum of all item widths plus separators between them
        float totalWidth = (itemCount * objectWidth) + ((itemCount - 1) * separator);

        return totalWidth;
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
        if (tooltip != null) {
            if (tooltip.getExternalScroller() != null) {
                offset = tooltip.getExternalScroller().getYOffset();
            }
        }
        for (PolicyPanel panel : panels) {
            if(panel.getButton().isChecked()){
                panel.getButton().setChecked(false);
                if(panel.isDisplayDialog()){
                    BasePopUpDialog dialog = new FactionPolicyWarningDialog("Policy Warning!");
                    AshMisc.initPopUpDialog(dialog,700,300);
                }
                else{
                    AoTDFactionManager.getInstance().removePolicyFromCopy(panel.policy.getSpec().getId());
                    changeRequired = true;
                    break;
                }

            }
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }

    @Override
    public CustomPanelAPI getMainPanel() {
       return mainPanel;
    }
}


