package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.BasePopUpDialog;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.ui.patrolfleet.templates.TemplatePanel;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.util.LinkedHashMap;
import java.util.Map;

public class SaveAsTemplateDialog extends BasePopUpDialog {
    TemplateCreatorDialog templateCreatorDialog;
    TextFieldAPI fieldAPI;
    LabelAPI warningLabel;
    CustomPanelAPI contentPanel,contentContainer;
    String prevText = "";
    boolean find = false;
    public SaveAsTemplateDialog(TemplateCreatorDialog dialog) {
        super("Save as Template");
        templateCreatorDialog = dialog;
    }

    @Override
    public void createContentForDialog(TooltipMakerAPI tooltip, float width) {
        tooltip.setParaFont(Fonts.ORBITRON_20AABOLD);
        tooltip.addSpacer(0f).getPosition().inTL(3f,0);
        tooltip.addPara("Save as Template", Misc.getTooltipTitleAndLightHighlightColor(),0f).setAlignment(Alignment.MID);

        fieldAPI = tooltip.addTextField(390,Fonts.ORBITRON_20AA,5f);
        fieldAPI.getPosition().inTL(width/2-(195),25);
        fieldAPI.setText(templateCreatorDialog.getShowcase().getTextForName().getText());
        tooltip.addSpacer(0f).getPosition().inTL(3f,45);
        contentContainer = Global.getSettings().createCustom(width,100,null);
        tooltip.addCustom(contentContainer,15f);
        fieldAPI.setMaxChars(25);
        update();
        tooltip.setHeightSoFar(0f);
    }
    public void update(){
        if(contentPanel!=null){
            contentContainer.removeComponent(contentPanel);
        }
        contentPanel= Global.getSettings().createCustom(contentContainer.getPosition().getWidth(),contentContainer.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = contentPanel.createUIElement(contentPanel.getPosition().getWidth(),contentPanel.getPosition().getHeight(),false);
        if(PatrolTemplateManager.getTemplatesAvailableSorted().get(prevText)!=null){
            tooltip.addPara("Warning! This name is used by existing template. If you confirm, this will override it's data!",Misc.getNegativeHighlightColor(),0f).setAlignment(Alignment.MID);

        }
        else{
            tooltip.addPara("This name is not used by any template",Misc.getPositiveHighlightColor(),0f).setAlignment(Alignment.MID);

        }

        contentPanel.addUIElement(tooltip).inTL(-5,0);
        contentContainer.addComponent(contentPanel).inTL(0,0);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if(fieldAPI!=null){
            if(!prevText.equals(fieldAPI.getText())){
                prevText = fieldAPI.getText();
                update();
            }
        }

    }

    @Override
    public void applyConfirmScript() {
        BasePatrolFleetTemplate template = new BasePatrolFleetTemplate(new LinkedHashMap<>(templateCreatorDialog.getShowcase().getList().getShips()),prevText);
        if(PatrolTemplateManager.templates.get(prevText)!=null){
            int index = 0;
            for (Map.Entry<String, BasePatrolFleetTemplate> entry : PatrolTemplateManager.templates.entrySet()) {
                if (entry.getKey().equals(prevText)) {
                    break;
                }
                index++;
            }
            AshMisc.replaceEntryAtIndex(PatrolTemplateManager.templates, index,prevText, template);
        }
        else{
            PatrolTemplateManager.templates.put(prevText,template);
        }

        PatrolTemplateManager.saveAllExistingTemplates();
        TemplatePanel.forceRequestUpdate = true;
    }
}
