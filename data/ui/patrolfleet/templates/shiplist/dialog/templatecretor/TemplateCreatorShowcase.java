package data.ui.patrolfleet.templates.shiplist.dialog.templatecretor;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import data.scripts.patrolfleet.managers.FactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.utilis.TemplateUtilis;
import data.ui.patrolfleet.templates.components.TemplateShipList;

import java.awt.*;
import java.util.List;

public class TemplateCreatorShowcase implements ExtendedUIPanelPlugin {

    BasePatrolFleetTemplate existingTemplate;
    CustomPanelAPI mainPanel;
    CustomPanelAPI componentPanel;
    TemplateShipList list;
    CustomPanelAPI headerPanel;
    TextFieldAPI textForName;
    CustomPanelAPI dataAboutFleetPanel;
    boolean patrolFleetCreatorMode = false;

    public TextFieldAPI getTextForName() {
        return textForName;
    }

    public void setExistingTemplate(BasePatrolFleetTemplate existingTemplate) {
        this.existingTemplate = existingTemplate;
    }

    public TemplateShipList getList() {
        return list;
    }

    public BasePatrolFleetTemplate getExistingTemplate() {
        return existingTemplate;
    }

    public TemplateCreatorShowcase(float width, float height,boolean patrolFleetCreatorMode) {
        mainPanel = Global.getSettings().createCustom(width,height,this);
        this.patrolFleetCreatorMode = patrolFleetCreatorMode;

    }
    public void addShip(String id){
        list.addNewShip(id);
    }
    public void addShip(String id,int number){
        list.addNewShip(id);
    }
    public TemplateCreatorShowcase(float width, float height, BasePatrolFleetTemplate existingTemplate) {
        mainPanel = Global.getSettings().createCustom(width,height,this);
        this.existingTemplate = existingTemplate;

    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        if(componentPanel!=null) {
            mainPanel.removeComponent(componentPanel);
        }
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        updateHeader();
        list  = new TemplateShipList(componentPanel.getPosition().getWidth(), componentPanel.getPosition().getHeight() - headerPanel.getPosition().getHeight()-105,existingTemplate,true);
        list.createUI();
        componentPanel.addComponent(list.getMainPanel()).inTL(0,50);
        createInfo();
        mainPanel.addComponent(componentPanel).inTL(0,0);



    }

    public void updateHeader(){
        if(headerPanel!=null) {
            componentPanel.removeComponent(headerPanel);
        }
        headerPanel = Global.getSettings().createCustom(componentPanel.getPosition().getWidth(),50,null);
        TooltipMakerAPI tooltip = headerPanel.createUIElement(headerPanel.getPosition().getWidth(),headerPanel.getPosition().getHeight(),false);
        tooltip.setTitleOrbitronLarge();
        if(patrolFleetCreatorMode){
            tooltip.addTitle("Fleet Name");
        }
        else{
            tooltip.addTitle("Template Name");
        }

        textForName = tooltip.addTextField(headerPanel.getPosition().getWidth()-5,25, Fonts.ORBITRON_20AA,3f);
        if(existingTemplate!=null){
            textForName.setText(existingTemplate.getNameOfTemplate());
        }
        headerPanel.addUIElement(tooltip).inTL(0,0);
        componentPanel.addComponent(headerPanel).inTL(0,0);

    }

    public void createInfo(){
        if(dataAboutFleetPanel!=null) {
            componentPanel.removeComponent(dataAboutFleetPanel);
        }
        dataAboutFleetPanel = Global.getSettings().createCustom(componentPanel.getPosition().getWidth(),100,null);
        TooltipMakerAPI tooltip = dataAboutFleetPanel.createUIElement(dataAboutFleetPanel.getPosition().getWidth(),dataAboutFleetPanel.getPosition().getHeight(),false);

        if(patrolFleetCreatorMode){
            tooltip.addSectionHeading("Fleet data",Alignment.MID,0f);
            int available =  FactionPatrolsManager.getInstance().getAvailableFP();
            if(existingTemplate instanceof BasePatrolFleet fleet){
                available+=fleet.geTotalFpTaken();
            }
            tooltip.addPara("We currently can afford maximum of %s fleet points !",3f,Color.ORANGE,""+available);
        }
        else{
            tooltip.addSectionHeading("Template data",Alignment.MID,0f);
        }
        tooltip.addPara("Fleet points used by combat vessels : %s",5f, Color.ORANGE,""+list.getFleetPoints(false));

        tooltip.addPara("Ships used : %s / %s",5f, Color.ORANGE,""+list.getCountOfShips(),""+TemplateUtilis.numOfShipsPerFleet);
        dataAboutFleetPanel.addUIElement(tooltip).inTL(0,0);
        componentPanel.addComponent(dataAboutFleetPanel).inTL(0,componentPanel.getPosition().getHeight()-100);
    }


    @Override
    public void clearUI() {

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
        if(list!=null){
            if(list.needsToUpgradeInfo){
                list.needsToUpgradeInfo = false;
                createInfo();
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
