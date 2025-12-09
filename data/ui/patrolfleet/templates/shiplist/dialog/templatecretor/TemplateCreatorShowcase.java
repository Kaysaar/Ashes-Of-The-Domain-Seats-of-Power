package data.ui.patrolfleet.templates.shiplist.dialog.templatecretor;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
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
    public ButtonAPI recon,home,star,enabledPrefix;
    boolean patrolFleetCreatorMode = false;
    FleetFactory.PatrolType type;
    boolean fleetPrefixNameDissabled;

    public boolean isFleetPrefixNameDissabled() {
        return fleetPrefixNameDissabled;
    }

    String prevText = "";
    public TextFieldAPI getTextForName() {
        return textForName;
    }

    public void setExistingTemplate(BasePatrolFleetTemplate existingTemplate) {
        this.existingTemplate = existingTemplate;
        if(existingTemplate instanceof BasePatrolFleet fleet){
            type = fleet.getPatrolType();
            fleetPrefixNameDissabled = fleet.isDontUseFactionPrefix();
        }
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
        float height  =  componentPanel.getPosition().getHeight() - headerPanel.getPosition().getHeight()-105;
        if(patrolFleetCreatorMode){
            height = componentPanel.getPosition().getHeight() - headerPanel.getPosition().getHeight()-205;
        }
        list  = new TemplateShipList(componentPanel.getPosition().getWidth(), height,existingTemplate,true);
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
        textForName.setMaxChars(25);
        if(existingTemplate!=null){
            textForName.setText(existingTemplate.getNameOfTemplate());
        }
        if(existingTemplate instanceof BasePatrolFleet fleet){
            textForName.setText(fleet.getNameOfFleet());
        }
        headerPanel.addUIElement(tooltip).inTL(0,0);
        componentPanel.addComponent(headerPanel).inTL(0,0);

    }

    public void createInfo(){
        if(dataAboutFleetPanel!=null) {
            componentPanel.removeComponent(dataAboutFleetPanel);
        }
        float height = 100;
        if(patrolFleetCreatorMode){
            height = 210;
        }
        dataAboutFleetPanel = Global.getSettings().createCustom(componentPanel.getPosition().getWidth(),height,null);
        TooltipMakerAPI tooltip = dataAboutFleetPanel.createUIElement(dataAboutFleetPanel.getPosition().getWidth(),dataAboutFleetPanel.getPosition().getHeight(),false);

        if(patrolFleetCreatorMode){
            enabledPrefix =  tooltip.addCheckbox(componentPanel.getPosition().getWidth(),20,"Disable Faction Prefix",null, ButtonAPI.UICheckboxSize.SMALL,0f);
            tooltip.addPara("On the campaign map, this fleet appears as",3f).setAlignment(Alignment.MID);
            int available =  AoTDFactionPatrolsManager.getInstance().getAvailableFP();
            enabledPrefix.setChecked(fleetPrefixNameDissabled);
            if(existingTemplate instanceof BasePatrolFleet fleet){
                available+=fleet.geTotalFpTaken();

            }
            if(enabledPrefix.isChecked()){
                tooltip.addPara("%s",1f,Color.ORANGE,getTextForName().getText()).setAlignment(Alignment.MID);

            }
            else{
                tooltip.addPara("%s",1f,Color.ORANGE,Global.getSector().getPlayerFaction().getDisplayName()+" "+getTextForName().getText()).setAlignment(Alignment.MID);

            }
            tooltip.addSectionHeading("Fleet data",Alignment.MID,5f);

            tooltip.addPara("We currently can afford maximum of %s fleet points !",3f,Color.ORANGE,""+available);
        }
        else{
            tooltip.addSectionHeading("Template data",Alignment.MID,0f);
        }
        tooltip.addPara("Fleet points used by combat vessels : %s",5f, Color.ORANGE,""+list.getFleetPoints(false));

        tooltip.addPara("Ships used : %s / %s",5f, Color.ORANGE,""+list.getCountOfShips(),""+TemplateUtilis.numOfShipsPerFleet);
        if(patrolFleetCreatorMode){
            tooltip.addPara("Current designated role : %s",3f,Color.ORANGE,BasePatrolFleet.getRole(type)).setAlignment(Alignment.MID);
            float y = tooltip.getHeightSoFar()+10;
            float rest = height-y;
            float heightButtons = 30;
            float widthOfButton = (dataAboutFleetPanel.getPosition().getWidth()/3-15);
            recon = tooltip.addButton("Recon","recon", Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID,CutStyle.NONE,widthOfButton,heightButtons,0f);
            home = tooltip.addButton("Home-guard","recon", Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID,CutStyle.NONE,widthOfButton,heightButtons,0f);
            star = tooltip.addButton("System Defence","recon", Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID,CutStyle.NONE,widthOfButton,heightButtons,0f);
            star.getPosition().inTL(dataAboutFleetPanel.getPosition().getWidth()-widthOfButton,y);
            home.getPosition().inTL(dataAboutFleetPanel.getPosition().getWidth()/2-(widthOfButton/2),y);
            recon.getPosition().inTL(0,y);
            LabelAPI l =tooltip.addPara("Roles are set only before a fleet begins patrol duty",3f);
            l.setAlignment(Alignment.MID);
            l.getPosition().inTL(5,y+heightButtons+3);
        }
        dataAboutFleetPanel.addUIElement(tooltip).inTL(0,0);
        componentPanel.addComponent(dataAboutFleetPanel).inTL(0,componentPanel.getPosition().getHeight()-height);
    }


    @Override
    public void clearUI() {
        getList().clearUI();
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
        if(textForName!=null){
            if(!textForName.getText().equals(prevText)){
                prevText = textForName.getText();
                createInfo();
            }
        }
        if(enabledPrefix!=null){
            if(enabledPrefix.isChecked()!=fleetPrefixNameDissabled){
               fleetPrefixNameDissabled = enabledPrefix.isChecked();
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
