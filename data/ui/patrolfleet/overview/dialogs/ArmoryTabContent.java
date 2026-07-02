package data.ui.patrolfleet.overview.dialogs;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import data.kaysaar.aotd.tot.produciton.specs.AoTDProductionSpec;

import java.util.List;

public class ArmoryTabContent implements ExtendedUIPanelPlugin {
    protected CustomPanelAPI mainPanel;
    protected CustomPanelAPI componentPanel;

    protected ArmoryTypesSection section;
    protected ArmoryBrowserSection browserSection;

    protected AoTDProductionSpec.AoTDProductionSpecType currType = AoTDProductionSpec.AoTDProductionSpecType.SHIP;

    public ArmoryTabContent(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        if (componentPanel != null) {
            mainPanel.removeComponent(componentPanel);
        }

        componentPanel = Global.getSettings().createCustom(
                mainPanel.getPosition().getWidth(),
                mainPanel.getPosition().getHeight(),
                null
        );

        section = new ArmoryTypesSection();

        browserSection = createBrowserSection(
                componentPanel.getPosition().getWidth(),
                componentPanel.getPosition().getHeight(),
                currType
        );
        browserSection.createUI();

        componentPanel.addComponent(browserSection.getMainPanel()).inTL(0, 0);
        componentPanel.addComponent(section.getMainPanel()).inTL(0, 0);

        mainPanel.addComponent(componentPanel).inTL(0, 0);
    }

    protected ArmoryBrowserSection createBrowserSection(
            float width,
            float height,
            AoTDProductionSpec.AoTDProductionSpecType type
    ) {
        return new ArmoryBrowserSection(width, height, type);
    }

    /**
     * Call this from the owning dialog confirm/accept button.
     *
     * Until this method is called, priority changes stay cached in ArmoryBrowserSection.
     */
    public void confirmRun() {
        if (browserSection != null) {
            browserSection.confirmRun();
        }
    }

    public ArmoryBrowserSection getBrowserSection() {
        return browserSection;
    }

    public ArmoryTypesSection getTypesSection() {
        return section;
    }

    public AoTDProductionSpec.AoTDProductionSpecType getCurrType() {
        return currType;
    }

    @Override
    public void clearUI() {
        if (browserSection != null) {
            browserSection.clearUI();
            browserSection = null;
        }

        if (section != null) {
            section.clearUI();
            section = null;
        }

        if (componentPanel != null) {
            mainPanel.removeComponent(componentPanel);
            componentPanel = null;
        }
    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {
        if (section == null || browserSection == null) return;

        if (section.isNeedsUpdate()) {
            section.setNeedsUpdate(false);

            AoTDProductionSpec.AoTDProductionSpecType prev = currType;
            currType = (AoTDProductionSpec.AoTDProductionSpecType) section.curr.getCustomData();

            if (prev != currType) {
                browserSection.setProdType(currType);
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