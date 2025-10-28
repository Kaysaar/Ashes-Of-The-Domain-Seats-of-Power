package data.ui.patrolfleet.overview.marketdata;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.FactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.ui.patrolfleet.overview.fleetview.DeleteFleetDialog;
import data.ui.patrolfleet.overview.fleetview.FleetButtonComponent;
import data.ui.patrolfleet.overview.fleetview.FleetOptions;
import data.ui.patrolfleet.overview.fleetview.fleetreloc.FleetRelocationDialog;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class FleetMarketData implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel;
    CustomPanelAPI componentPanel;
    MarketAPI market;
    ButtonAPI add;
    ArrayList<FleetButtonComponent>components = new ArrayList<>();
    public  FleetButtonComponent lastChecked;
    public boolean showEdit,showDelete,showReloc;
    public FleetMarketData(float width,float height) {
        mainPanel = Global.getSettings().createCustom(width,height,this);

    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        if(componentPanel!=null){
            mainPanel.removeComponent(componentPanel);
        }
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        clearUI();
        if(market!=null){
            TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),20,false);
            tooltip.addSectionHeading("Currently Assigned Fleets to this market", Alignment.MID,0f);
            componentPanel.addUIElement(tooltip).inTL(0,0);
            TooltipMakerAPI buttonT = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),30,false);
            buttonT.setButtonFontOrbitron20();
            add =buttonT.addButton("Add new fleet",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID, CutStyle.C2_MENU,300,30,0f);
            add.getPosition().inTL(componentPanel.getPosition().getWidth()-(add.getPosition().getWidth())-5,0);
            ButtonAPI bt = tooltip.addAreaCheckbox("Name",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Misc.getBrightPlayerColor(),230,20,0f);
            bt.getPosition().inTL(0,20);
            bt.setClickable(false);
            componentPanel.addUIElement(buttonT).inTL(0,componentPanel.getPosition().getHeight()-30);
            bt = tooltip.addAreaCheckbox("Fleet Composition",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Misc.getBrightPlayerColor(),mainPanel.getPosition().getWidth()-237-300,20,0f);
            bt.getPosition().inTL(231,20);
            bt.setClickable(false);

            bt = tooltip.addAreaCheckbox("Status",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Misc.getBrightPlayerColor(),200,20,0f);
            componentPanel.addUIElement(buttonT).inTL(0,componentPanel.getPosition().getHeight()-30);
            bt.setClickable(false);
            bt.getPosition().inTL(mainPanel.getPosition().getWidth()-5-300,20);

            bt = tooltip.addAreaCheckbox("FP",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Misc.getBrightPlayerColor(),99,20,0f);
            componentPanel.addUIElement(buttonT).inTL(0,componentPanel.getPosition().getHeight()-30);
            bt.setClickable(false);
            bt.getPosition().inTL(mainPanel.getPosition().getWidth()-104,20);

            TooltipMakerAPI content = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight()-75,true);

            content.addSpacer(0f).getPosition().inTL(0,0);
//            int i =1;
//            for (BasePatrolFleetTemplate value : PatrolTemplateManager.templates.values()) {
//                BasePatrolFleet fleet = new BasePatrolFleet(value);
//                fleet.setFleetName("Persean League Hater "+Global.getSettings().getRoman(i));
//                FleetButtonComponent test = new FleetButtonComponent(componentPanel.getPosition().getWidth()-5,60,fleet,false);
//                test.createUI();
//                content.addCustom(test.getMainPanel(),5f);
//                i++;
//            }
            for (BasePatrolFleet fleet : FactionPatrolsManager.getInstance().getAssignedFleetsForMarket(market)) {
                FleetButtonComponent test = new FleetButtonComponent(componentPanel.getPosition().getWidth()-5,60,fleet,false);
                test.createUI();
                content.addCustom(test.getMainPanel(),5f);
                components.add(test);
            }


            componentPanel.addUIElement(content).inTL(-5,40);

        }



        mainPanel.addComponent(componentPanel);

    }

    public void setMarket(MarketAPI market) {
        this.market = market;
        createUI();
    }

    @Override
    public void clearUI() {
        components.clear();
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
        if(add!=null&&add.isChecked()){
            add.setChecked(false);
            AshMisc.initPopUpDialog(new TemplateCreatorDialog("Create new Patrol",true,market),1200,600);

        }
        if(showEdit){
            showEdit = false;
            AshMisc.initPopUpDialog(new TemplateCreatorDialog("Edit Patrol",true,lastChecked.getData(),market),1200,600);
            lastChecked = null;

        }
        if(showDelete){
            showDelete = false;
            AshMisc.initPopUpDialog(new DeleteFleetDialog(lastChecked.getData()),800,205);
            lastChecked = null;

        }
        if(showReloc){
            showReloc = false;
            AshMisc.initPopUpDialog(new FleetRelocationDialog(lastChecked.getData()),700,400);
            lastChecked = null;

        }
        for (FleetButtonComponent component : components) {
            if(component.mainButton.isChecked()){
                component.setChecked(false);
                FleetOptions list = new FleetOptions(this);
                lastChecked = component;
                AshMisc.placePopUpUIInTL(list,component.mainButton,300,100,new Vector2f(-300,-60));
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
