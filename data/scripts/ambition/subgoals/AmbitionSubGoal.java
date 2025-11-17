package data.scripts.ambition.subgoals;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ambition.AmbitionSpecManager;

public class AmbitionSubGoal {
   String idOfSpec;
   boolean completed = false;

    public String getIdOfSpec() {
        return idOfSpec;
    }
    public AmbitionSubGoalSpec getSpec(){
        return AmbitionSpecManager.getSubGoalSpec(getIdOfSpec());
    }

    public void setIdOfSpec(String idOfSpec) {
        this.idOfSpec = idOfSpec;
    }
    public void createTaskLabelCompleted(TooltipMakerAPI tooltip){
        tooltip.addPara(getSpec().getTaskToDo(), Misc.getPositiveHighlightColor(),3f);

    }
    public boolean shouldShowOnUI(){
        return true;
    }
    public boolean isCompleted() {
        if(!completed){
            doCheckupForQuest();
        }
        return completed;

    }
    public void doCheckupForQuest(){

    }

    public void createTaskLabelNonComplete(TooltipMakerAPI tooltip){

    }
    public void createTaskLabel (TooltipMakerAPI tooltip){

        if(completed){
            createTaskLabelCompleted(tooltip);
        }
        else{
            createTaskLabelNonComplete(tooltip);
        }
    }



}
