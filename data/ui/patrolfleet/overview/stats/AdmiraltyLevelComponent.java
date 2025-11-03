package data.ui.patrolfleet.overview.stats;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.ui.patrolfleet.overview.OverviewPatrolPanel;

import java.util.ArrayList;
import java.util.List;

public class AdmiraltyLevelComponent implements ExtendedUIPanelPlugin {
    ArrayList<ButtonAPI> buttons;
    CustomPanelAPI mainPanel,componentPanel;
    public static float boxSize =30;
    public static float seperatorX = 5;
    ButtonAPI curr;
    public AdmiraltyLevelComponent(){
        mainPanel = Global.getSettings().createCustom(boxSize*AoTDFactionPatrolsManager.MAX_ADMIRALTY_LEV+(seperatorX*(AoTDFactionPatrolsManager.MAX_ADMIRALTY_LEV-1)),boxSize,this);
        buttons = new ArrayList<>();
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
        TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),false);
        buttons.clear();
        float currX = 0;

        int currPoints = (int) (AoTDFactionPatrolsManager.getInstance().getAvailableFP()+ AoTDFactionPatrolsManager.getInstance().getFpConsumedByAdmiralty());
        for (int i = 1; i <= AoTDFactionPatrolsManager.MAX_ADMIRALTY_LEV; i++) {
            Integer in = i;
            ButtonAPI button =tooltip.addAreaCheckbox(null,in, Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Misc.getBrightPlayerColor(),boxSize,boxSize,0f)      ;
            button.getPosition().inTL(currX,0);
            if(Global.getSector().getPlayerFaction().getDoctrine().getOfficerQuality()==i){
                curr = button;
            }
            if(currPoints<AoTDFactionPatrolsManager.levels.get(in)){
                button.setEnabled(false);
            }
            buttons.add(button);
            currX+=boxSize+seperatorX;
        }
        componentPanel.addUIElement(tooltip).inTL(0,0);
        mainPanel.addComponent(componentPanel).inTL(0,0);



    }

    @Override
    public void clearUI() {
        buttons.clear();
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
        if(curr!=null){
            for (ButtonAPI button : buttons) {
                if(button.getCustomData() instanceof Integer inter){
                    Integer interMain = (Integer) curr.getCustomData();
                    if(inter<=interMain){
                        button.highlight();
                    }
                    else{
                        button.unhighlight();
                    }
                }
            }
        }
        for (ButtonAPI button : buttons) {
            if(button.isChecked()){
                button.setChecked(false);
                if(button.getCustomData() instanceof Integer inter){
                    Global.getSector().getPlayerFaction().getDoctrine().setOfficerQuality(inter);
                }
                curr = button;
                OverviewPatrolPanel.forceRequestUpdateStatsOnly = true;
                break;
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
