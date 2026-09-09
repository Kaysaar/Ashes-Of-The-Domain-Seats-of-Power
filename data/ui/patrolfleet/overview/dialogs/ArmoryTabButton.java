package data.ui.patrolfleet.overview.dialogs;

import ashlib.data.plugins.info.FighterInfoGenerator;
import ashlib.data.plugins.info.ShipInfoGenerator;
import ashlib.data.plugins.info.WeaponInfoGenerator;
import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.FormationType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.tot.produciton.specs.AoTDProductionSpec;
import data.kaysaar.aotd.tot.ui.UIData;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionBrowserSection;
import data.kaysaar.aotd.tot.ui.customprod.components.ProductionCustomButton;


public class ArmoryTabButton extends ProductionCustomButton {

    protected ArmoryBrowserSection browserSection;

    public ArmoryTabButton(
            float width,
            float height,
            AoTDProductionSpec buttonData,
            ProductionBrowserSection.ColumnLayout columnLayout
    ) {
        super(width, height, buttonData, columnLayout);
    }

    public ArmoryTabButton(
            float width,
            float height,
            AoTDProductionSpec buttonData,
            ProductionBrowserSection.ColumnLayout columnLayout,
            ArmoryBrowserSection browserSection
    ) {
        super(width, height, buttonData, columnLayout);
        this.browserSection = browserSection;
    }

    @Override
    public void createContainerContent(CustomPanelAPI container) {
        float rowHeight = container.getPosition().getHeight();
        float iconSize = rowHeight - 4f;
        float opadText = 6f;

        addIconAndTooltip(container, iconSize);

        addNameColumn(container, rowHeight, opadText);
        addTextColumn(container, "size", getSpec().getSize(), opadText);
        addTextColumn(container, "type", getSpec().getTypeString(), opadText);
        addTextColumn(container, "design", getSpec().getManufacturer(), opadText, true);
        addPriorityColumn(container, opadText);
    }

    protected void addIconAndTooltip(CustomPanelAPI container, float iconSize) {
        Object underlying = getSpec().getUnderlyingSpec();

        if (underlying instanceof ShipHullSpecAPI) {
            addShipIcon(container, iconSize, (ShipHullSpecAPI) underlying);
            return;
        }

        if (underlying instanceof WeaponSpecAPI) {
            addWeaponIcon(container, iconSize, (WeaponSpecAPI) underlying);
            return;
        }

        if (underlying instanceof FighterWingSpecAPI) {
            addFighterIcon(container, iconSize, (FighterWingSpecAPI) underlying);
        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        boolean priority = browserSection != null && browserSection.isPriorityCached(getSpec());
        if(priority){
            mainButton.highlight();
        }
        else{
            mainButton.unhighlight();
        }
    }

    protected void addShipIcon(CustomPanelAPI container, float iconSize, ShipHullSpecAPI shipSpec) {
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
    }

    protected void addWeaponIcon(CustomPanelAPI container, float iconSize, WeaponSpecAPI weaponSpec) {
        CustomPanelAPI weaponPanel = WeaponInfoGenerator.getImageOfWeapon(
                weaponSpec,
                Math.max(1f, iconSize - 10f)
        ).one;

        container.addComponent(weaponPanel).inTL(5, 7);

        UIData.createWeaponTooltip(weaponSpec, weaponPanel);
    }

    protected void addFighterIcon(CustomPanelAPI container, float iconSize, FighterWingSpecAPI fighterSpec) {
        CustomPanelAPI fighterPanel = FighterInfoGenerator.createFormationPanel(
                fighterSpec,
                FormationType.BOX,
                (int) Math.max(1f, iconSize),
                fighterSpec.getNumFighters()
        ).one;

        container.addComponent(fighterPanel).inTL(2, 2);

        FleetMemberAPI fleetMember = Global.getFactory().createFleetMember(
                FleetMemberType.FIGHTER_WING,
                getSpec().getId()
        );

        UIData.createFighterTooltip(fleetMember, fighterSpec, fighterPanel);

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

    protected void addTextColumn(CustomPanelAPI container, String columnId, String text, float opadText) {
        addTextColumn(container, columnId, text, opadText, false);
    }

    protected void addTextColumn(
            CustomPanelAPI container,
            String columnId,
            String text,
            float opadText,
            boolean useManufacturerColor
    ) {
        if (!hasColumn(columnId)) return;

        TooltipMakerAPI tl = container.createUIElement(
                getColumnWidth(columnId),
                container.getPosition().getHeight(),
                false
        );

        LabelAPI label;

        if (useManufacturerColor) {
            label = tl.addPara(text == null ? "" : text, getSpec().getManufacturerColor(), opadText);
        } else {
            label = tl.addPara(text == null ? "" : text, opadText);
        }

        label.setAlignment(Alignment.MID);

        container.addUIElement(tl).inTL(getColumnStartX(columnId), 0);
    }

    protected void addPriorityColumn(CustomPanelAPI container, float opadText) {
        if (!hasColumn("priority")) return;
        boolean priority = browserSection != null && browserSection.isPriorityCached(getSpec());

        if(priority){
            float middleX = getColumnStartX("priority")+(getColumnWidth("priority")/2f);
            ImageViewer viewer = new ImageViewer(15,15,Global.getSettings().getSpriteName("ui","checkmark"));
            viewer.setColorOverlay(Misc.getPositiveHighlightColor());
            container.addComponent(viewer.getComponentPanel()).inTL(middleX-(viewer.getComponentPanel().getPosition().getWidth()/2), container.getPosition().getHeight()/2-(viewer.getComponentPanel().getPosition().getHeight()/2));
        }


    }
}