package data.plugins.coreui;

import ashlib.data.plugins.coreui.CommandTabListener;
import ashlib.data.plugins.coreui.CommandUIPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import data.misc.UIDataSop;
import data.scripts.managers.AoTDFactionManager;
import data.ui.PatrolTabPanel;
import org.lwjgl.input.Keyboard;

public class PatrolTabListener implements CommandTabListener {
    @Override
    public String getNameForTab() {
        return "Military & Templates";
    }

    @Override
    public String getButtonToReplace() {
        return "doctrine & blueprints";
    }

    @Override
    public String getButtonToBePlacedNear() {
        return "income";
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
                tooltip.addPara("In this tabs you will be able to design patrol fleets and control your forces, to guard your assets.",5f);
            }
        };
    }

    @Override
    public CommandUIPlugin createPlugin() {
        return new PatrolTabPanel(UIDataSop.WIDTH, UIDataSop.HEIGHT);
    }

    @Override
    public float getWidthOfButton() {
        return 205;
    }

    @Override
    public int getKeyBind() {
        return Keyboard.KEY_4;
    }

    @Override
    public void performRecalculations(UIComponentAPI uiComponentAPI) {

    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public boolean shouldButtonBeEnabled() {
        return true;
    }

    @Override
    public void performRefresh(ButtonAPI buttonAPI) {

    }
}
