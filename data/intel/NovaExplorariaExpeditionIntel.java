package data.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class NovaExplorariaExpeditionIntel extends BaseIntelPlugin {
    LinkedHashMap<PlanetAPI, String> surveyMap = new LinkedHashMap<>();
    ArrayList<String> others = new ArrayList<>();
    StarSystemAPI target;

    public ArrayList<String> getOthers() {
        return others;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public NovaExplorariaExpeditionIntel(StarSystemAPI target) {
        this.target = target;

    }
    public static NovaExplorariaExpeditionIntel get(StarSystemAPI target) {
        for (IntelInfoPlugin intelInfoPlugin : Global.getSector().getIntelManager().getIntel(NovaExplorariaExpeditionIntel.class)) {
            if (((NovaExplorariaExpeditionIntel) intelInfoPlugin).target.equals(target)){
                return (NovaExplorariaExpeditionIntel) intelInfoPlugin;
            }
        }
        return  new NovaExplorariaExpeditionIntel(target);
    }

    public boolean successful = false;
    public boolean finished = false;

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }


    public boolean isSuccessful() {
        return successful;
    }

    public LinkedHashMap<PlanetAPI, String> getSurveyMap() {
        return surveyMap;
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }

    boolean canBeDeleted = false;
    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;
        if (!finished) {
            info.addPara("Currently fleet is conducting survey expedition, to unravel data about %s", pad, Color.ORANGE, target.getName());
        }
        else{
            if(successful){
                info.addPara("Exploration fleet has brought valuable data about this star system", opad, Color.ORANGE, target.getName());
            TooltipMakerAPI.PlanetInfoParams params = new TooltipMakerAPI.PlanetInfoParams();
                params.showConditions = true;
                params.showName = true;
                params.withClass = true;
                params.scaleEvenWhenShowingName = true;
                params.conditionsYOffset = 32f;
                params.showHazardRating = true;

                for (PlanetAPI planetAPI : surveyMap.keySet()) {
                    info.showPlanetInfo(planetAPI, width, width / 1.62f, params, opad + params.conditionsYOffset);
                    opad =30f;
                }
                if(!others.isEmpty()){
                    info.addPara("On top of that certain objects have been located.", opad, Color.ORANGE, target.getName());
                    bullet(info);
                    for (String other : others) {
                        info.addPara(other, Color.ORANGE,pad);
                    }
                }
                else{
                    info.addPara("Nothing else was found",Misc.getTooltipTitleAndLightHighlightColor(),opad);
                }
            }
            else{
                info.addPara("Exploration fleet was destroyed during expedition, probably result of hostile actions of third parties",Misc.getTooltipTitleAndLightHighlightColor(),opad);
            }
            canBeDeleted = true;
        }
        if (canBeDeleted) {
            addDeleteButton(info,width-10);
            info.addSpacer(5f);
        }

    }

    // 3 Methods regarding intel deletion upon button press
    @Override
    protected void notifyEnded() {
        if (canBeDeleted) {
            Global.getSector().getIntelManager().removeIntel(this);
        }
    }
    @Override
    protected void addDeleteButton(TooltipMakerAPI info, float width) {
        addDeleteButton(info, width, "Delete Nova Exploraria log entry");
    }
    @Override
    protected void createDeleteConfirmationPrompt(TooltipMakerAPI prompt) {
        prompt.addPara("Are you sure you want to permanently delete this Nova Exploraria log entry?", Misc.getTextColor(), 0f);
    }

    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return target.getHyperspaceAnchor();
    }

    @Override
    protected String getName() {
        if(isSuccessful()){
            return "Exploraria Expedition Fleet Report: "+ target.getName();

        } else if (finished) {
            return "Exploraria Expedition Failure : "+target.getName();
        }
        return "Exploraria Expedition Fleet Status: "+ target.getName();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.clear();
        tags.add("Nova Exploraria Archive");
        return tags;
    }
}
