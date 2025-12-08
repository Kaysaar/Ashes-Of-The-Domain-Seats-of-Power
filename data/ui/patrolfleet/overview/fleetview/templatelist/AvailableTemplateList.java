package data.ui.patrolfleet.overview.fleetview.templatelist;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import ashlib.data.plugins.ui.models.PopUpUI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.overview.fleetview.FleetButtonComponent;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.util.ArrayList;

public class AvailableTemplateList extends BasePopUpDialog {
    CustomPanelAPI mainPanel;
    ArrayList<FleetButtonComponent>components = new ArrayList<>();
    TemplateCreatorDialog dialog;
    AvailableTemplateListPlugin plugin;
    public AvailableTemplateList(TemplateCreatorDialog dialog) {
        super("Choose template for fleet");
        this.dialog = dialog;

    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
         plugin =new AvailableTemplateListPlugin(width,panelToInfluence.getPosition().getHeight() - this.y);
        plugin.createUI();
        tooltip.addCustom(plugin.getMainPanel(),0f).getPosition().inTL(-3,0);
        tooltip.setHeightSoFar(0f);
    }


    @Override
    public void applyConfirmScript() {
        if(plugin.table.currFleet!=null){
            dialog.getShowcase().getList().getShips().clear();
            dialog.getShowcase().getList().getShips().putAll(plugin.table.currFleet.assignedShipsThatShouldSpawn);
            if(!AshMisc.isStringValid(dialog.getShowcase().getTextForName().getText())||PatrolTemplateManager.templates.get(dialog.getShowcase().getTextForName().getText())!=null){
                dialog.getShowcase().getTextForName().setText(plugin.table.currFleet.getNameOfFleet());
            }

            dialog.getShowcase().getList().createUI();

        }
    }

    @Override
    public void onExit() {
        plugin.clearUI();


        super.onExit();
    }
}
