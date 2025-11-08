package data.scripts.ambition;

import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.LinkedHashSet;

public class BaseAmbition {
    String idOfSpec;
    public LinkedHashSet<String>currentTask = new LinkedHashSet<>();
    public LinkedHashSet<String>completedTask = new LinkedHashSet<>();
    public void init(String specId){
        this.idOfSpec = specId;
        currentTask.addAll(getSpec().getStartingTasks());
    }
    public float getProgress(){
        if(getSpec().getTaskList().isEmpty()){
            return 1f;
        }
        int total ,completed;
        total = 0;
        completed =0;
        for (String e : getSpec().getTaskList()) {
            int weight = AmbitionSpecManager.getSubGoalSpec(e).getWeight();
            if(completedTask.contains(e)){
                completed+=weight;
            }
            total+=weight;
        }
        return total/(float)completed;
    }
    public AmbitionSpec getSpec(){
        return AmbitionSpecManager.getAmbitionSpec(idOfSpec);
    }
    public void createFlavourTooltip(TooltipMakerAPI tooltip){

    }
    public void createIntroductionTooltip(TooltipMakerAPI tooltip){
        tooltip.addPara(getSpec().getDesc(),
                Misc.getTooltipTitleAndLightHighlightColor(),
                2f
        );
        if(AshMisc.isStringValid(getSpec().getShortExplanation())){
            tooltip.addPara(getSpec().getShortExplanation(),5f).setAlignment(Alignment.MID);
        }
    }
}
