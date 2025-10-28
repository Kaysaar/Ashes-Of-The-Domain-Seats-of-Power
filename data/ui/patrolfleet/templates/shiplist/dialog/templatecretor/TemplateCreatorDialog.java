package data.ui.patrolfleet.templates.shiplist.dialog.templatecretor;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;
import data.scripts.patrolfleet.managers.FactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.overview.fleetview.AvailableTemplateList;
import data.ui.patrolfleet.templates.TemplatePanel;
import data.ui.patrolfleet.templates.TemplateShowcase;
import data.ui.patrolfleet.templates.shiplist.components.ShipSelector;
import data.ui.patrolfleet.templates.shiplist.dialog.templaterandom.TemplateRandomSection;

import java.util.Map;

public class TemplateCreatorDialog extends BasePopUpDialog {
    ShipSelector selector;
    TemplateCreatorShowcase showcase;
    TemplatePanel referencePanel;
    TemplateShowcase showRefernce;
    CustomPanelAPI buttonPanel;
    CustomPanelAPI contentOfButtonPanel;
    ButtonAPI toggleMode;
    TemplateRandomSection randSection;
    ButtonAPI templateList;
    boolean randomMode = false;
    boolean patrolFleetCreatorMode = false;
    CustomPanelAPI content;
    MarketAPI market;
    BasePatrolFleet fleet;
    public TemplateCreatorShowcase getShowcase() {
        return showcase;
    }

    public TemplateCreatorDialog(String headerTitle) {
        super(headerTitle);
    }

    public TemplateCreatorDialog(String headerTitle, TemplatePanel referenceParent) {
        super(headerTitle);
        this.referencePanel = referenceParent;
    }
    public TemplateCreatorDialog(String headerTitle, boolean patrolFleetCreator, MarketAPI market) {
        super(headerTitle);
        this.market = market;
        this.patrolFleetCreatorMode = patrolFleetCreator;
    }
    public TemplateCreatorDialog(String headerTitle, boolean patrolFleetCreator,BasePatrolFleet fleet, MarketAPI market) {
        super(headerTitle);
        this.market = market;
        this.fleet = fleet;
        this.patrolFleetCreatorMode = patrolFleetCreator;
    }
    public TemplateCreatorDialog(String headerTitle, TemplateShowcase referencePanel) {
        super(headerTitle);
        this.showRefernce = referencePanel;
    }

    public void updateContent() {
        if (randomMode) {
            content.removeComponent(selector.getMainPanel());
            content.addComponent(randSection.getMainPanel());
        } else {
            content.removeComponent(randSection.getMainPanel());
            content.addComponent(selector.getMainPanel());

        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);


        if (selector != null && showcase != null) {
            for (ButtonAPI orderButton : selector.optionPanel.getOrderButtons()) {
                if (orderButton.isChecked()) {
                    orderButton.setChecked(false);
                    if (orderButton.getCustomData() instanceof ShipHullSpecAPI spec) {
                        showcase.addShip(spec.getHullId());
                    }
                }
            }
        }
        if (toggleMode != null && toggleMode.isChecked()) {
            toggleMode.setChecked(false);
            randomMode = !randomMode;
            updateContentOfButton();
            updateContent();
        }
        if(randSection!=null&&showcase!=null){
            if(randSection.reqGeneration){
                showcase.list.getShips().clear();
                showcase.list.getShips().putAll(randSection.generateData());
                showcase.list.createUI();
            }
        }
        if(showcase!=null){
            boolean canConfirm  =AshMisc.isStringValid(showcase.textForName.getText())&&showcase.list.getCountOfShips()<=30&&showcase.list.getCountOfShips()>0;
            boolean additionalConfirm = true;
            if(patrolFleetCreatorMode){
                additionalConfirm = showcase.list.getFleetPoints(false)<= FactionPatrolsManager.getInstance().getAvailableFP();
            }
            if(fleet!=null){
                additionalConfirm = showcase.list.getFleetPoints(false)<= FactionPatrolsManager.getInstance().getAvailableFP()+fleet.geTotalFpTaken();
            }
            if(confirmButton.isEnabled()!=(canConfirm&&additionalConfirm)){
                confirmButton.setEnabled(canConfirm&&additionalConfirm);
            }
        }
        if(templateList!=null&&templateList.isChecked()){
            templateList.setChecked(false);
            AvailableTemplateList list = new AvailableTemplateList(this);
            AshMisc.placePopUpUI(list,templateList,700,400);
        }

    }


    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        content = Global.getSettings().createCustom(width, 500, null);

