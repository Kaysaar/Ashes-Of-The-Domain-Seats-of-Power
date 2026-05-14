package data.ui.patrolfleet.overview.fleetview.fleetreloc;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.plugins.AoTDSopMisc;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.ui.patrolfleet.templates.shiplist.components.SortingState;
import data.kaysaar.aotd.vok.campaign.econ.megastructures.impl.scripts.BifrostMegastructure;
import data.kaysaar.aotd.vok.campaign.econ.megastructures.impl.scripts.BifrostMegastructureManager;

import java.awt.*;
import java.util.List;

public class FleetRelocationSelectorPanel implements ExtendedUIPanelPlugin {

    protected CustomPanelAPI mainPanel;
    protected CustomPanelAPI contentPanel;
    protected CustomPanelAPI infoPanel;
    protected CustomPanelAPI infoPanelContent;

    protected ButtonAPI buttonName;
    protected ButtonAPI buttonFPUsed;
    protected ButtonAPI buttonDistance;

    protected FleetRelocMarketList list;
    protected FleetLocationData currChosen;
    protected final FactionAPI faction;
    protected final MarketAPI sourceMarket;

    protected final float width;
    protected final float height;

    public FleetRelocationSelectorPanel(float width, float height, MarketAPI sourceMarket) {
        this.width = width;
        this.height = height;
        this.faction = Global.getSector().getPlayerFaction();
        this.sourceMarket = sourceMarket;

        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public FleetLocationData getCurrChosen() {
        return currChosen;
    }

    public MarketAPI getSelectedMarket() {
        if (currChosen == null) return null;
        return (MarketAPI) currChosen.buttonData;
    }

    public boolean hasSelection() {
        return getSelectedMarket() != null;
    }

    public float getTravelDaysToSelectedMarket() {
        MarketAPI market = getSelectedMarket();
        if (market == null) return 0f;

        return RouteLocationCalculator.getTravelDays(
                sourceMarket.getPrimaryEntity(),
                market.getPrimaryEntity()
        );
    }

    public boolean isSelectedMarketConnectedByBifrost() {
        MarketAPI market = getSelectedMarket();
        if (market == null) return false;

        if (!Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
            return false;
        }

        BifrostMegastructure mega = BifrostMegastructureManager.getInstance().getMegastructure();

        return mega != null &&
                mega.areStarSystemsConnected(sourceMarket.getStarSystem(), market.getStarSystem());
    }

    @Override
    public void createUI() {
        if (contentPanel != null) {
            mainPanel.removeComponent(contentPanel);
        }

        contentPanel = Global.getSettings().createCustom(width, height, null);

        float usableWidth = width - 13f;
        float section = usableWidth / 3f;

        Color base = Misc.getBasePlayerColor();
        Color bg = Misc.getDarkPlayerColor();
        Color bright = Misc.getBrightPlayerColor();

        float headerHeight = 20f;
        float infoHeight = 120f;
        float gap = 2f;

        CustomPanelAPI headerPanel = Global.getSettings().createCustom(width, headerHeight, null);
        TooltipMakerAPI headerTooltip = headerPanel.createUIElement(width, headerHeight, false);

        buttonName = headerTooltip.addAreaCheckbox(
                "Name",
                SortingState.ASCENDING,
                base,
                bg,
                bright,
                section,
                20f,
                0f
        );

        buttonFPUsed = headerTooltip.addAreaCheckbox(
                "FP allocated",
                SortingState.NON_INITIALIZED,
                base,
                bg,
                bright,
                section,
                20f,
                0f
        );

        buttonDistance = headerTooltip.addAreaCheckbox(
                "Distance (LY)",
                SortingState.NON_INITIALIZED,
                base,
                bg,
                bright,
                section,
                20f,
                0f
        );

        buttonName.getPosition().inTL(2f, 0f);
        buttonFPUsed.getPosition().inTL(section + 2f, 0f);
        buttonDistance.getPosition().inTL((section + 1f) * 2f+3f, 0f);

        buttonName.setClickable(false);
        buttonFPUsed.setClickable(false);
        buttonDistance.setClickable(false);

        headerPanel.addUIElement(headerTooltip).inTL(0f, 0f);
        contentPanel.addComponent(headerPanel).inTL(0f, 0f);

        float infoY = height - infoHeight;

        float tableY = headerHeight + gap;
        float tableHeight = infoY - tableY - gap;

        list = new FleetRelocMarketList(
                width,
                tableHeight,
                faction,
                sourceMarket
        );

        list.createUI();
        contentPanel.addComponent(list.getMainPanel()).inTL(3f, tableY);

        infoPanel = Global.getSettings().createCustom(width - 10f, infoHeight, null);
        contentPanel.addComponent(infoPanel).inTL(0f, infoY);

        mainPanel.addComponent(contentPanel).inTL(0f, 0f);

        updateInfo();
    }
    public void updateInfo() {
        if (infoPanel == null) return;

        if (infoPanelContent != null) {
            infoPanel.removeComponent(infoPanelContent);
        }

        infoPanelContent = Global.getSettings().createCustom(
                infoPanel.getPosition().getWidth(),
                infoPanel.getPosition().getHeight(),
                null
        );

        TooltipMakerAPI tooltip = infoPanelContent.createUIElement(
                infoPanelContent.getPosition().getWidth(),
                infoPanelContent.getPosition().getHeight(),
                false
        );

        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);

        if (!FleetRelocMarketList.doesPlayerFactionMeetCriteriaForInterstellarReloc()) {
            tooltip.addPara(
                    "For relocation between star systems %s must be under control of faction",
                    5f,
                    Color.ORANGE,
                    AoTDSopMisc.getAllIndustriesJoined(
                            FleetRelocMarketList.industriesAllowingInterstellarTransition.stream().toList(),
                            "or"
                    )
            ).setAlignment(Alignment.MID);
        }

        MarketAPI market = getSelectedMarket();

        if (market != null) {
            if (isSelectedMarketConnectedByBifrost()) {
                tooltip.addPara(
                        "Re-location of our forces will take few days, due to %s connecting both star systems!",
                        20,
                        Color.ORANGE,
                        "Bifrost Network"
                ).setAlignment(Alignment.MID);
            } else {
                tooltip.addPara(
                        "Re-location of our forces to %s will take around %s",
                        20,
                        Color.ORANGE,
                        market.getName(),
                        AshMisc.convertDaysToString(Math.round(getTravelDaysToSelectedMarket()))
                ).setAlignment(Alignment.MID);
            }
        }

        infoPanelContent.addUIElement(tooltip).inTL(0f, 0f);
        infoPanel.addComponent(infoPanelContent).inTL(0f, 0f);
    }

    @Override
    public void advance(float amount) {
        if (list == null || list.data == null) return;

        for (FleetLocationData datum : list.data) {
            if (datum.mainButton.isChecked()) {
                datum.mainButton.setChecked(false);
                currChosen = datum;
                updateInfo();
                break;
            }
        }

        for (FleetLocationData datum : list.data) {
            if (datum.equals(currChosen)) {
                datum.mainButton.highlight();
            } else {
                datum.mainButton.unhighlight();
            }
        }
    }

    @Override
    public void clearUI() {
        if (list != null) {
            list.clearUI();
        }

        if (contentPanel != null) {
            mainPanel.removeComponent(contentPanel);
            contentPanel = null;
        }

        infoPanel = null;
        infoPanelContent = null;
        buttonName = null;
        buttonFPUsed = null;
        buttonDistance = null;
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
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}