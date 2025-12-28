package data.scripts.models;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseSpaceStructure {
    public enum TooltipMode{
        WIDGET,
        UPGRADE,
        HOVER
    }
    public String upgradeId,specId;
    SectorEntityToken entity;
    StarSystemAPI system;

    public BaseSpaceStructure(String specId,StarSystemAPI system) {
        this.specId = specId;
        this.system = system;
    }

    public void setSpecId(String specId) {
        this.specId = specId;

    }

    public void apply(MarketAPI market){

    }
    public void unapply(MarketAPI market){

    }
    public boolean doesAffectAllMarkets(){
        return false;
    }
    public void advance(float amount) {
        FactionAPI faction = entity.getFaction();
        for (MarketAPI marketAPI : Misc.getMarketsInLocation(this.entity.getContainingLocation())) {
            if(doesAffectAllMarkets()||marketAPI.getFaction().getId().equals(faction.getId())){
                apply(marketAPI);
            }
        }
    }
    public void onRemoval(){
        for (MarketAPI marketAPI : Misc.getMarketsInLocation(this.entity.getContainingLocation())) {
            unapply(marketAPI);
        }
    }
    public void finishedBuildingOrUpgrading(boolean upgrade){
        if(upgrade){

        }
        else{

        }
    }

    public void createTooltipForWidget(TooltipMode mode, TooltipMakerAPI tooltip, boolean expanded){

    }
}
