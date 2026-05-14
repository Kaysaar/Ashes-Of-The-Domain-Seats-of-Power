package data.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import data.kaysaar.aotd.tot.industries.AoTDToolboxPopAndInfra;

public class CapitalComplex extends AoTDToolboxPopAndInfra {
    @Override
    protected Object readResolve() {
        id = Industries.POPULATION;
        Object toReturn = super.readResolve();
        spec = Global.getSettings().getIndustrySpec("aotd_capital_complex");
        return toReturn;
    }

    @Override
    protected String getDescriptionOverride() {
        int size = market.getSize();
        String cid = null;
        if (size >= 1 && size <= 9) {
            cid = "population_" + size;
            MarketConditionSpecAPI mcs = Global.getSettings().getMarketConditionSpec(cid);
            if (mcs != null) {
                return spec.getDesc() + "\n\n" + mcs.getDesc().replaceAll("\\$MarketName", market.getName());
            }
        }
        return super.getDescriptionOverride();
    }

    @Override
    public String getId() {
        return Industries.POPULATION;
    }

    @Override
    public IndustrySpecAPI getSpec() {
        if (spec == null) spec = Global.getSettings().getIndustrySpec("aotd_capital_complex");
        if(!spec.getId().equals("aotd_capital_complex")){
            spec = Global.getSettings().getIndustrySpec("aotd_capital_complex");
        }
        return spec;
    }@Override
    public String getCurrentImage() {
        return spec.getImageName();
    }
}
