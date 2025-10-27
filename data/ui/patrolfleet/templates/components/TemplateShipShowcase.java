package data.ui.patrolfleet.templates.components;

import ashlib.data.plugins.info.ShipInfoGenerator;
import ashlib.data.plugins.ui.models.resizable.ButtonComponent;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.templates.shiplist.components.ShipPanelData;
import data.ui.patrolfleet.templates.shiplist.components.ShipUIData;

import java.awt.*;

public class TemplateShipShowcase extends ButtonComponent {
    String shipId;
    TemplateShipList list;
    CustomPanelAPI shipPanel;
    CustomPanelAPI tooltipPanel;
    boolean removable = false;
    BasePatrolFleetTemplate template;

    public TemplateShipList getList() {
        return list;
    }

    public TemplateShipShowcase(float boxSize, boolean shouldRenderBorders, String shipId) {
        super(boxSize, boxSize);
        this.shouldRenderBorders = shouldRenderBorders;
        this.shipId = shipId;
        if(shipId!=null) {
            shipPanel =  ShipInfoGenerator.getShipImage(Global.getSettings().getHullSpec(shipId),boxSize-10,null).one;
            componentPanel.addComponent(shipPanel).inTL(originalWidth/2-(shipPanel.getPosition().getWidth()/2)+1,5);
        }
    }
    public TemplateShipShowcase(float boxSize, boolean shouldRenderBorders, String shipId,TemplateShipList list) {
        super(boxSize, boxSize);
        this.shipId = shipId;
        this.list = list;
        if(shipId!=null) {
            Color col = null;
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(shipId);
            if(!ShipPanelData.learnedShips.contains(spec)) {
                col = new Color(54, 54, 54);
            }
            shipPanel =  ShipInfoGenerator.getShipImage(spec,boxSize-10,col).one;
            componentPanel.addComponent(shipPanel).inTL(originalWidth/2-(shipPanel.getPosition().getWidth()/2)+1,5);
            ShipUIData.createTooltipForShip(Global.getSettings().getHullSpec(shipId),shipPanel);
        }
    }
    public TemplateShipShowcase(float boxSize, boolean shouldRenderBorders, String shipId,TemplateShipList list,boolean removable) {
        super(boxSize, boxSize);
        this.shouldRenderBorders = shouldRenderBorders;
        this.shipId = shipId;
        this.list = list;
        this.removable = removable;
        if(shipId!=null) {
            Color col = null;
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(shipId);
            if(!ShipPanelData.learnedShips.contains(spec)) {
                col = new Color(54, 54, 54);
            }
            shipPanel =  ShipInfoGenerator.getShipImage(spec,boxSize-10,col).one;

            componentPanel.addComponent(shipPanel).inTL(originalWidth/2-(shipPanel.getPosition().getWidth()/2)+1,5);
            ShipUIData.createTooltipForShip(Global.getSettings().getHullSpec(shipId),shipPanel);
        }
    }
    public TemplateShipShowcase(float boxSize, boolean shouldRenderBorders, String shipId,TemplateShipList list,int am) {
        super(boxSize, boxSize);
        this.shouldRenderBorders = shouldRenderBorders;
        this.shipId = shipId;
        this.list = list;
        if(shipId!=null) {
            Color col = null;
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(shipId);
            if(!ShipPanelData.learnedShips.contains(spec)) {
                col = new Color(54, 54, 54);
            }
            shipPanel =  ShipInfoGenerator.getShipImage(spec,boxSize-10,col).one;
            tooltipPanel = Global.getSettings().createCustom(boxSize,boxSize,null);

            TooltipMakerAPI tooltip = tooltipPanel.createUIElement(boxSize+2,boxSize,false);
            tooltip.addPara(""+am,boxSize-12).setAlignment(Alignment.BR);
            componentPanel.addComponent(shipPanel).inTL(originalWidth/2-(shipPanel.getPosition().getWidth()/2)+1,5);
            tooltipPanel.addUIElement(tooltip).inTL(0,0);
            componentPanel.addComponent(tooltipPanel).inTL(0,0);
            ShipUIData.createTooltipForShip(Global.getSettings().getHullSpec(shipId),shipPanel);
        }
    }

    public void overrideToBeShortCutForFleetView(BasePatrolFleetTemplate template){
        componentPanel.removeComponent(tooltipPanel);
        componentPanel.removeComponent(shipPanel);
        tooltipPanel = Global.getSettings().createCustom(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),null);

        TooltipMakerAPI tooltip = tooltipPanel.createUIElement(tooltipPanel.getPosition().getWidth(),tooltipPanel.getPosition().getHeight(),false);
        tooltip.addPara("[...]",tooltipPanel.getPosition().getWidth()/2-5).setAlignment(Alignment.MID);
        this.template = template;
        tooltipPanel.addUIElement(tooltip).inTL(0,0);
        componentPanel.addComponent(tooltipPanel).inTL(0,0);
    }
    @Override
    public void performActionOnClick(boolean isRightClick) {
        if(removable&&list!=null){
            list.removeShip(shipId);
        }

    }
}
