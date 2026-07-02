package data.ui.patrolfleet.templates.shiplist;

import ashlib.data.plugins.info.ShipInfoGenerator;
import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.tot.produciton.specs.AoTDProductionSpec;
import data.kaysaar.aotd.tot.ui.UIData;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionBrowserSection;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionCustomButton;

import java.awt.*;


public class TemplateShipPickerButton extends ProductionCustomButton {

    public TemplateShipPickerButton(float width, float height, AoTDProductionSpec buttonData) {
        super(width, height, buttonData);
    }

    public TemplateShipPickerButton(
            float width,
            float height,
            AoTDProductionSpec buttonData,
            ProductionBrowserSection.ColumnLayout columnLayout
    ) {
        super(width, height, buttonData, columnLayout);
    }

    @Override
    public void createContainerContent(CustomPanelAPI container) {
        ShipHullSpecAPI shipSpec = getShipSpec();
        if (shipSpec == null) return;

        float rowHeight = container.getPosition().getHeight();
        float iconSize = rowHeight - 4f;
        float opadText = 12f;

        CustomPanelAPI shipPanel = ShipInfoGenerator.getShipImage(shipSpec, iconSize, null).one;
        container.addComponent(shipPanel).inTL(2, 2);

        FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(
                FleetMemberType.SHIP,
                AshMisc.getVaraint(shipSpec)
        );
        fleetMember.getRepairTracker().setCR(0.7f);
        fleetMember.getCrewComposition().addCrew(fleetMember.getMinCrew());
        fleetMember.updateStats();

        UIData.createTooltipForShip(fleetMember, shipPanel);

        addNameColumn(container, rowHeight, opadText);
        addTextColumn(container, "size", getSpec().getSize(), opadText, Misc.getTextColor());
        addTextColumn(container, "type", getSpec().getTypeString(), opadText,Misc.getTextColor());
        addTextColumn(container, "design", getSpec().getManufacturer(), opadText,Misc.getDesignTypeColor(getSpec().getManufacturer()));
        addTextColumn(container, "fp", formatFleetPoints(shipSpec.getFleetPoints()), opadText,Color.ORANGE);
    }

    protected void addNameColumn(CustomPanelAPI container, float rowHeight, float opadText) {
        float iconReserved = rowHeight + 2f;

        float nameWidth = hasColumn("name")
                ? Math.max(0f, getColumnWidth("name") - iconReserved)
                : Math.max(0f, container.getPosition().getWidth() - iconReserved);

        TooltipMakerAPI tl = container.createUIElement(nameWidth, rowHeight, false);
        tl.addPara(getSpec().getName(), opadText + 1f);

        container.addUIElement(tl).inTL(iconReserved, 0);
    }

    protected void addTextColumn(CustomPanelAPI container, String columnId, String text, float opadText, Color color) {
        if (!hasColumn(columnId)) return;

        TooltipMakerAPI tl = container.createUIElement(
                getColumnWidth(columnId),
                container.getPosition().getHeight(),
                false
        );

        LabelAPI label = tl.addPara(text,color, opadText);
        label.setAlignment(Alignment.MID);

        container.addUIElement(tl).inTL(getColumnStartX(columnId), 0);
    }

    protected ShipHullSpecAPI getShipSpec() {
        if (getSpec() == null) return null;

        if (getSpec().getUnderlyingSpec() instanceof ShipHullSpecAPI) {
            return (ShipHullSpecAPI) getSpec().getUnderlyingSpec();
        }

        return null;
    }

    protected String formatFleetPoints(float fp) {
        if (fp == (int) fp) {
            return String.valueOf((int) fp);
        }

        return String.format(java.util.Locale.US, "%.1f", fp);
    }
}