package data.ui.patrolfleet.overview.fleetview.massorders;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.aotd_entities.BiFrostGateEntity;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;
import data.kaysaar.aotd.vok.campaign.econ.megastructures.impl.scripts.BifrostMegastructure;
import data.kaysaar.aotd.vok.campaign.econ.megastructures.impl.scripts.BifrostMegastructureManager;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.utilis.PatrolFleetFactory;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.overview.fleetview.FleetButtonComponent;
import data.ui.patrolfleet.overview.fleetview.fleetreloc.FleetRelocationSelectorPanel;
import data.ui.patrolfleet.overview.fleetview.templatelist.FleetButtonDrop;
import data.ui.patrolfleet.overview.fleetview.templatelist.FleetTable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MassRelocationOrderUI implements ExtendedUIPanelPlugin {

    protected CustomPanelAPI mainPanel;
    protected CustomPanelAPI contentPanel;
    protected CustomPanelAPI infoPanel;
    protected CustomPanelAPI infoPanelContent;

    protected FleetTable tableOfFleets;
    protected FleetRelocationSelectorPanel relocationSelector;

    protected MarketAPI sourceMarket;
    protected ArrayList<BasePatrolFleet> fleetsToRelocate = new ArrayList<>();

    protected float widthFirstSection = 700f;
    protected float infoHeight = 120f;
    protected float headingHeight = 20f;
    protected float gap = 5f;

    public MassRelocationOrderUI(float width, float height, MarketAPI sourceMarket) {
        this.sourceMarket = sourceMarket;
        mainPanel = Global.getSettings().createCustom(width, height, this);
        createUI();
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public ArrayList<BasePatrolFleet> getFleetsToRelocate() {
        return fleetsToRelocate;
    }

    public MarketAPI getSelectedMarket() {
        if (relocationSelector == null) return null;
        return relocationSelector.getSelectedMarket();
    }

    public boolean shouldEnableConfirm() {
        return !fleetsToRelocate.isEmpty()
                && relocationSelector != null
                && relocationSelector.hasSelection();
    }

    public void executeConfirmSection() {
        if(relocationSelector == null) return;
        if(!relocationSelector.hasSelection())return;
        MarketAPI curr = relocationSelector.getSelectedMarket();


        for (BasePatrolFleet fleetData : fleetsToRelocate) {
            float days = RouteLocationCalculator.getTravelDays(
                    curr.getPrimaryEntity(),
                    fleetData.getTiedTo().getPrimaryEntity()
            );
            if(!AoTDMilitaryBase.isPatroling(fleetData.getId(), fleetData.getTiedTo())){
                FleetParamsV3 params = new FleetParamsV3(
                        fleetData.getTiedTo(),
                        null,
                        fleetData.getTiedTo().getFactionId(),
                        Misc.getShipQuality(fleetData.getTiedTo(), fleetData.getTiedTo().getFactionId()),
                        null,
                        fleetData.getFPTaken(), // combatPts
                        0f, // freighterPts
                        0f, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        0f // qualityMod
                );
                CampaignFleetAPI fleetToSpawn = PatrolFleetFactory.createFleetFromAssigned(fleetData, params, fleetData.getTiedTo(), params.random);
                fleetData.getTiedTo().getContainingLocation().addEntity(fleetToSpawn);
                fleetToSpawn.setFacing((float) Math.random() * 360f);
                // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
                fleetToSpawn.setLocation(fleetData.getTiedTo().getPrimaryEntity().getLocation().x, fleetData.getTiedTo().getPrimaryEntity().getLocation().y);
                fleetToSpawn.addEventListener(new FleetEventListener() {
                    @Override
                    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
                        if(reason.equals(CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION)){
                            fleetData.setInTransit(false);
                        }
                        else{
                            fleetData.forceTransitDays();
                        }
                    }

                    @Override
                    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

                    }
                });
                fleetToSpawn.setTransponderOn(true);
                fleetToSpawn.addAbility(Abilities.SUSTAINED_BURN);
                fleetToSpawn.getAbility(Abilities.SUSTAINED_BURN).activate();
                fleetToSpawn.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
                fleetToSpawn.addAssignment(FleetAssignment.ORBIT_PASSIVE, fleetData.getTiedTo().getPrimaryEntity(),1f,"Preparing for re-location");
                if (Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
                    /// TOOD - MEgA
                    BifrostMegastructure mega = BifrostMegastructureManager.getInstance().getMegastructure();
                    if(mega!=null&&mega.areStarSystemsConnected(fleetData.getTiedTo().getStarSystem(),curr.getStarSystem())){
                        SectorEntityToken token = mega.getSectionEntityInStarSystem(fleetData.getTiedTo().getStarSystem());
                        SectorEntityToken travel = mega.getSectionEntityInStarSystem(curr.getStarSystem());

                        fleetToSpawn.addAssignment(FleetAssignment.GO_TO_LOCATION, token, 15, "Taking course towards Bifrost Gate",new Script() {
                            @Override
                            public void run() {
                                float distLY = Misc.getDistanceLY(token, travel);
                                if (token.getCustomPlugin() instanceof BiFrostGateEntity) {
                                    BiFrostGateEntity plugin = (BiFrostGateEntity) token.getCustomPlugin();
                                    plugin.showBeingUsed(distLY);
                                }
                                if (travel.getCustomPlugin() instanceof BiFrostGateEntity) {
                                    BiFrostGateEntity plugin = (BiFrostGateEntity) travel.getCustomPlugin();
                                    plugin.showBeingUsed(distLY);
                                }
                                JumpPointAPI.JumpDestination dest = new JumpPointAPI.JumpDestination(travel, null);
                                Global.getSector().doHyperspaceTransition(fleetToSpawn, token, dest, 2f);

                            }
                        });


                    }

                }
                fleetToSpawn.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, curr.getPrimaryEntity(),10000f,"Relocating to new market");


            }
            fleetData.setInTransit(true,days);

            fleetData.setTiedTo(curr);
        }
        OverviewPatrolPanel.forceRequestUpdate = true;
    }

    @Override
    public void createUI() {
        if (contentPanel != null) {
            mainPanel.removeComponent(contentPanel);
        }

        float width = mainPanel.getPosition().getWidth();
        float height = mainPanel.getPosition().getHeight();

        contentPanel = Global.getSettings().createCustom(width, height, null);

        float widthSecondSection = width - widthFirstSection - gap;

        float infoY = height - infoHeight+10;
        float tableY = headingHeight + gap;
        float tableHeight = infoY - tableY - gap;

        TooltipMakerAPI tlFirst = contentPanel.createUIElement(widthFirstSection, headingHeight, false);
        TooltipMakerAPI tlSecond = contentPanel.createUIElement(widthSecondSection, headingHeight, false);

        tlFirst.addSectionHeading("Fleets to relocate", Alignment.MID, 0f);
        tlSecond.addSectionHeading("Relocation target", Alignment.MID, 0f);

        contentPanel.addUIElement(tlFirst).inTL(0f, 0f);
        contentPanel.addUIElement(tlSecond).inTL(widthFirstSection + gap, 0f);
        final MassRelocationOrderUI self = this;
        if (tableOfFleets == null) {


            tableOfFleets = new FleetTable(
                    widthFirstSection,
                    tableHeight,
                    true,
                    0,
                    0,
                    AoTDFactionPatrolsManager.getInstance().getAssignedFleetsForMarketForRelocation(sourceMarket)
            ) {
                @Override
                public void reportButtonPressed(CustomButton buttonPressed) {
                    if (buttonPressed instanceof FleetButtonComponent component) {
                        BasePatrolFleet fleet = component.getData();

                        if (fleetsToRelocate.contains(fleet)) {
                            fleetsToRelocate.remove(fleet);
                        } else {
                            fleetsToRelocate.add(fleet);
                        }

                        self.updateInfo();
                    }
                }

                @Override
                public void advance(float amount) {
                    super.advance(amount);

                    dropDownButtons.forEach(x -> {
                        if (x instanceof FleetButtonDrop bt) {
                            if (fleetsToRelocate.contains(bt.fleet)) {
                                x.mainButton.mainButton.highlight();
                            } else {
                                x.mainButton.mainButton.unhighlight();
                            }
                        }
                    });
                }
            };

            tableOfFleets.createSections();
            tableOfFleets.createTable();
        }

        if (relocationSelector == null) {
            relocationSelector = new FleetRelocationSelectorPanel(
                    widthSecondSection,
                    tableHeight+123,
                    sourceMarket
            ){
                @Override
                public void updateInfo() {
                    super.updateInfo();
                    self.updateInfo();
                }
            };
        }

        contentPanel.addComponent(tableOfFleets.mainPanel).inTL(0f, tableY);
        contentPanel.addComponent(relocationSelector.getMainPanel()).inTL(widthFirstSection + gap, tableY);

        infoPanel = Global.getSettings().createCustom(width, infoHeight, null);
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

        MarketAPI target = getSelectedMarket();

        if (fleetsToRelocate.isEmpty()) {
            tooltip.addPara(
                    "Select at least one fleet to relocate.",
                    18f,
                    Color.ORANGE
            );
        } else if (target == null) {
            String fleets = fleetsToRelocate.size() == 1 ? "one fleet" : "multiple fleets";

            tooltip.addPara(
                    "You have selected %s for relocation.",
                    18,
                    Color.ORANGE,
                    fleets
            );

            tooltip.addPara(
                    "Choose destination market to proceed.",
                    3f,
                    Color.ORANGE
            );
        } else {
            String fleets = fleetsToRelocate.size() == 1 ? "one fleet" : "multiple fleets";

            tooltip.addPara(
                    "You are about to relocate %s from %s.",
                    18f,
                    Color.ORANGE,
                    fleets,
                    sourceMarket.getName()
            );

        }

        infoPanelContent.addUIElement(tooltip).inTL(0f, 0f);
        infoPanel.addComponent(infoPanelContent).inTL(0f, 0f);
    }

    @Override
    public void clearUI() {
        if (tableOfFleets != null) {
            tableOfFleets.clearUI();
        }

        if (relocationSelector != null) {
            relocationSelector.clearUI();
        }

        if (contentPanel != null) {
            mainPanel.removeComponent(contentPanel);
            contentPanel = null;
        }

        infoPanel = null;
        infoPanelContent = null;
    }

    @Override
    public void advance(float amount) {
        if (tableOfFleets != null) {
            tableOfFleets.advance(amount);
        }

        if (relocationSelector != null) {
            boolean hadSelection = relocationSelector.hasSelection();

            MarketAPI before = relocationSelector.getSelectedMarket();
            relocationSelector.advance(amount);
            MarketAPI after = relocationSelector.getSelectedMarket();

            if (before != after || hadSelection != relocationSelector.hasSelection()) {
                updateInfo();
            }
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
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}