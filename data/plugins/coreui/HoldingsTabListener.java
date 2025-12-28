package data.plugins.coreui;

import ashlib.data.plugins.coreui.CommandTabListener;
import ashlib.data.plugins.coreui.CommandUIPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import data.misc.UIDataSop;
import data.ui.HoldingsUIPanel;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class HoldingsTabListener implements CommandTabListener {
    @Override
    public String getNameForTab() {
        return "Holdings";
    }

    @Override
    public String getButtonToReplace() {
        return "colonies";
    }

    @Override
    public String getButtonToBePlacedNear() {
        return null;
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getTooltipCreatorForButton() {
        return new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 500f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addSectionHeading("Ashes of the Domain : Seats of Power", Alignment.MID,0f);
                tooltip.addPara("This tab lists all holdings owned by you or your faction: %s, %s, %s, and %s."
                        ,5f, Color.ORANGE,"Star Systems","Colonies","Megastructures","Companies");
                tooltip.addPara("From here, you can manage all of them.", 3f);
            }
        };
    }

    @Override
    public CommandUIPlugin createPlugin() {
        return new HoldingsUIPanel(UIDataSop.WIDTH, UIDataSop.HEIGHT);
    }

    @Override
    public float getWidthOfButton() {
        return 130;
    }

    @Override
    public int getKeyBind() {
        return Keyboard.KEY_1;
    }

    @Override
    public void performRecalculations(UIComponentAPI uiComponentAPI) {

    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean shouldButtonBeEnabled() {
        return true;
    }

    @Override
    public void performRefresh(ButtonAPI buttonAPI) {

    }
}
