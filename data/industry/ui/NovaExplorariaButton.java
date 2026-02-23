package data.industry.ui;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.dialogs.PlanetSurveyChooseDialog;
import data.industry.NovaExploraria;
import data.misc.ProductionUtil;
import data.ui.overview.capitalbuilding.BaseCapitalButton;

import java.awt.*;

public class NovaExplorariaButton extends BaseCapitalButton {
    public NovaExploraria industry;
    public NovaExplorariaButton(float width, float height,NovaExploraria exploraria) {
        super(width, height);
        this.industry = exploraria;
    }
    public ButtonAPI surveyFleetButton,techHunterPatrol,abyssButton;


    @Override
    public void createUIImpl(CustomPanelAPI panel, TooltipMakerAPI tooltip) {
        if(industry!=null) {
            tooltip.setParaFont(Fonts.ORBITRON_16);
            button.setClickable(false);
            button.setHighlightBrightness(0f);
            button.setGlowBrightness(0f);
            button.setHighlightBounceDown(false);
            button.setMouseOverSound(null);
            tooltip.addPara(industry.getCurrentName(),0f).getPosition().inTL(5,5);
            ImageViewer viewer = new ImageViewer(190,95,industry.getSpec().getImageName());
            tooltip.addCustom(viewer.getComponentPanel(),5f).getPosition().inTL(5,25);

            float width = panel.getPosition().getWidth()-200;
            float effectiveWidth = width/2;
            TooltipMakerAPI tl = tooltip.beginSubTooltip(width);
            tl.setParaFont(Fonts.ORBITRON_16);
            tl.addPara("Available Exploraria Fleets",0f).setAlignment(Alignment.MID);
            LabelAPI label = tl.addPara("%s / %s",5f, Color.ORANGE,industry.getRemainingOnes()+"",industry.getMaxAmountOfExpeditions().getModifiedInt()+"");
            label.setAlignment(Alignment.MID);
            float y = tl.getHeightSoFar()+10;
            surveyFleetButton= tl.addButton("Sent Survey Fleet",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID, CutStyle.NONE,220,25,5f);
            surveyFleetButton.getPosition().inTL(effectiveWidth-(surveyFleetButton.getPosition().getWidth()+10),y);
            surveyFleetButton.setEnabled(industry.canSentExpedition());
            techHunterPatrol = tl.addButton("Sent Tech Hunters",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID, CutStyle.NONE,220,25,5f);
            techHunterPatrol.getPosition().inTL(effectiveWidth+10,y);
            techHunterPatrol.setEnabled(industry.canSentExpedition()&&NovaExploraria.canDoInifniteTechmining());
            abyssButton= tl.addButton("Sent Abyss Delvers",null,Misc.getBasePlayerColor(),new Color(120, 80, 200) ,Alignment.MID, CutStyle.NONE,220,25,5f);
            abyssButton.getPosition().belowMid((UIComponentAPI) label,45);
            abyssButton.setEnabled(industry.canSentExpedition()&&NovaExploraria.canDoAbyssDiving());

            tooltip.endSubTooltip();

            tooltip.addCustom(tl,0f).getPosition().inTL(200,5);
        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if(surveyFleetButton.isChecked()){
            surveyFleetButton.setChecked(false);
            Global.getSector().getCampaignUI().showInteractionDialogFromCargo(new PlanetSurveyChooseDialog(), null, this::createUI);
        }
        if(abyssButton.isChecked()){
            abyssButton.setChecked(false);
            BasePopUpDialog dialog = new AbyssExpeditionDialog("Abyss Expedition",1,12,1,1,this);
            AshMisc.initPopUpDialog(dialog,700,330);

        }
        if(techHunterPatrol.isChecked()){
            techHunterPatrol.setChecked(false);
            BasePopUpDialog dialog = new TechHunterDialog("Tech Hunters Expedition",1,12,1,1,this);

            AshMisc.initPopUpDialog(dialog,700,330);

        }
    }
}
