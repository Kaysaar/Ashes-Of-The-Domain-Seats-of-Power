package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.models.PopUpUI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.util.ArrayList;
import java.util.List;

public class AvailableTemplateList extends PopUpUI {
    CustomPanelAPI mainPanel;
    MarketAPI market;
    ArrayList<FleetButtonComponent>components = new ArrayList<>();
    TemplateCreatorDialog dialog;
    BasePatrolFleet chosenFleet= null;
    public AvailableTemplateList(TemplateCreatorDialog dialog) {
        this.dialog = dialog;

    }
    @Override
    public void createUI(CustomPanelAPI panelAPI) {
        createUIMockup(panelAPI);
        panelAPI.addComponent(mainPanel).inTL(0, 0);
    }

    @Override
    public float createUIMockup(CustomPanelAPI panelAPI) {
        mainPanel = panelAPI.createCustomPanel(panelAPI.getPosition().getWidth(), panelAPI.getPosition().getHeight(), null);
        TooltipMakerAPI tooltip = mainPanel.createUIElement(panelAPI.getPosition().getWidth(),panelAPI.getPosition().getHeight(),true);
        tooltip.addSpacer(0f).getPosition().inTL(0,0);
        for (BasePatrolFleetTemplate value : PatrolTemplateManager.getTemplatesAvailableSorted().values()) {
            BasePatrolFleet fleet = new BasePatrolFleet(value);
            fleet.setFleetName(value.nameOfTemplate);
            FleetButtonComponent test = new FleetButtonComponent(mainPanel.getPosition().getWidth()-5,60,fleet,true);
            components.add(test);
            test.createUI();
            tooltip.addCustom(test.getMainPanel(),5f);

        }
        tooltip.addSpacer(5f);
        mainPanel.getPosition().setSize(panelAPI.getPosition().getWidth(),tooltip.getHeightSoFar());
        mainPanel.addUIElement(tooltip).inTL(-3,0);
        return tooltip.getHeightSoFar();


    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        for (FleetButtonComponent component : components) {
            if(component.mainButton.isChecked()){
                component.mainButton.setChecked(false);
                chosenFleet = component.getData();
                forceDismiss();
                break;
            }
        }
    }

    @Override
    public void onExit() {
//        for (FleetButtonComponent component : components) {
//            component.clearUI();
//        }
        components.clear();
        if(chosenFleet!=null){
            dialog.getShowcase().getList().getShips().clear();
            dialog.getShowcase().getList().getShips().putAll(chosenFleet.assignedShipsThatShouldSpawn);
            dialog.getShowcase().getTextForName().setText(chosenFleet.getNameOfFleet());
            dialog.getShowcase().getList().createUI();
        }

        super.onExit();
    }
}
