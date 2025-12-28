package data.ui.holdings.starsystems;

import ashlib.data.plugins.ui.models.DropDownButton;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import data.misc.ReflectionUtilis;
import data.ui.holdings.starsystems.components.StarSystemHoldingDropDown;
import data.ui.holdings.starsystems.components.StarSystemHoldingTable;

import java.util.List;

public class StarSystemHoldingsUI implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,contentPanel;

    StarSystemHoldingTable table;
    CurrentStarSystemTab currSystem;
    Object originalPanel;
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }
    public StarSystemHoldingsUI(float width,float height,Object originalPanel) {
        this.originalPanel = originalPanel;
        this.mainPanel = Global.getSettings().createCustom(width,height,this);

        createUI();
    }
    @Override
    public void createUI() {
        if(contentPanel!=null){
            contentPanel.removeComponent(mainPanel);
        }
        contentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        if (table != null) {
            table.recreateTable();
        }
        else{
            if(contentPanel.getPosition().getWidth()-StarSystemHoldingTable.getWidth()-18>765){
                float additional = contentPanel.getPosition().getWidth()-StarSystemHoldingTable.getWidth()-18-765;
                StarSystemHoldingTable.reDestributeAdditionalWidth(additional);
            }
            CustomPanelAPI panel = Global.getSettings().createCustom(StarSystemHoldingTable.getWidth()+13 , contentPanel.getPosition().getHeight()-25, null);
            table = new StarSystemHoldingTable(panel.getPosition().getWidth(), panel.getPosition().getHeight(), panel, true, 0, 0);
            if( Global.getSector().getPlayerMemoryWithoutUpdate().contains("$aotd_tab_holdings_star_id")){
                String id = Global.getSector().getPlayerMemoryWithoutUpdate().getString("$aotd_tab_holdings_star_id");
                for (DropDownButton dropDownButton : table.dropDownButtons) {
                    if(dropDownButton instanceof StarSystemHoldingDropDown bt){
                        if(bt.getStarSystem().getId().equals(id)){
                            table.currSystem = bt.getStarSystem();
                        }
                    }
                }
            }
            table.createSections();
            table.createTable();
        }
        if(currSystem==null){
            currSystem = new CurrentStarSystemTab(Math.min(contentPanel.getPosition().getWidth()-StarSystemHoldingTable.getWidth()-18,750),contentPanel.getPosition().getHeight()-25);
        }
        contentPanel.addComponent(currSystem.getMainPanel()).inTL(contentPanel.getPosition().getWidth()-currSystem.getMainPanel().getPosition().getWidth()-15,0);
        contentPanel.addComponent(table.mainPanel).inTL(0,0);
        mainPanel.addComponent(contentPanel).inTL(0,0);

    }

    @Override
    public void clearUI() {

    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {
        if(table!=null){
            table.advance(amount);
            if(table.currSystem!=null){
                if(currSystem.currStarSystem==null){
                    currSystem.setCurrStarSystem(table.currSystem);
                    Global.getSector().getPlayerMemoryWithoutUpdate().set("$aotd_tab_holdings_star_id",table.currSystem.getId());
                } else if (!currSystem.currStarSystem.getId().equals(table.currSystem.getId())) {
                    currSystem.setCurrStarSystem(table.currSystem);
                    Global.getSector().getPlayerMemoryWithoutUpdate().set("$aotd_tab_holdings_star_id",table.currSystem.getId());
                }
            }
            if(table.currentlyChosenMarket!=null){
                MarketAPI copy = table.currentlyChosenMarket;
                table.currentlyChosenMarket = null;
                ReflectionUtilis.invokeMethodWithAutoProjection("showMarketDetail",originalPanel,copy);
            }
        }

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
