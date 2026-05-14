package data.ui.patrolfleet.overview.fleetview.massorders;

import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.DropDownButton;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;
import data.ui.patrolfleet.overview.fleetview.FleetButtonComponent;
import data.ui.patrolfleet.overview.fleetview.templatelist.AvailableTemplateListPlugin;
import data.ui.patrolfleet.overview.fleetview.templatelist.FleetTable;
import data.ui.patrolfleet.templates.filter.TemplateManufactureFilter;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MassDeploymentOrderUI implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,contentPanel;
    MarketAPI towards;
    FleetTable table;
    FleetTable tableOfChosen;
    TemplateManufactureFilter filterPanel;
    CustomPanelAPI sectionOfFPData;
    float heightOfFilter = 150;
    float widthFirstSection = 700;
    public void executeConfirmSection(){
        for (DropDownButton dropDownButton : tableOfChosen.dropDownButtons) {
            if(dropDownButton.mainButton instanceof FleetButtonComponent component){
                BasePatrolFleet fleet = new BasePatrolFleet(component.getData());
                fleet.setTiedTo(towards);
                fleet.setPatrolType(AoTDFactionPatrolsManager.getInstance().getTypeBasedOnFP(fleet.geTotalFpTaken()));
                fleet.setDontUseFactionPrefix(false);
                fleet.setFleetName(component.getData().getNameOfFleet());
                AoTDFactionPatrolsManager.getInstance().addNewFleet(fleet);

            }
        }
        OverviewPatrolPanel.forceRequestUpdate = true;
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }
    public MassDeploymentOrderUI(float width,float height,MarketAPI deployTo){
        this.towards = deployTo;
        mainPanel = Global.getSettings().createCustom(width,height,this);
        createUI();
    }
    @Override
    public void createUI() {
        if(contentPanel!=null) {
            mainPanel.removeComponent(contentPanel);
        }
        contentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);

        TooltipMakerAPI tlFirst = contentPanel.createUIElement(widthFirstSection,20,false);
        float widthSecondSection = contentPanel.getPosition().getWidth()-widthFirstSection-5;
        TooltipMakerAPI tlSecond = contentPanel.createUIElement(widthSecondSection,20,false);


        tlFirst.addSectionHeading("Available templates", Alignment.MID,0f);
        tlSecond.addSectionHeading("Chosen templates", Alignment.MID,0f);

        float availHeight = (contentPanel.getPosition().getHeight()-55)-heightOfFilter;
        if(table==null){

            table = new FleetTable(widthFirstSection,availHeight,true,0,0){
                @Override
                public void reportButtonPressed(CustomButton buttonPressed) {
                    if(buttonPressed instanceof FleetButtonComponent fleet){
                        BasePatrolFleet fleet1 = new BasePatrolFleet(fleet.getData());
                        fleet1.setFleetName(fleet.getData().getNameOfFleet());
                        tableOfChosen.addToTable(fleet1);
                        recreateSectionFPData();
                    }
                }
            };
            table.createSections();
            table.recreateTable();
        }
        if(tableOfChosen==null){

            tableOfChosen = new FleetTable(widthSecondSection,availHeight,true,0,0,new ArrayList<>()){
                @Override
                public void reportButtonPressed(CustomButton buttonPressed) {
                    if(buttonPressed instanceof FleetButtonComponent fleet){
                        this.removeFromTable(fleet.getData());
                        recreateSectionFPData();
                    }
                }
            };
            tableOfChosen.createSections();
            tableOfChosen.recreateTable();
        }
        if(filterPanel==null){

            filterPanel = new TemplateManufactureFilter(widthFirstSection,heightOfFilter){
                @Override
                public void pruneMap(LinkedHashMap<String, Integer> map) {
                    if (map == null || map.isEmpty()) return;

                    LinkedHashSet<String> manusPresent = new LinkedHashSet<>();
                    for (BasePatrolFleetTemplate tmpl : PatrolTemplateManager.getTemplatesAvailable().values()) {
                        Collection<String> m = tmpl.getManufactures();
                        if (m != null) manusPresent.addAll(m);
                    }

                    // Drop all keys not present in manusPresent
                    map.keySet().removeIf(k -> !manusPresent.contains(k)&&!k.equals("All designs"));
                }

                @Override
                public void onChange() {
                    table.recreateTable();

                }
            };
            table.manuFilters = filterPanel.chosenManu ;
            filterPanel.createUI();
        }

        contentPanel.addUIElement(tlFirst).inTL(0,0);
        contentPanel.addUIElement(tlSecond).inTL(contentPanel.getPosition().getWidth()-widthSecondSection,0);
        contentPanel.addComponent(table.mainPanel).inTL(-2,20);
        contentPanel.addComponent(tableOfChosen.mainPanel).inTL(contentPanel.getPosition().getWidth()-widthSecondSection-4,20);
        contentPanel.addComponent(filterPanel.getMainPanel()).inTL(0,contentPanel.getPosition().getHeight()-heightOfFilter-10);
        recreateSectionFPData();
        mainPanel.addComponent(contentPanel).inTL(0,0);
    }

    public void recreateSectionFPData(){
        float widthSecondSection = contentPanel.getPosition().getWidth()-widthFirstSection-5;
        if(sectionOfFPData!=null){
            contentPanel.removeComponent(sectionOfFPData);
        }
        sectionOfFPData = Global.getSettings().createCustom(widthSecondSection,heightOfFilter,null);
        TooltipMakerAPI tl = sectionOfFPData.createUIElement(widthSecondSection,heightOfFilter,false);
        Color[] arr = new Color[2];
        int am = 0;
        for (DropDownButton dropDownButton : tableOfChosen.dropDownButtons) {
            CustomButton button = dropDownButton.mainButton;
            if(button instanceof FleetButtonComponent fleet){
                am+=fleet.getData().geTotalFpTaken();
            }
        }
        int aval = AoTDFactionPatrolsManager.getInstance().getAvailableFP();
        arr[0] = Misc.getNegativeHighlightColor();
        arr[1] = Color.ORANGE;
        if(am<=aval)arr[0] = Misc.getTextColor();
        tl.setParaFont(Fonts.ORBITRON_20AA);
        tl.addPara("Projected FP Cost of Deployment",0f).setAlignment(Alignment.MID);
        tl.addPara("%s / %s",5f, arr,""+am,""+ aval).setAlignment(Alignment.MID);
        sectionOfFPData.addUIElement(tl).inTL(0,0);
        contentPanel.addComponent(sectionOfFPData).inTL(contentPanel.getPosition().getWidth()-widthSecondSection,contentPanel.getPosition().getHeight()-heightOfFilter-10);


    }
    public boolean shouldEnableConfirm(){
        if(tableOfChosen!=null){
            int am = 0;
            for (DropDownButton dropDownButton : tableOfChosen.dropDownButtons) {
                CustomButton button = dropDownButton.mainButton;
                if(button instanceof FleetButtonComponent fleet){
                    am+=fleet.getData().geTotalFpTaken();
                }
            }
            return am<= AoTDFactionPatrolsManager.getInstance().getAvailableFP();
        }
        else{
            return false;
        }

    }
    @Override
    public void clearUI() {
        table.clearUI();
        tableOfChosen.clearUI();
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

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
