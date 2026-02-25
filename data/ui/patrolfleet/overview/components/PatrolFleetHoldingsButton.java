package data.ui.patrolfleet.overview.components;

import ashlib.data.plugins.ui.models.CustomButton;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;

import java.awt.*;

public class PatrolFleetHoldingsButton extends CustomButton {
    public Object data;
    public PatrolFleetHoldingsButton(float width, float height, Object buttonData, float indent, Color base, Color bg, Color bright, boolean isWithArrow) {
        super(width, height, buttonData, indent, base, bg, bright);
        this.setWithArrow(isWithArrow);
    }

    @Override
    public void createButtonContent(TooltipMakerAPI tooltip) {
        float widthOfSection = width;
        float nameSectionWidth = ((widthOfSection)/3);
        nameSectionWidth+=20;
        float restWidthToDistribute = widthOfSection - nameSectionWidth;
        float fPGeneratedAmount =restWidthToDistribute/2;
        float fPUsedAmount = restWidthToDistribute/2;
        float centerY = height/2;
        SectorEntityToken star=null;
        int takenV =0;
        int generatedV = 0;
        if(buttonData instanceof MarketAPI market){
            star = market.getPrimaryEntity();

            generatedV = (int) FleetPointUtilis.getFleetPointsGeneratedByMarket(market);
            takenV = (int) FleetPointUtilis.getFleetPointsConsumedByMarket(market);

        } else if (buttonData instanceof StarSystemAPI system) {
            star = system.getStar();
            if (star == null) star = system.getCenter();
            generatedV = (int) FleetPointUtilis.getFleetPointsGeneratedByStarSystem(system,Global.getSector().getPlayerFaction());
            takenV = (int) FleetPointUtilis.getFleetPointsTakenByStarSystem(system,Global.getSector().getPlayerFaction());
        }
        EntityWithNameComponent component = new EntityWithNameComponent(star,nameSectionWidth,height-25);
        component.createUI();
        tooltip.addCustom(component.getMainPanel(),0f).getPosition().inTL(-indent,8);
        LabelAPI taken = tooltip.addPara(""+takenV,0f);
        if(takenV>0){
            taken.setColor(Color.ORANGE);
        }

        LabelAPI generated = tooltip.addPara(""+generatedV,Misc.getPositiveHighlightColor(),0f);
        if(isWithArrow){
            panelIndicator = Global.getSettings().createCustom(15,15,null);
            tooltip.addCustom(panelIndicator,0f).getPosition().inTL(widthOfSection-30,centerY-7);

        }
        float currCenter = (float) (nameSectionWidth+(fPGeneratedAmount/2)-(Math.floor(indent/2)));
        taken.getPosition().inTL(currCenter-(taken.computeTextWidth(taken.getText())/2),centerY-(taken.computeTextHeight(taken.getText())/2));

        currCenter = (float) (nameSectionWidth+fPGeneratedAmount+(fPUsedAmount/2)-(Math.floor(indent/2)));
        generated.getPosition().inTL(currCenter-(generated.computeTextWidth(generated.getText())/2),centerY-(generated.computeTextHeight(generated.getText())/2));
    }
}
