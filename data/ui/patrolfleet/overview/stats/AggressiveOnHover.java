package data.ui.patrolfleet.overview.stats;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class AggressiveOnHover implements TooltipMakerAPI.TooltipCreator {
    int aggressiveLevel;
    public AggressiveOnHover(int aggressiveLevel) {
        this.aggressiveLevel = aggressiveLevel;
    }
    @Override
    public boolean isTooltipExpandable(Object tooltipParam) {

        return false;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 400;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        tooltip.addPara("Aggression level : %s",3f, Color.ORANGE,""+aggressiveLevel);
        String toHighlight = null;
        String before = "";
        switch (aggressiveLevel){
            case 1->{
            toHighlight = Misc.getAndJoined("Cautious");
            }
            case 2->{
                toHighlight = Misc.getAndJoined("Steady");
            }
            case 3->{
                toHighlight = Misc.getAndJoined("Aggressive");
            }
            case 4->{
                before = "A mix of ";
                toHighlight = Misc.getAndJoined("aggressive","reckless");
            }
            case 5->{
                toHighlight = Misc.getAndJoined("Reckless");
            }
        }
        tooltip.addPara(before+"%s officers and ship commanders",5f,Color.ORANGE,toHighlight);
    }
}
