package data.ui.patrolfleet.overview.fleetview.dialogs;

import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.ui.patrolfleet.overview.fleetview.FleetOptions;
import data.ui.patrolfleet.overview.fleetview.massorders.MassDecommissionOrderUI;
import data.ui.patrolfleet.overview.fleetview.massorders.MassDeploymentOrderUI;

public class MassDecommissionDialog extends BasePopUpDialog {
    MassDecommissionOrderUI order;
    float height = 0;
    MarketAPI marketAPI;
    FleetOptions options;
    public MassDecommissionDialog(MarketAPI marketToDeploy, FleetOptions options) {
        super("De-commission fleets stationed in "+marketToDeploy.getName());
        this.marketAPI = marketToDeploy;
        this.options = options;
    }
    public void createUI(CustomPanelAPI panelAPI) {
        this.createHeaader(panelAPI);
        TooltipMakerAPI tooltip = panelAPI.createUIElement(panelAPI.getPosition().getWidth() - 30.0F, panelAPI.getPosition().getHeight() - this.y, true);
        height = panelAPI.getPosition().getHeight() - this.y;
        this.createContentForDialog(tooltip, panelAPI.getPosition().getWidth() - 30.0F);
        this.addTooltip(tooltip);
        panelAPI.addUIElement(tooltip).inTL(this.x, this.y);
        this.createConfirmAndCancelSection(panelAPI);
    }
    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        order = new MassDecommissionOrderUI(width,height,marketAPI);
        tooltip.addCustom(order.getMainPanel(),0f);
        tooltip.setHeightSoFar(0f);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
    }

    @Override
    public void applyConfirmScript() {
        super.applyConfirmScript();
        order.executeConfirmSection();
        order.clearUI();
        options.forceDismiss();
        options.getData().createUI();
    }

}
