package data.ui.patrolfleet.overview.fleetview.templatelist;

import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.templates.TemplatePanel;
import data.ui.patrolfleet.templates.filter.TemplateFilterPanel;
import data.ui.patrolfleet.templates.filter.TemplateManufactureFilter;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AvailableTemplateListPlugin implements ExtendedUIPanelPlugin {

    CustomPanelAPI mainPanel,contentPanel;
    CustomPanelAPI infoPanel;
    TemplateManufactureFilter filterPanel;
    FleetTable table;

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }
    public AvailableTemplateListPlugin(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width,height,this);

    }

    @Override
    public void createUI() {
        if(contentPanel!=null) {
            mainPanel.removeComponent(contentPanel);
        }
        contentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        float disposableHeightForFirstPart = contentPanel.getPosition().getHeight()-240;
        if(table==null){
            final AvailableTemplateListPlugin thisPlugin = this;

            table = new FleetTable(contentPanel.getPosition().getWidth(),disposableHeightForFirstPart,true,0,0){
                @Override
                public void reportButtonPressed(CustomButton buttonPressed) {
                    super.reportButtonPressed(buttonPressed);
                    thisPlugin.createUI();
                }
            };
            table.createSections();
            table.recreateTable();
        }
        ArrayList<String > validToHighlight = new ArrayList<>();

        if(filterPanel==null){

            filterPanel = new TemplateManufactureFilter(contentPanel.getPosition().getWidth(),120){
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
                    validToHighlight.removeIf(k->!manusPresent.contains(k));
                }

                @Override
                public void onChange() {
                    table.recreateTable();

                }
            };
            table.manuFilters = filterPanel.chosenManu ;
            filterPanel.createUI();
        }
        infoPanel = Global.getSettings().createCustom(contentPanel.getPosition().getWidth(),100,null);
        TooltipMakerAPI tooltip = infoPanel.createUIElement(infoPanel.getPosition().getWidth(),infoPanel.getPosition().getHeight(),false);
        tooltip.setParaFont(Fonts.ORBITRON_20AA);
        int points = AoTDFactionPatrolsManager.getInstance().getAvailableFP();
        tooltip.addPara("You have %s points to use!",3f, Color.ORANGE, ""+points).setAlignment(Alignment.MID);
        if(table.currFleet!=null){
            int fleetPoints = table.currFleet.getFPTaken();
            if(points>=fleetPoints){
                tooltip.addPara("This template is within range of available FP!", Misc.getPositiveHighlightColor(),5f).setAlignment(Alignment.MID);
            }
            else{
                tooltip.addPara("This template goes beyond range of available FP!",Misc.getNegativeHighlightColor(),5f).setAlignment(Alignment.MID);
            }
        }
        infoPanel.addUIElement(tooltip).inTL(0,0);
        contentPanel.addComponent(table.mainPanel).inTL(0,0);
        contentPanel.addComponent(filterPanel.getMainPanel()).inTL(5,disposableHeightForFirstPart+25);
        contentPanel.addComponent(infoPanel).inTL(0,disposableHeightForFirstPart+145);
        mainPanel.addComponent(contentPanel).inTL(0,0);

    }



    @Override
    public void clearUI() {
        table.clearUI();
        filterPanel.clearUI();
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
