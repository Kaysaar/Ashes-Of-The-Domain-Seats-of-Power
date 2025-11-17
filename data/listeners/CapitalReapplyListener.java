package data.listeners;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.conditions.AoTDFactionCapital;
import data.scripts.ambition.AmbitionManager;
import data.scripts.managers.AoTDFactionManager;

public class CapitalReapplyListener implements PlayerColonizationListener {
    @Override
    public void reportPlayerColonizedPlanet(PlanetAPI planet) {
        if(AoTDFactionManager.getInstance().getCapitalMarket()!=null){
            if(planet.getMarket().getId().equals(AoTDFactionManager.getInstance().getCapitalMarket().getId())){
                planet.getMarket().removeIndustry(Industries.POPULATION, MarketAPI.MarketInteractionMode.REMOTE,false);
                PlanetSpecAPI spec = planet.getSpec();
                ((PlanetSpec) spec).name = "Capital";
                planet.applySpecChanges();
                AoTDFactionCapital.applyToCapital();
            }
        }
        AmbitionManager.getInstance();
    }

    @Override
    public void reportPlayerAbandonedColony(MarketAPI colony) {

    }
}
