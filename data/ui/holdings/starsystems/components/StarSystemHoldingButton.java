package data.ui.holdings.starsystems.components;

import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.ui.patrolfleet.overview.components.EntityWithNameComponent;

import java.awt.*;

public class StarSystemHoldingButton extends CustomButton {

    public StarSystemHoldingButton(float width, float height, Object buttonData, float indent, Color base, Color bg, Color bright, boolean isWithArrow) {
        super(width, height, buttonData, indent, base, bg, bright);
        this.setWithArrow(isWithArrow);
    }

    @Override
    public void createButtonContent(TooltipMakerAPI tooltip) {
        CustomPanelAPI container = Global.getSettings().createCustom(this.width,this.height,null);
        createContainerContent(container);
        tooltip.addCustom(container,0f).getPosition().inTL(0,0);
        float centerY = height/2;
        if(isWithArrow){
            panelIndicator = Global.getSettings().createCustom(15,15,null);
            tooltip.addCustom(panelIndicator,0f).getPosition().inTL((float) StarSystemHoldingTable.widthMap.get("name")*0.75f,centerY-7);

        }
    }
    public void createContainerContent(CustomPanelAPI container) {

        if(buttonData instanceof StarSystemAPI system){
            EntityWithNameComponent component = new EntityWithNameComponent(system.getStar(),true,StarSystemHoldingTable.widthMap.get("name"),50);
            component.createUI();
            container.addComponent(component.getMainPanel()).inTL(-indent,8);

            float startingX = StarSystemHoldingTable.getStartingX("data")+5;
            float iconsize = 25;
            for (SectorEntityToken token : system.getEntitiesWithTag(Tags.OBJECTIVE)) {
                ImageViewer viewer = new ImageViewer(iconsize,iconsize,token.getCustomEntitySpec().getIconName());
                if(token.getFaction()!=null&&!token.getFaction().getId().equals(Factions.NEUTRAL)){
                    viewer.setColorOverlay(token.getFaction().getBaseUIColor());
                }
                container.addComponent(viewer.getComponentPanel()).inTL(startingX,height/2-(iconsize/2));
                startingX+=iconsize+5;
            }

            for (SectorEntityToken token : system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
                ImageViewer viewer = new ImageViewer(iconsize,iconsize,token.getCustomEntitySpec().getIconName());
                if(token.getFaction()!=null&&!token.getFaction().getId().equals(Factions.NEUTRAL)){
                    viewer.setColorOverlay(token.getFaction().getBaseUIColor());
                }
                container.addComponent(viewer.getComponentPanel()).inTL(startingX,height/2-(iconsize/2));
                startingX+=iconsize+5;
            }
             startingX = StarSystemHoldingTable.getStartingX("income")+10;
            float width = StarSystemHoldingTable.widthMap.get("income");
            TooltipMakerAPI tooltipIncome = container.createUIElement(width,height,false);
            int income = 0;
            for (MarketAPI marketAPI :Global.getSector().getEconomy().getMarkets(system)) {
                if(marketAPI.isPlayerOwned()||marketAPI.getFaction().isPlayerFaction()){
                    income+=Math.round(marketAPI.getNetIncome());
                }
            }
            Color c = Color.ORANGE;
            if(income<0){
                c = Misc.getNegativeHighlightColor();
            }
            if(income ==0){
                c = Misc.getGrayColor();
            }
            LabelAPI l = tooltipIncome.addPara(Misc.getDGSCredits(income),c,0f);
            l.getPosition().inTL(0,(height/2)-(l.computeTextHeight(l.getText())/2));
            l.setAlignment(Alignment.MID);
            container.addUIElement(tooltipIncome).inTL(startingX,0);

        } else if (buttonData instanceof MarketAPI market) {
            EntityWithNameComponent component = new EntityWithNameComponent(market.getPrimaryEntity(),StarSystemHoldingTable.widthMap.get("name"),50,true);
            component.createUI();
            container.addComponent(component.getMainPanel()).inTL(-indent,8);
            float width = StarSystemHoldingTable.widthMap.get("data");
            TooltipMakerAPI tooltipConditions = container.createUIElement((width/2)-5,height-5,false);
            TooltipMakerAPI tooltipItems = container.createUIElement((width/2)-5,height-5,false);
            if(market.getPrimaryEntity() instanceof PlanetAPI planet){
                tooltipConditions.addPara(planet.getSpec().getName(),planet.getSpec().getIconColor(),4f);
            }
            else{
                tooltipConditions.addPara(market.getPrimaryEntity().getName(),market.getTextColorForFactionOrPlanet(),4f);
            }
            MarketConditionWidget panel = new MarketConditionWidget((width / 2) - 5, height - 10, market);
            ItemWidget itemPanel = new ItemWidget((width/2)-5,height-10,market);
            tooltipConditions.addCustom(panel.getMainPanel(),4f);
            tooltipItems.addCustom(itemPanel.getMainPanel(),5f);
            float startingX = StarSystemHoldingTable.getStartingX("data");
            container.addUIElement(tooltipConditions).inTL(startingX,5);
            container.addUIElement(tooltipItems).inTL(startingX+((width / 2)),5);

            startingX = StarSystemHoldingTable.getStartingX("income")+10-indent;
             width = StarSystemHoldingTable.widthMap.get("income");
            TooltipMakerAPI tooltipIncome = container.createUIElement(width,height,false);
            int income = Math.round(market.getNetIncome());
            Color c = Color.ORANGE;
            if(income<0){
                c = Misc.getNegativeHighlightColor();
            }
            if(income ==0){
                c = Misc.getGrayColor();
            }
            LabelAPI l = tooltipIncome.addPara(Misc.getDGSCredits(income),c,0f);
            l.getPosition().inTL(0,(height/2)-(l.computeTextHeight(l.getText())/2));
            l.setAlignment(Alignment.MID);
            container.addUIElement(tooltipIncome).inTL(startingX,0);
        }

    }

}
