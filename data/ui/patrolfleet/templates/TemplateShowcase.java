package data.ui.patrolfleet.templates;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.models.PatrolTemplateDataPackage;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.utilis.TemplateUtilis;
import data.ui.patrolfleet.templates.components.TemplateShipList;
import data.ui.patrolfleet.templates.shiplist.components.ShipPanelData;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateShowcase implements ExtendedUIPanelPlugin {
    BasePatrolFleetTemplate template;
    public CustomPanelAPI mainPanel,componentPanel;
    ButtonAPI copy,edit,delete;
    UILinesRenderer renderer;
    boolean init = false;
    TemplateShowcaseList list;

    public void setTemplate(BasePatrolFleetTemplate template) {
        this.template = template;
    }

    public BasePatrolFleetTemplate getTemplate() {
        return template;
    }


    public TemplateShowcase(BasePatrolFleetTemplate template, float width, float height, TemplateShowcaseList referenceList) {
        this.template = template;
        mainPanel = Global.getSettings().createCustom(width,height,this);
        renderer = new UILinesRenderer(0f);
        renderer.setPanel(mainPanel);
        this.list = referenceList;
        createUI();
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
        if(template!=null){
            if(!template.isTemplateValidForModList()){
                renderer.setBoxColor(Misc.getNegativeHighlightColor());
            } else if (!template.doesKnowAllShips()) {
                renderer.setBoxColor(Misc.getGrayColor());
            }
        }
        TemplateShipList list = new TemplateShipList(componentPanel.getPosition().getWidth()-210,140,template,false);
        list.createUI();

        float takenWidth = componentPanel.getPosition().getWidth()-210;
        float width = componentPanel.getPosition().getWidth()-210;
        TooltipMakerAPI tooltip = componentPanel.createUIElement(width,componentPanel.getPosition().getHeight(),false);

        tooltip.setTitleSmallOrbitron();
        tooltip.addTitle(template.nameOfTemplate);

        tooltip.addCustom(list.getMainPanel(),5f);
        if(!template.isTemplateValidForModList()){
            LabelAPI labelAPI = tooltip.addPara("Missing essential mods to use template properly!",Misc.getNegativeHighlightColor(),3f);
            labelAPI.setHighlightOnMouseover(true);
            tooltip.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return 500f;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addPara("This template requires those mods to be enabled first to use! If issue persists then it means some ships are no longer part of specific mod!",2f);
                    for (Map.Entry<String, String> str : template.modsReq.entrySet()) {
                        if(Global.getSettings().getModManager().isModEnabled(str.getKey())){
                            tooltip.addPara(str.getValue()+" ("+str.getKey()+")"+" : Enabled",Misc.getPositiveHighlightColor(),5f);
                        }
                        else{
                            tooltip.addPara(str.getValue()+" ("+str.getKey()+")"+" : Disabled",Misc.getNegativeHighlightColor(),5f);
                        }
                    }
                }
            }, (UIComponentAPI) labelAPI, TooltipMakerAPI.TooltipLocation.BELOW,false);
        } else if (!template.doesKnowAllShips()) {
            LabelAPI labelAPI = tooltip.addPara("Not all ships are known from template!",Misc.getTooltipTitleAndLightHighlightColor(),3f);
            labelAPI.setHighlightOnMouseover(true);
            tooltip.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return 500f;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addPara("This template requires ships, that your faction have no idea, how to build!",2f);
                    ArrayList<String>names = new ArrayList<>();
                    for (String string : template.assignedShipsThatShouldSpawn.keySet()) {
                        ShipHullSpecAPI specAPI = Global.getSettings().getHullSpec(string);
                        if(!ShipPanelData.learnedShips.contains(specAPI)){
                            names.add(specAPI.getHullName());
                        }
                    }
                    tooltip.addPara("Missing ships in question : %s",5f,Misc.getNegativeHighlightColor(),Misc.getAndJoined(names));
                }
            }, (UIComponentAPI) labelAPI, TooltipMakerAPI.TooltipLocation.BELOW,false);
        }

        float tWidth = componentPanel.getPosition().getWidth()-takenWidth-5;
        TooltipMakerAPI tooltip2 = componentPanel.createUIElement(componentPanel.getPosition().getWidth()-takenWidth-5,componentPanel.getPosition().getHeight(),false);

        tooltip2.addSectionHeading("Template Data", Alignment.MID,2f);
        tooltip2.addPara("Fleet points : %s",3f, Color.ORANGE,""+list.getFleetPoints(false));
        tooltip2.addPara("Ships used : %s / %s",3f, Color.ORANGE,""+list.getCountOfShips(),""+ TemplateUtilis.numOfShipsPerFleet);
        edit = tooltip2.addButton("Edit Template",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID,CutStyle.NONE,tWidth-15,30,5f);
        copy = tooltip2.addButton("Copy Template",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID,CutStyle.NONE,tWidth-15,30,5f);
        delete = tooltip2.addButton("Delete Template",null,Global.getSector().getFaction(Factions.PIRATES).getBaseUIColor(),Global.getSector().getFaction(Factions.PIRATES).getDarkUIColor(),Alignment.MID,CutStyle.NONE,tWidth-15,30,5f);


        componentPanel.addUIElement(tooltip).inTL(0,1);
        componentPanel.addUIElement(tooltip2).inTL(takenWidth+5,1);
        mainPanel.addComponent(componentPanel).inTL(0,0);
        init = true;

    }

    @Override
    public void clearUI() {

    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {
        renderer.render(alphaMult);
    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {
        if(init){
            if(delete.isChecked()){
                delete.setChecked(false);
                //Confirmation later;
                PatrolTemplateManager.templates.remove(template.getNameOfTemplate());
                PatrolTemplateManager.saveAllExistingTemplates();
                TemplatePanel.forceRequestUpdate = true;
                list.createUI();

                return;
            }
            if(edit.isChecked()){
                edit.setChecked(false);
                //Confirmation later;
                float height = 600;
                if(!AshMisc.isPLayerHavingHeavyIndustry()){
                    height = 630;
                }
                AshMisc.initPopUpDialog(new TemplateCreatorDialog("Edit Patrol Fleet Template",this),1200,height);
                return;
            }
            if(copy.isChecked()){
                copy.setChecked(false);
                Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                PatrolTemplateDataPackage pack = template.getPackage();
                if(pack!=null){
                    StringSelection selection  = new StringSelection(pack.toString());
                    cb.setContents(selection, null);
                }

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
