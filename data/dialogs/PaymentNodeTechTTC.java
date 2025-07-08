package data.dialogs;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class PaymentNodeTechTTC implements TooltipMakerAPI.TooltipCreator {
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
        tooltip.addPara("Cost due to recent Tech Hunters Expeditions",5f);
    }

}