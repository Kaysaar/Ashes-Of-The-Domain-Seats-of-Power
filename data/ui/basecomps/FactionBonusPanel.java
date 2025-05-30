package data.ui.basecomps;

import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.managers.FactionPolicySpecManager;
import data.scripts.models.BaseFactionPolicy;
import data.ui.factionpolicies.DetailedFactionPolicyTooltip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FactionBonusPanel implements ExtendUIPanelPlugin {

    public CustomPanelAPI mainPanel;
    public CustomPanelAPI panelForUI;
    UILinesRenderer renderer = new UILinesRenderer(0f);
    LabelAPI warningLabel;
    public IntervalUtil util = new IntervalUtil(2f,2f);
    public boolean renderBorders = true;

    public FactionBonusPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
        renderer = new UILinesRenderer(0f);
        renderer.setPanel(mainPanel);

    }
    public FactionBonusPanel(float width, float height,boolean renderBorders) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
        this.renderBorders = renderBorders;
        renderer = new UILinesRenderer(0f);
        renderer.setPanel(mainPanel);

    }
    public void createUI() {
        if (panelForUI != null) {
            mainPanel.removeComponent(panelForUI);
        }

        panelForUI = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), null);
        TooltipMakerAPI tooltip = panelForUI.createUIElement(panelForUI.getPosition().getWidth(), panelForUI.getPosition().getHeight()-25, true);
        TooltipMakerAPI tooltipForTitle =panelForUI.createUIElement(panelForUI.getPosition().getWidth(),20, false);
        float width = panelForUI.getPosition().getWidth();
        tooltipForTitle.setTitleFont(Fonts.ORBITRON_24AABOLD);
        LabelAPI label = tooltipForTitle.addTitle("Current Effects");
        label.setAlignment(Alignment.MID);
        HashSet<String> copy = AoTDFactionManager.getInstance().getCopyOfPolicies();
        float opad = 0f;
        ArrayList<BaseFactionPolicy>currentPolicies = AoTDFactionManager.getInstance().getCurrentFactionPolicies();
        if (copy.stream().anyMatch(x -> !AoTDFactionManager.getInstance().doesHavePolicyEnabled(x))||currentPolicies.stream().anyMatch(x->!copy.contains(x.getSpec().getId()))) {
            if(warningLabel==null){
                warningLabel = tooltip.addPara("Warning: These changes apply upon leaving the Command Tab", Misc.getNegativeHighlightColor(), 5f);
                warningLabel.setAlignment(Alignment.MID);
                warningLabel.flash(1f,1f);
                util.setInterval(2f,2f);
            }
            else{
                tooltip.addCustom((UIComponentAPI) warningLabel,5f);
            }


            copy.stream().filter(x -> !AoTDFactionManager.getInstance().doesHavePolicyEnabled(x)).forEach(x -> tooltip.addPara(BaseIntelPlugin.BULLET + "%s "+FactionPolicySpecManager.getSpec(x).getName(),3f,Misc.getTooltipTitleAndLightHighlightColor(),Misc.getPositiveHighlightColor(),"Add"));
            currentPolicies.stream().filter(x->!copy.contains(x.getSpec().getId())).forEach(x -> tooltip.addPara(BaseIntelPlugin.BULLET + "%s "+x.getSpec().getName(),3f,Misc.getTooltipTitleAndLightHighlightColor(),Misc.getNegativeHighlightColor(),"Remove"));
        }
        else{
            warningLabel = null;
        }
        tooltip.addPara("Current policies still in effect",Misc.getButtonTextColor(),5f).setAlignment(Alignment.MID);
        tooltip.addSpacer(2f);
        currentPolicies.forEach(x -> {
            LabelAPI labels = tooltip.addPara(BaseIntelPlugin.BULLET + x.getSpec().getName(),Misc.getPositiveHighlightColor(),3f);
            labels.getPosition().setSize(labels.computeTextWidth(labels.getText()),labels.getPosition().getHeight());
            labels.setHighlightOnMouseover(true);
            tooltip.addTooltipTo(new DetailedFactionPolicyTooltip(x), (UIComponentAPI) labels, TooltipMakerAPI.TooltipLocation.BELOW,false);
        });
        tooltip.addSpacer(10f);



        panelForUI.addUIElement(tooltipForTitle).inTL(0, 2f);
        panelForUI.addUIElement(tooltip).inTL(0, 22f);
        mainPanel.addComponent(panelForUI);

    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {
        if(renderBorders){
            renderer.render(alphaMult);

        }
    }

    @Override
    public void advance(float amount) {
        util.advance(amount);
        if(util.intervalElapsed()){
            if(warningLabel!=null){
                warningLabel.flash(1f,1f);

            }
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
