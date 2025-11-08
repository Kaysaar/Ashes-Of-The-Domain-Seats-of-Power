package data.scripts.ambition.impl;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.scripts.ambition.BaseAmbition;

public class AdAstraPerAspera extends BaseAmbition {

    @Override
    public void createFlavourTooltip(TooltipMakerAPI tooltip) {
        float progress = getProgress();

        tooltip.addPara("Test",5f);
    }
}
