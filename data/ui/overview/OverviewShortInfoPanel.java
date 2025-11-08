package data.ui.overview;

import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.industry.NovaExploraria;
import data.scripts.ambition.AmbitionManager;
import data.scripts.managers.AoTDFactionManager;
import data.ui.basecomps.*;

import java.util.ArrayList;
import java.util.List;

public class OverviewShortInfoPanel implements ExtendUIPanelPlugin {
    CustomPanelAPI mainPanel, contentPanel;
    UILinesRenderer renderer;
    CustomPanelAPI panelOfGatheringPoint;
    ArrayList<ButtonAPI> buttons = new ArrayList<>();
    public boolean recreateUI = false;
    public String current = "pop";

    public OverviewShortInfoPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        renderer = new UILinesRenderer(0f);
        renderer.setPanel(mainPanel);
        createUI();
    }

    public CustomPanelAPI createGatheringPointBar(float width) {
        UILinesRenderer renderer = new UILinesRenderer(0f);
        panelOfGatheringPoint = Global.getSettings().createCustom(width, 100, renderer);
        TooltipMakerAPI tooltip = panelOfGatheringPoint.createUIElement(width, 100, false);
        tooltip.addSectionHeading("Faction Capital", Alignment.MID, 0f);
        if (AoTDFactionManager.getInstance().doesControlCapital()) {
                Pair<CustomPanelAPI, ButtonAPI> pair = AoTDGatehringPointPlugin.getMarketEntitySpriteButton(width - 15f, 75, 75, AoTDFactionManager.getInstance().getCapitalMarket());
                tooltip.addCustom(pair.one, 5f);
                pair.two.setClickable(false);

        } else if (!AoTDFactionManager.getInstance().didDeclaredCapital()) {
            tooltip.setParaFont(Fonts.ORBITRON_12);
            tooltip.addPara("No capital selected. Choose one via the colony UI by clicking Population & Infrastructure (Min size 5 colony!).", Misc.getTooltipTitleAndLightHighlightColor(), 5f);
            tooltip.addPara("Note: The capital is permanent and cannot be changed later. Losing capital will give heavy de-buffs!", Misc.getTooltipTitleAndLightHighlightColor(), 5f);

        }
        else{
            tooltip.setParaFont(Fonts.ORBITRON_12);
            tooltip.addPara("Capital is lost! We must reclaim it immediately!",Misc.getNegativeHighlightColor(),5f).setAlignment(Alignment.MID);

        }


        panelOfGatheringPoint.addUIElement(tooltip).inTL(0, 0);
        return panelOfGatheringPoint;
    }

    public void createUI() {
        if (contentPanel != null) {
            mainPanel.removeComponent(contentPanel);
        }
        contentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), null);
        TooltipMakerAPI tooltip = contentPanel.createUIElement(contentPanel.getPosition().getWidth(), contentPanel.getPosition().getHeight(), false);
        tooltip.addCustom(new FactionFlagButtonComponent(200, 125, true).getPanelOfButton(), 5f).getPosition().inTL(100, 28);
        tooltip.addCustom(new FactionXPPanel(contentPanel.getPosition().getWidth(), 125, false).getMainPanel(), 5f).getPosition().inTL(0, 155);

        TooltipMakerAPI tooltipSub = tooltip.beginSubTooltip(contentPanel.getPosition().getWidth());

        tooltipSub.addSectionHeading("Additional info", Alignment.MID, 5f);

        buttons.add(tooltipSub.addButton("Population", "pop", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, contentPanel.getPosition().getWidth() - 10, 30, 10f));
//        buttons.add(tooltipSub.addButton("Star Systems", "star", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, contentPanel.getPosition().getWidth() - 10, 30, 10f));
       ButtonAPI button =tooltipSub.addButton("Capital Abilities", "colonies", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, contentPanel.getPosition().getWidth() - 10, 30, 10f);
       button.setEnabled(AoTDFactionManager.getInstance().doesControlCapital()&& NovaExploraria.getNova()!=null&&NovaExploraria.getNova().isFunctional());
        buttons.add(button);
        buttons.add(tooltipSub.addButton("Global Market Data", "commodities", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, contentPanel.getPosition().getWidth() - 10, 30, 10f));
        tooltip.endSubTooltip();

        tooltip.addCustom(new FactionBonusPanel(contentPanel.getPosition().getWidth(), contentPanel.getPosition().getHeight() - tooltipSub.getHeightSoFar() - tooltip.getHeightSoFar() - 205f, false).getMainPanel(), 5f);
        tooltip.addSectionHeading("Ambition", Alignment.MID, 5f);
        String amb = "Show Ambition Progress";
        if(AmbitionManager.getInstance().getCurrentAmbition()==null){
            amb = "Choose Ambition";
        }
        buttons.add(        tooltip.addButton(amb, "ambition", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, contentPanel.getPosition().getWidth() - 10, 30, 8f));
        float y = contentPanel.getPosition().getHeight() - tooltipSub.getHeightSoFar() - 5f;
        tooltip.addCustom(tooltipSub, 0f).getPosition().inTL(-5, y);
        tooltip.addCustom(createGatheringPointBar(contentPanel.getPosition().getWidth()), 5f).getPosition().inTL(-5, y - 105);
        contentPanel.addUIElement(tooltip).inTL(5, 0);
        mainPanel.addComponent(contentPanel);
        //        mainPanel.addComponent(new FactionFlagButtonComponent(130, 130).getPanelOfButton()).inTL(((width - 10) / 2) - 65, height - 130);
//        mainPanel.addComponent(new FactionXPPanel(450, 130).getMainPanel()).inTL(((width - 10) / 2) - 65 - 455, height - 130);
//        mainPanel.addComponent(new FactionBonusPanel(450, 130).getMainPanel()).inTL(((width - 10) / 2) + 70, height - 130);

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
        buttons.stream().filter(x->!x.getCustomData().equals(current)).forEach(ButtonAPI::unhighlight);
        buttons.stream().filter(x->x.getCustomData().equals(current)).findFirst().ifPresent(ButtonAPI::highlight);
        for (ButtonAPI button : buttons) {
            if(button.isChecked()){
                button.setChecked(false);
                current = (String) button.getCustomData();
                recreateUI = true;
                break;
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