        selector = new ShipSelector(770, 495);
        randSection = new TemplateRandomSection(770, 495);
        content.addComponent(selector.getMainPanel()).inTL(0, 2);
        showcase = new TemplateCreatorShowcase(width - 780, 500,patrolFleetCreatorMode);
        if (showRefernce != null) {
            showcase.setExistingTemplate(showRefernce.getTemplate());
        }
        if(fleet!=null){
            showcase.setExistingTemplate(fleet);
        }
        showcase.createUI();
        content.addComponent(showcase.getMainPanel()).inTL(775, -1);
        tooltip.addCustom(content, 2f);
        tooltip.setHeightSoFar(0f);
    }

    @Override
    public void createConfirmAndCancelSection(CustomPanelAPI mainPanel) {
        super.createConfirmAndCancelSection(mainPanel);
        float totalWidth = 400f;
        buttonPanel = Global.getSettings().createCustom(totalWidth, 25, null);
        updateContentOfButton();
        mainPanel.addComponent(buttonPanel).inTL(10.0F, mainPanel.getPosition().getHeight() - 40.0F);
    }

    public void updateContentOfButton() {
        if (contentOfButtonPanel != null) {
            buttonPanel.removeComponent(contentOfButtonPanel);
        }
        contentOfButtonPanel = Global.getSettings().createCustom(buttonPanel.getPosition().getWidth(), 25, null);
        TooltipMakerAPI tooltip = contentOfButtonPanel.createUIElement(buttonPanel.getPosition().getWidth(), 25.0F, false);
        tooltip.setButtonFontOrbitron20();
        if (!randomMode) {
            toggleMode = tooltip.addButton("Toggle Random-Mode", "random", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, buttonConfirmWidth * 1.5F, 25.0F, 0.0F);

        } else {
            toggleMode = tooltip.addButton("Toggle Normal-Mode", "normal", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TL_BR, buttonConfirmWidth * 1.5F, 25.0F, 0.0F);

        }
        if(patrolFleetCreatorMode){
            templateList =tooltip.addButton("Pick available template", "normal", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.BL_TR, buttonConfirmWidth * 1.8f, 25.0F, 0.0F);
            templateList.getPosition().inTL(buttonConfirmWidth*1.5F+10,0);
        }
        contentOfButtonPanel.addUIElement(tooltip).inTL(0, 0);
        buttonPanel.addComponent(contentOfButtonPanel).inTL(0, 0);
    }

    @Override
    public void applyConfirmScript() {
        super.applyConfirmScript();
        if(patrolFleetCreatorMode){
            if(fleet==null){
                BasePatrolFleet fleet = new BasePatrolFleet(showcase.list.getShips(),showcase.textForName.getText());
                fleet.setTiedTo(market);
                fleet.setFleetName(showcase.textForName.getText());
                FactionPatrolsManager.getInstance().addNewFleet(fleet);
                OverviewPatrolPanel.forceRequestUpdate = true;
            }
            else{
                if(AoTDMilitaryBase.isPatroling(fleet.getId(),fleet.getTiedTo())){
                    fleet.getShipsForReplacementWhenInPrep().putAll(showcase.list.getShips());
                    OverviewPatrolPanel.forceRequestUpdate = true;
                }
                else{
                    fleet.assignedShipsThatShouldSpawn.clear();
                    fleet.data.clear();
                    BasePatrolFleet fleet = new BasePatrolFleet(showcase.list.getShips(),showcase.textForName.getText());
                    this.fleet.assignedShipsThatShouldSpawn.putAll(fleet.assignedShipsThatShouldSpawn);
                    this.fleet.data.putAll(fleet.data);
                    fleet.setFleetName(showcase.textForName.getText());
                    OverviewPatrolPanel.forceRequestUpdate = true;
                }

            }



        }
        else{
            if (showRefernce != null && showRefernce.getTemplate() != null) {
                int index = 0;
                for (Map.Entry<String, BasePatrolFleetTemplate> entry : PatrolTemplateManager.templates.entrySet()) {
                    if (entry.getKey().equals(showRefernce.getTemplate().getNameOfTemplate())) {
                        break;
                    }
                    index++;
                }
                BasePatrolFleetTemplate template = new BasePatrolFleetTemplate(showcase.list.getShips(), showcase.textForName.getText());
                AshMisc.replaceEntryAtIndex(PatrolTemplateManager.templates, index, showcase.textForName.getText(), template);
                PatrolTemplateManager.saveAllExistingTemplates();
                TemplatePanel.forceRequestUpdate = true;
                return;

            }
            PatrolTemplateManager.templates.put(showcase.textForName.getText(), new BasePatrolFleetTemplate(showcase.list.getShips(), showcase.textForName.getText()));
            PatrolTemplateManager.saveAllExistingTemplates();
            TemplatePanel.forceRequestUpdate = true;
        }


    }

    @Override
    public void onExit() {
        super.onExit();
        selector.clearUI();
    }
}
