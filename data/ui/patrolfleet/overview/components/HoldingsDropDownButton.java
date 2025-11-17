package data.ui.patrolfleet.overview.components;

import ashlib.data.plugins.ui.models.CustomButton;
import ashlib.data.plugins.ui.models.DropDownButton;
import ashlib.data.plugins.ui.plugins.UITableImpl;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;

public class HoldingsDropDownButton extends DropDownButton {
    StarSystemAPI main;
    ArrayList<MarketAPI> sub;

    public HoldingsDropDownButton(UITableImpl tableOfReference, float width, float height, float maxWidth, float maxHeight, boolean droppableMode, StarSystemAPI system, ArrayList<MarketAPI> markets) {
        super(tableOfReference, width, height, maxWidth, maxHeight, system != null);
        this.main = system;
        this.sub = markets;
    }

    public List<MarketAPI> getMarkets() {
        return sub;

    }

    public StarSystemAPI getStarSystem() {
        return main;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        HoldingsButton bt = (HoldingsButton) mainButton;
        bt.setArrowPointDown(isDropped);

    }

    @Override
    public void createUIContent() {
        if (buttons == null) {
            buttons = new ArrayList<>();
            if (droppableMode) {
                for (MarketAPI subSpec : sub) {
                    HoldingsButton button = new HoldingsButton(width-5f , height, subSpec, 5f, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), false);
                    button.createUI();
                    buttons.add(button);
                }
            }
            if (droppableMode) {
                mainButton = new HoldingsButton(width , height, main, 0f, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), true);

            } else {
                mainButton = new HoldingsButton(width , height, main, 0f, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), false);

            }
            mainButton.createUI();
        }
        if (!isDropped) {
            tooltipOfImpl.addCustom(mainButton.getPanel(), 5f).getPosition().inTL(0, 0);
        } else {
            tooltipOfImpl.addCustom(mainButton.getPanel(), 5f).getPosition().inTL(0, 0);
            float currY = mainButton.getPanel().getPosition().getHeight() + 2;
            for (CustomButton button : buttons) {
                tooltipOfImpl.addCustom(button.getPanel(), 0f).getPosition().inTL(button.indent, currY);
                currY += button.getPanel().getPosition().getHeight() + 2;
            }
            tooltipOfImpl.getPosition().setSize(width, currY);
            panelOfImpl.getPosition().setSize(width, currY);
            mainPanel.getPosition().setSize(width, currY);
        }
    }

    @Override
    public void createUI() {
        super.createUI();
    }

    @Override
    public void clear() {
        super.clear();
        sub.clear();
    }
}
