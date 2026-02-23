package data.ui.patrolfleet.templates;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import data.ui.patrolfleet.templates.filter.TemplateFilterPanel;
import data.ui.patrolfleet.templates.shiplist.dialog.templatecretor.TemplateCreatorDialog;

import java.util.List;

public class TemplatePanel implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,componentPanel;
    TemplateFilterPanel filterPanel;
    TemplateShowcaseList list;
    public static boolean forceRequestUpdate= false;
    public static boolean forceRequestUpdateListOnly= false;
    public TemplateFilterPanel getFilterPanel() {
        return filterPanel;
    }

    public TemplateShowcaseList getTemplateShowcaseList() {
        return list;
    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public TemplatePanel (float width, float height) {
        mainPanel = Global.getSettings().createCustom(width,height,this);
        createUI();

    }

    @Override
    public void createUI() {
        if(componentPanel!=null) {
            mainPanel.removeComponent(componentPanel);
        }
        componentPanel= Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        filterPanel = new TemplateFilterPanel(335,mainPanel.getPosition().getHeight());
        filterPanel.createUI();
        filterPanel.advance(0f);
        float effectiveWidth = mainPanel.getPosition().getWidth()-filterPanel.getMainPanel().getPosition().getWidth()-5;
        componentPanel.addComponent(filterPanel.getMainPanel()).inTL(0,0);
        list = new TemplateShowcaseList(effectiveWidth,mainPanel.getPosition().getHeight());
        list.createUIImpl(filterPanel.getFilteredTemplates());
        forceRequestUpdateListOnly = false;
        forceRequestUpdate = false;
        componentPanel.addComponent(list.getMainPanel()).inTL(filterPanel.getMainPanel().getPosition().getWidth()+5,0);
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
        if(filterPanel!=null) {
            if(filterPanel.createNew!=null&&filterPanel.createNew.isChecked()){
                filterPanel.createNew.setChecked(false);
                float height = 600;
                AshMisc.initPopUpDialog(new TemplateCreatorDialog("Create new Patrol Fleet Template",this),TemplateCreatorDialog.width,height);
            }
            if(forceRequestUpdate){
                forceRequestUpdate= false;
                filterPanel.createUI();
                list.createUIImpl(filterPanel.getFilteredTemplates());

            }
            if(forceRequestUpdateListOnly){
                forceRequestUpdateListOnly =false;
                list.createUIImpl(filterPanel.getFilteredTemplates());

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
