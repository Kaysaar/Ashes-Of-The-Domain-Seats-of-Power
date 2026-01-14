package data.ui.tooltips;

import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class TempArmoryTooltip extends BaseTooltipCreator {

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        tooltip.addPara("Not implemented into SoP yet. For setting the AI fleet aggression, check the Overview tab",0);
    }

}
