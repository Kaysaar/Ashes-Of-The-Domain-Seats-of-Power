package data.plugins.coreui;

import ashlib.data.plugins.coreui.CommandTabListener;
import ashlib.data.plugins.coreui.CommandUIPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import data.misc.UIDataSop;
import data.scripts.managers.AoTDFactionManager;
import data.ui.FactionPanel;
import org.lwjgl.input.Keyboard;

import static data.plugins.AoTDSopMisc.tryToGetButtonProd;

public class FactionTabListener implements CommandTabListener {
    @Override
    public String getNameForTab() {
        return "Faction";
    }

    @Override
    public String getButtonToReplace() {
        return "orders";
    }

    @Override
    public String getButtonToBePlacedNear() {
        return "colonies";
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
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addSectionHeading("Ashes of the Domain : Seats Of Power", Alignment.MID,0f);
                tooltip.addPara("In this tab,your empire timeline and can govern your faction by changing policies.",5f);
            }
        };
    }

    @Override
    public CommandUIPlugin createPlugin() {
        return new FactionPanel(UIDataSop.WIDTH,UIDataSop.HEIGHT);
    }

    @Override
    public float getWidthOfButton() {
        return 130;
    }

    @Override
    public int getKeyBind() {
        return Keyboard.KEY_2;
    }

    @Override
    public void performRecalculations(UIComponentAPI uiComponentAPI) {
        ButtonAPI button = tryToGetButtonProd("holdings");
        if(button==null){
            button = tryToGetButtonProd("colonies");
        }
        UIDataSop.WIDTH = Global.getSettings().getScreenWidth() - button.getPosition().getX();
        UIDataSop.HEIGHT = uiComponentAPI.getPosition().getHeight();
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public boolean shouldButtonBeEnabled() {
        return !AoTDFactionManager.getMarketsUnderPlayer().isEmpty();
    }

    @Override
    public void performRefresh(ButtonAPI buttonAPI) {

    }

}
