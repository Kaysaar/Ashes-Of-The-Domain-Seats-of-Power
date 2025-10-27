package data.ui.patrolfleet.overview.components;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

public class EntityWithNameComponent implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel, componentPanel;
    SectorEntityToken token;


    public EntityWithNameComponent(SectorEntityToken token, float width, float height) {
        this.token = token;
        mainPanel = Global.getSettings().createCustom(width, height, this);


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
        componentPanel= Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),false);
        float width = componentPanel.getPosition().getWidth();
        float height = componentPanel.getPosition().getHeight();
        float boxSize = Math.min(width, height);
        EntityRenderer renderer = new EntityRenderer(token, boxSize);
        float startX = (width/2)-(boxSize/2);
        float startY = 0;
        Color color  = Misc.getTooltipTitleAndLightHighlightColor();
        if(token instanceof PlanetAPI planet){
            color = planet.getSpec().getIconColor();
        }
        if(token.getFaction()!=null&&!token.getFaction().getId().equals(Factions.NEUTRAL)){
            color = token.getFaction().getBaseUIColor();
        }

        tooltip.addPara(token.getName(),color,boxSize-2).setAlignment(Alignment.MID);
        tooltip.addCustom(renderer.getMainPanel(),0f).getPosition().inTL(startX,startY);
        componentPanel.addUIElement(tooltip).inTL(0,0);
        mainPanel.addComponent(componentPanel).inTL(0,0);

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
