package data.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;

import java.awt.*;
import java.util.Set;

public class AbyssDelversFleetIntel extends BaseIntelPlugin {
    public boolean enteredAbyss = false;
    CampaignFleetAPI target;
    CargoAPI savedCargo;
    public boolean retreat= false;
    public CargoAPI getCargo() {
        return savedCargo;
    }
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public AbyssDelversFleetIntel(CampaignFleetAPI target) {
        this.target = target;
        savedCargo = Global.getFactory().createCargo(false);

    }
    public static AbyssDelversFleetIntel get(CampaignFleetAPI target) {
        for (IntelInfoPlugin intelInfoPlugin : Global.getSector().getIntelManager().getIntel(AbyssDelversFleetIntel.class)) {
            if (((AbyssDelversFleetIntel) intelInfoPlugin).target.equals(target)){
                return (AbyssDelversFleetIntel) intelInfoPlugin;
            }
        }
        return  new AbyssDelversFleetIntel(target);
    }

    public boolean successful = false;
    public boolean finished = false;

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public boolean isEnteredAbyss() {
        return enteredAbyss;
    }

    public void setEnteredAbyss(boolean enteredAbyss) {
        this.enteredAbyss = enteredAbyss;
    }

    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public boolean shouldRemoveIntel() {
        return isEnded();
    }

    @Override
    protected void notifyEnded() {
        target = null;
        getCargo().clear();
        savedCargo = null;
        Global.getSector().getIntelManager().removeIntel(this);
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;
        if (!finished) {
            if(!enteredAbyss){
                info.addPara("Currently fleet is on it's way, to dive into the Abyss, in hopes of finding valuable loot", pad, Color.ORANGE, target.getName());

            }
            else{
                info.addPara("A signal from fleet has been received, about entering successfully the Abyss, currently there is no communication, aside fleet sending low frequency signal, as sort of proof they are still alive!", pad, Color.ORANGE, target.getName());

            }

        }
        else{
            if(successful){
                if(retreat){
                    info.addPara("Abyss Delvers have barley survived journey ",pad );
                    info.addPara("Those who have not lost their sanity, described about weird vessels, that hunted them down one by one in deepest parts of the Abyss",Misc.getNegativeHighlightColor(),opad);


                }
                else{
                    info.addPara("Abyss Delvers have brought valuable loot that has been re-located to local storages of %s", opad, Color.ORANGE, Global.getSector().getPlayerFaction().getProduction().getGatheringPoint().getName());
                    bullet(info);
                    info.showCargo(savedCargo, 20, true, opad);

                }

            }
            else{
                info.addPara("We have no data about Abyss Delvers, Nova Exploraria HQ was receiving traces of their signal until now. This fleet was deemed lost.",Misc.getTooltipTitleAndLightHighlightColor(),opad);
            }
        }


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
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        bullet(info);
        float pad = 3f;
        float opad = 10f;
        if(mode.equals(ListInfoMode.MESSAGES)){
            if(isSuccessful()){
                if(retreat){
                    info.addPara("Fleet has barely returned from expedition!",pad);
                    return;

                }
                else{
                    info.addPara("Fleet has successfully returned from expedition!",pad);
                    return;

                }


            } else if (finished) {
                info.addPara("Contact with fleet has been lost",pad);
                return;
            }
            if(enteredAbyss){

                info.addPara("Fleet has entered the Abyss",pad);
                return;
            }
           info.addPara("Exploration is ready to explore the Abyss",pad);
            return;
        }
        unindent(info);
    }

    @Override
    protected String getName() {
        if(isSuccessful()){
            if(retreat){
                return "Abyss Delvers Report : Survival";
            }
            else{
                return "Abyss Delvers Report : Success";
            }


        } else if (finished) {
            return "Abyss Delvers Report : Failure";
        }
        if(enteredAbyss){
            return "Abyss Delvers :Last dive";

        }
        return "Abyss Delvers : Venture into unknown";
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.clear();
        tags.add("Nova Exploraria Archive");
        return tags;
    }
}
