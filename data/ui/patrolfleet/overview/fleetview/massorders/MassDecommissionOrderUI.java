package data.ui.patrolfleet.overview.fleetview.massorders;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.ui.patrolfleet.overview.fleetview.FleetButtonComponent;
import data.ui.patrolfleet.overview.fleetview.templatelist.FleetButtonDrop;
import data.ui.patrolfleet.overview.fleetview.templatelist.FleetTable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MassDecommissionOrderUI implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,contentPanel;
    FleetTable tableOfFleets;
    MarketAPI currMarket;
    ArrayList<BasePatrolFleet>fleetsToDecom = new ArrayList<>();
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }
    public void executeConfirmSection(){

    }
    public MassDecommissionOrderUI(float width, float height,MarketAPI decomMarket){
        this.currMarket = decomMarket;
        mainPanel = Global.getSettings().createCustom(width,height,this);
        final  MassDecommissionOrderUI fi = this;
        tableOfFleets = new FleetTable(mainPanel.getPosition().getWidth()-15,mainPanel.getPosition().getHeight()-180,true,0,0, AoTDFactionPatrolsManager.getInstance().getAssignedFleetsForMarket(currMarket)){
            @Override
            public void reportButtonPressed(CustomButton buttonPressed) {
                if(buttonPressed instanceof FleetButtonComponent component){
                    if(fleetsToDecom.contains(component.getData())){
                        fleetsToDecom.remove(component.getData());
                    }
                    else{
                        fleetsToDecom.add(component.getData());
                    }
                    fi.createUI();

                }
            }

            @Override
            public void advance(float amount) {
                super.advance(amount);
                dropDownButtons.forEach(x->{
                    if(x instanceof FleetButtonDrop bt){
                        if(fleetsToDecom.contains(bt.fleet)){
                            x.mainButton.mainButton.highlight();
                        }
                        else{
                            x.mainButton.mainButton.unhighlight();
                        }
                    }
                });
            }
        };
        tableOfFleets.createSections();
        tableOfFleets.createTable();
        mainPanel.addComponent(tableOfFleets.mainPanel).inTL(0,0);
        createUI();
    }

    @Override
    public void createUI() {
        if(contentPanel!=null) {
            mainPanel.removeComponent(contentPanel);
        }
        contentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),150,null);
        TooltipMakerAPI tooltip = contentPanel.createUIElement(contentPanel.getPosition().getWidth(),contentPanel.getPosition().getHeight(),false);
        float mostDays = 0f;
        for (BasePatrolFleet fleet : fleetsToDecom) {
            if(AoTDFactionPatrolsManager.getInstance().getDaysToDecomFleet(fleet)>=mostDays){
                mostDays = AoTDFactionPatrolsManager.getInstance().getDaysToDecomFleet(fleet);
            }
        }
        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);
        String fleets  = "one fleet";
        if(!fleetsToDecom.isEmpty()){
            if(fleetsToDecom.size()>1){
                fleets = "multiple fleets";
            }
            tooltip.addPara("You are about to de-commission %s , which will leave %s, less defended.", 3f, Color.ORANGE, fleets,currMarket.getName()).setAlignment(Alignment.MID);;
            tooltip.addPara("It will take roughly %s for all fleets to de-commission. FP will be slowly refunded as de-commission progresses.", 5f,Color.ORANGE, AshMisc.convertDaysToString(Math.round(mostDays))).setAlignment(Alignment.MID);
            tooltip.addPara("This procedure can not be canceled! Do you want to proceed?", 10f).setAlignment(Alignment.MID);
        }
        contentPanel.addUIElement(tooltip).inTL(0,0);
        mainPanel.addComponent(contentPanel).inTL(0,mainPanel.getPosition().getHeight()-contentPanel.getPosition().getHeight());
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

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
