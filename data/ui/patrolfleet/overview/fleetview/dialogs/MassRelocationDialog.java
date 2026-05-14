package data.ui.patrolfleet.overview.fleetview.dialogs;

import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.ui.patrolfleet.overview.fleetview.FleetOptions;
import data.ui.patrolfleet.overview.fleetview.massorders.MassRelocationOrderUI;

public class MassRelocationDialog extends BasePopUpDialog {

    MassRelocationOrderUI order;
    float height = 0f;
    MarketAPI marketAPI;
    FleetOptions options;

    public MassRelocationDialog(MarketAPI marketToRelocateFrom, FleetOptions options) {
        super("Order Mass Relocation from " + marketToRelocateFrom.getName());
        this.marketAPI = marketToRelocateFrom;
        this.options = options;
    }

    public void createUI(CustomPanelAPI panelAPI) {
        this.createHeaader(panelAPI);

        TooltipMakerAPI tooltip = panelAPI.createUIElement(
                panelAPI.getPosition().getWidth() - 30f,
                panelAPI.getPosition().getHeight() - this.y,
                true
        );

        height = panelAPI.getPosition().getHeight() - this.y;

        this.createContentForDialog(
                tooltip,
                panelAPI.getPosition().getWidth() - 30f
        );

        this.addTooltip(tooltip);
        panelAPI.addUIElement(tooltip).inTL(this.x, this.y);
        this.createConfirmAndCancelSection(panelAPI);
    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        order = new MassRelocationOrderUI(width, height, marketAPI);
        tooltip.addCustom(order.getMainPanel(), 0f);
        tooltip.setHeightSoFar(0f);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (order != null) {
            order.advance(amount);
        }

        if (getConfirmButton() != null && order != null) {
            boolean shouldEnable = order.shouldEnableConfirm();

            if (getConfirmButton().isEnabled() != shouldEnable) {
                getConfirmButton().setEnabled(shouldEnable);
            }
        }
    }

    @Override
    public void applyConfirmScript() {
        super.applyConfirmScript();

        if (order != null) {
            order.executeConfirmSection();
            order.clearUI();
        }

        options.forceDismiss();
    }

    @Override
    public void onExit() {
        if (order != null) {
            order.clearUI();
        }
    }
}