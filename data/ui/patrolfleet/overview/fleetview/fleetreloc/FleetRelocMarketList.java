package data.ui.patrolfleet.overview.fleetview.fleetreloc;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetRelocMarketList implements ExtendedUIPanelPlugin {
    CustomPanelAPI mainPanel,componentPanel;
    FactionAPI faction;
    ArrayList<FleetLocationData>data = new ArrayList<>();
    MarketAPI marketOrigin;
    public FleetRelocMarketList(float width, float height, FactionAPI faction,MarketAPI marketOrigin){
        mainPanel = Global.getSettings().createCustom(width,height,this);
        this.faction = faction;
        this.marketOrigin = marketOrigin;
    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        if(componentPanel!=null){
            mainPanel.removeComponent(componentPanel);
        }
        data.clear();
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = componentPanel.createUIElement(componentPanel.getPosition().getWidth(),componentPanel.getPosition().getHeight(),true);
        ArrayList<MarketAPI>markets = sortMarketsByDistance(Misc.getFactionMarkets(faction),marketOrigin);
        for (MarketAPI market : markets) {
            if(market.getId().equals(marketOrigin.getId()))continue;
            FleetLocationData data = new FleetLocationData(componentPanel.getPosition().getWidth()-10,50,market,0f,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Misc.getBrightPlayerColor(),false,Misc.getDistanceLY(marketOrigin.getPrimaryEntity(),market.getPrimaryEntity()));
            data.createUI();
            this.data.add(data);
            tooltip.addCustom(data.getPanel(),5f);
        }
        componentPanel.addUIElement(tooltip).inTL(-10,0);
        mainPanel.addComponent(componentPanel);

    }

    public ArrayList<MarketAPI> sortMarketsByDistance(List<MarketAPI> currMarkets, MarketAPI marketOrigin) {
        if (currMarkets == null) return new ArrayList<>();
        if (marketOrigin == null || marketOrigin.getPrimaryEntity() == null) {
            return new ArrayList<>(currMarkets);
        }

        final SectorEntityToken origin = marketOrigin.getPrimaryEntity();

        return currMarkets.stream()
                .filter(m -> m != null && m.getPrimaryEntity() != null)
                .sorted(Comparator.comparingDouble(m ->
                        Misc.getDistanceLY(origin, m.getPrimaryEntity())
                ))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    @Override
    public void clearUI() {
        data.clear();
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

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
