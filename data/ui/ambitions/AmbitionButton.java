package data.ui.ambitions;

import ashlib.data.plugins.ui.models.resizable.ButtonComponent;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ambition.AmbitionSpec;

public class AmbitionButton extends ButtonComponent {
    CustomPanelAPI content;
    ImageViewer imageViewer;
    AmbitionSpec ambitionSpec;
    public AmbitionButton(float width, float height, AmbitionSpec spec) {
        super(width, height);
        this.ambitionSpec = spec;
        createUI();
    }


    public AmbitionSpec getAmbitionSpec() {
        return ambitionSpec;
    }

    public void createUI(){

        CustomPanelAPI mainPanel = getComponentPanel();
        if(imageViewer==null){
            imageViewer = new ImageViewer(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(), Global.getSettings().getSpriteName("illustrations",ambitionSpec.getBannerId()));
            addComponent(imageViewer,0,0);
        }

        if(content!=null){
            mainPanel.removeComponent(content);
        }
        content = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = content.createUIElement(content.getPosition().getWidth(),content.getPosition().getHeight(),false);
        tooltip.setParaFont(Fonts.ORBITRON_24AA);
        tooltip.addPara(ambitionSpec.name, Misc.getTooltipTitleAndLightHighlightColor(),1f).setAlignment(Alignment.MID);
        content.addUIElement(tooltip).inTL(0,0);
        mainPanel.addComponent(content).inTL(0,0);
    }

}
