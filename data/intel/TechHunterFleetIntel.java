package data.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class TechHunterFleetIntel extends BaseIntelPlugin {
    CampaignFleetAPI target;
    CargoAPI savedCargo;
    public CargoAPI getCargo() {
        return savedCargo;
    }
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public TechHunterFleetIntel(CampaignFleetAPI target) {
        this.target = target;
        savedCargo = Global.getFactory().createCargo(false);

    }
    public static TechHunterFleetIntel get(CampaignFleetAPI target) {
        for (IntelInfoPlugin intelInfoPlugin : Global.getSector().getIntelManager().getIntel(TechHunterFleetIntel.class)) {
            if (((TechHunterFleetIntel) intelInfoPlugin).target.equals(target)){
                return (TechHunterFleetIntel) intelInfoPlugin;
            }
        }
        return  new TechHunterFleetIntel(target);
    }

    public boolean successful = false;
    public boolean finished = false;

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }


    public boolean isSuccessful() {
        return successful;
    }

    boolean canBeDeleted = false;
    @Override
    protected void notifyEnded() {
        target = null;
        getCargo().clear();
        savedCargo = null;
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

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }
    @Override
    public boolean shouldRemoveIntel() {
        return isEnded();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;
        if (!finished) {
            info.addPara("Currently fleet is conducting tech mining operations across entire sector", pad, Color.ORANGE, target.getName());

        }
        else{
            if(successful){
                info.addPara("Tech Hunters have brought valuable loot that has been re-located to local storages of %s", opad, Color.ORANGE, Global.getSector().getPlayerFaction().getProduction().getGatheringPoint().getName());
                bullet(info);
                info.showCargo(savedCargo, 20, true, opad);
            }
            else{
                info.addPara("Tech Hunters fleet was destroyed during operation, probably result of hostile actions of third parties",Misc.getTooltipTitleAndLightHighlightColor(),opad);
            }
            canBeDeleted = true;
        }
        if (canBeDeleted) {
            addDeleteButton(info,width-10);
            info.addSpacer(5f);
        }

    }
    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        bullet(info);
        float pad = 3f;
        float opad = 10f;
        if(mode.equals(ListInfoMode.MESSAGES)){
            if(isSuccessful()){
                    info.addPara("Fleet has successfully returned from expedition!",pad);

            } else if (finished) {
                info.addPara("Contact with fleet has been lost",pad);
            }

            info.addPara("Exploration is ready to find forgotten artefacts across the Sector",pad);
        }
        unindent(info);
    }
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        // Caused a 'return value of "data.scripts.managers.AoTDFactionManager.getCapitalMarket()" is null'
        // when losing the capital post sending a fleet with existing intel bulletins
//        return AoTDFactionManager.getInstance().getCapitalMarket().getPrimaryEntity();

        if (AoTDFactionManager.getInstance().getCapitalMarket() != null) {
            SectorEntityToken capitalEntity = AoTDFactionManager.getInstance().getCapitalMarket().getPrimaryEntity();
            Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().set("$knownCapital", capitalEntity);
            return capitalEntity;
        }
        else {
            SectorEntityToken knownCapital = (SectorEntityToken) Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().get("$knownCapital");
            if (knownCapital == null) throw new RuntimeException("Player faction capital has never been set");
            else return knownCapital;
        }
    }

    @Override
    protected String getName() {
        if(isSuccessful()){
            return "Tech Hunter Report : Success";

        } else if (finished) {
            return "Tech Hunter Report : Failed";
        }
        return "Tech Hunter Expedition";
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.clear();
        tags.add("Nova Exploraria Archive");
        return tags;
    }
}
