package data.ui.timeline;

import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.misc.ReflectionUtilis;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionTimelineEvent;
import data.scripts.models.CycleTimelineEvents;
import data.ui.basecomps.ExtendUIPanelPlugin;
import data.ui.basecomps.RightMouseInterceptor;
import data.ui.basecomps.RightMouseTooltipMoverV2;

import java.util.ArrayList;
import java.util.List;

public class FactionTimelineViewerComponent implements ExtendUIPanelPlugin {
    public CustomPanelAPI mainPanel;
    CustomPanelAPI dummy;
    UILinesRenderer renderer;
    RightMouseTooltipMoverV2 mover;
    TooltipMakerAPI tooltip;
    CustomPanelAPI content;
    RightMouseInterceptor interceptor = new RightMouseInterceptor();
    ButtonAPI left, right;
    float currX = 0;

    public FactionTimelineViewerComponent(float width, float height) {
        renderer = new UILinesRenderer(0f);
        mainPanel = Global.getSettings().createCustom(width, height, this);
        interceptor = new RightMouseInterceptor();
        renderer.setPanel(mainPanel);
        init();
    }

    public void init() {
        dummy = Global.getSettings().createCustom(mainPanel.getPosition().getWidth() , mainPanel.getPosition().getHeight(),null);
        CustomPanelAPI blocker = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), interceptor);
        tooltip = mainPanel.createUIElement(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), true);
        tooltip.addSpacer(mainPanel.getPosition().getHeight() * 2);
        createUI();
        dummy.getPosition().setSize(Math.max(content.getPosition().getWidth(),mainPanel.getPosition().getWidth()), dummy.getPosition().getHeight());
        tooltip.addCustom(dummy, 0f).getPosition().inTL(0, 0);

        mainPanel.addUIElement(tooltip).inTL(0, 0);
        interceptor.setPanelPos(blocker);
        mainPanel.addComponent(blocker).inTL(0, 0);


        ;
        mover = new RightMouseTooltipMoverV2();
        mover.init(dummy, mainPanel);
        left = tooltip.addButton("<<", "<<", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 20, 40, 0f);
        right = tooltip.addButton(">>", ">>", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 20, 40, 0f);
        left.getPosition().inTL(mainPanel.getPosition().getCenterX()-left.getPosition().getWidth()-10, mainPanel.getPosition().getHeight()-left.getPosition().getHeight()-5);
        right.getPosition().inTL(mainPanel.getPosition().getCenterX()+right.getPosition().getWidth()+10, mainPanel.getPosition().getHeight()-right.getPosition().getHeight()-5);
        tooltip.setHeightSoFar(0f);
        mover.setBorders(-dummy.getPosition().getWidth() + mainPanel.getPosition().getWidth(), 0);
        mover.setCurrOffset(0f);
        if (tooltip.getExternalScroller() != null) {
            ReflectionUtilis.invokeMethodWithAutoProjection("setMaxShadowHeight", tooltip.getExternalScroller(), 10f);
            ReflectionUtilis.invokeMethodWithAutoProjection("setShowScrollbars", tooltip.getExternalScroller(), false);
        }
    }

    public void createUI() {
        if (content != null) {
            dummy.removeComponent(content);
        }
        float calWidth = 25;
        AoTDFactionManager.getInstance().getCycles().forEach(x->x.getEventsDuringCycle().forEach(BaseFactionTimelineEvent::updateDataUponEntryOfUI));
        ArrayList<FactionCycleShowcase>generatedShowcases = new ArrayList<>();
        for (CycleTimelineEvents cycle : AoTDFactionManager.getInstance().getCycles()) {
            FactionCycleShowcase showcase = new FactionCycleShowcase(cycle,dummy.getPosition().getHeight());
            generatedShowcases.add(showcase);
            calWidth+=showcase.getGeneratedWidth()+FactionCycleShowcase.spacerBetweenEvents;
        }
        content = Global.getSettings().createCustom(calWidth, dummy.getPosition().getHeight(), null);
        TooltipMakerAPI tooltip = content.createUIElement(calWidth, content.getPosition().getHeight(), false);
        CustomPanelAPI dash = new DashLinePanel(Math.max(mainPanel.getPosition().getWidth(),content.getPosition().getWidth()),content.getPosition().getHeight()).getMainPanel();
        tooltip.addCustom(dash,0f).getPosition().inTL(0,0);;
        float currOffset =25;
        for (FactionCycleShowcase showcase : generatedShowcases) {
            tooltip.addCustom(showcase.getMainPanel(),0f).getPosition().inTL(currOffset,0);
            currOffset+=showcase.generatedWidth+FactionCycleShowcase.spacerBetweenEvents;
        }
        generatedShowcases.clear();
        content.addUIElement(tooltip).inTL(0, 0);
        dummy.addComponent(content).inTL(0, 0);
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
        renderer.render(alphaMult);
    }

    @Override
    public void advance(float amount) {
        mover.advance(amount);

        if(left!=null){
            if(mover.isMoving()&&left.isEnabled()){
                left.setEnabled(false);
            } else if (!mover.isMoving()) {
                left.setEnabled(true);
            }
            if(!mover.isMoving()){
                if(left.isChecked()){
                    left.setChecked(false);
                    mover.moveBy(mainPanel.getPosition().getWidth());
                }
            }



        }
        if(right!=null){
            if(mover.isMoving()&&right.isEnabled()){
                right.setEnabled(false);
            } else if (!mover.isMoving()) {
                right.setEnabled(true);
            }
            if(!mover.isMoving()){
                if(right.isChecked()){
                    right.setChecked(false);
                    mover.moveBy(-mainPanel.getPosition().getWidth());
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
}
