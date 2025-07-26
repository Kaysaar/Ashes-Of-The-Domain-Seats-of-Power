package data.listeners;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.conditions.AoTDFactionCapital;
import data.scripts.managers.AoTDFactionManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChooseCapitalListener extends BaseIndustryOptionProvider {
    public static Object INTERACTION_PLUGIN = new Object();
    public static Object INTERACTION_TRIGGER = new Object();
    public static Object CUSTOM_PLUGIN = new Object();
    public static Object STORY_ACTION = new Object();
    public static Object IMMEDIATE_ACTION = new Object();
    public static Object DISABLED_OPTION = new Object();

    @Override
    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        if (isUnsuitable(ind, false)) return null;


        if (ind.getId().equals(Industries.POPULATION)&&ind.getMarket().getPrimaryEntity() instanceof PlanetAPI &&ind.getMarket().getFaction().isPlayerFaction()&& !AoTDFactionManager.getInstance().didDeclaredCapital()&&ind.getMarket().getSize()>=5) {

            List<IndustryOptionData> result = new ArrayList<IndustryOptionData>();

            IndustryOptionData opt = new IndustryOptionData("Declare "+ind.getMarket().getName()+" a capital", CUSTOM_PLUGIN, ind, this);
            opt.color = Color.ORANGE;
            result.add(opt);


            return result;
        }
        return null;
    }

    @Override
    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CUSTOM_PLUGIN) {
            tooltip.addPara("Declare this world a capital", 0f);
        }
    }

    @Override
    public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui) {
         if (opt.id == CUSTOM_PLUGIN) {
            CustomDialogDelegate delegate = new BaseCustomDialogDelegate() {
                @Override
                public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                    TooltipMakerAPI info = panel.createUIElement(800, 100, false);
                    info.addSpacer(2f);
                    info.setParaInsigniaLarge();;
                    info.addPara("Declaring this world as your capital will grant a %s bonus and allow the construction of unique buildings that can only be built on a capital world. Losing this world will cause a -10 Stability penalty across all colonies until the capital is retaken.", 5f,new Color[]{Color.ORANGE,Misc.getNegativeHighlightColor()},"+3 Stability","-7 Stability");

                    panel.addUIElement(info).inTL(0, 0);
                }

                @Override
                public boolean hasCancelButton() {
                    return true;
                }

                @Override
                public void customDialogConfirm() {
                    AoTDFactionManager.getInstance().setCapitalID(opt.ind.getMarket().getPrimaryEntity().getId());
                    opt.ind.getMarket().removeIndustry(Industries.POPULATION, MarketAPI.MarketInteractionMode.REMOTE,false);
                    opt.ind.getMarket().addIndustry("aotd_capital_complex");
                    Industry ind = opt.ind.getMarket().getIndustry(Industries.POPULATION);
                    ind.setSpecialItem(opt.ind.getSpecialItem());
                    ind.setImproved(opt.ind.isImproved());
                    ind.setAICoreId(opt.ind.getAICoreId());
                    PlanetAPI planet = ind.getMarket().getPlanetEntity();
                    PlanetSpecAPI spec = planet.getSpec();
                    ((PlanetSpec) spec).name = "Capital";
                    planet.applySpecChanges();

                    AoTDFactionCapital.applyToCapital();

                }

                @Override
                public void customDialogCancel() {
                }
            };
            ui.showDialog(800, 100, delegate);
        }

    }

}
