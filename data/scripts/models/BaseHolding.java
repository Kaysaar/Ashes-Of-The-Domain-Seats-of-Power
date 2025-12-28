package data.scripts.models;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;

public class BaseHolding {
    public ArrayList<BaseHolding>subHoldings;
    public SectorEntityToken tokenOfHolding;
    //Sprite of holding if is not null will override tokenOfHolding image
    public String pathToImage;
    public String id;
    public String getName(){
        if(tokenOfHolding != null){
            return tokenOfHolding.getName();
        }
        return "";
    }

    public ArrayList<BaseHolding> getSubHoldings() {
        return subHoldings;
    }

    public SectorEntityToken getTokenOfHolding() {
        return tokenOfHolding;
    }

    public String getPathToImage() {
        return pathToImage;
    }

    public void setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
    }

    public void setTokenOfHolding(SectorEntityToken tokenOfHolding) {
        this.tokenOfHolding = tokenOfHolding;
    }
    public void addSubHolding(BaseHolding subHolding) {
        subHoldings.add(subHolding);
    }
    public BaseHolding(SectorEntityToken token){
        this.tokenOfHolding = token;
        this.id = token.getId();
        this.subHoldings = new ArrayList<BaseHolding>();
    }
    public BaseHolding(String id,String pathOfImage){
        this.pathToImage = pathOfImage;
        this.id = id;
        this.subHoldings = new ArrayList<BaseHolding>();
    }
    public TooltipMakerAPI.TooltipCreator getOnHoverTooltipCreator(){
        return null;
    }



}
