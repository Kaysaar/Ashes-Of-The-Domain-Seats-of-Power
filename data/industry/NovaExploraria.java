package data.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.industry.ui.NovaExplorariaButton;
import data.intel.NovaExplorariaExpeditionIntel;
import data.route.AbyssDelversFleetRouteManager;
import data.route.NovaExplorariaExpeditionFleetRouteManager;
import data.route.TechHuntersFleetRouteManager;
import data.scripts.managers.AoTDFactionManager;
import data.ui.overview.capitalbuilding.BaseCapitalButton;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Random;


public class NovaExploraria extends BaseCapitalIndustry {
    public int amountOfExpeditions =0;
    public MutableStat maxAmountOfExpeditions = new MutableStat(1f);
    public MutableStat multiplierOfTechHunterCost = new MutableStat(1f);
    public MutableStat multiplierOfAbyssExpeditions = new MutableStat(1f);
    public MutableStat multiplierOfSurveyExpeditions = new MutableStat(1f);
    ArrayList<CampaignFleetAPI>techHunterFleets = new ArrayList<>();
    ArrayList<CampaignFleetAPI>abyssDivers = new ArrayList<>();

    public ArrayList<CampaignFleetAPI> getTechHunterFleets() {
        return techHunterFleets;
    }

    public MutableStat getMultiplierOfSurveyExpeditions() {
        return multiplierOfSurveyExpeditions;
    }

    public MutableStat getMultiplierOfAbyssExpeditions() {
        return multiplierOfAbyssExpeditions;
    }

    public ArrayList<CampaignFleetAPI> getAbyssDivers() {
        return abyssDivers;
    }

    @Override
    public boolean isAvailableToBuild() {
        return AoTDFactionManager.getInstance().doesControlCapital()&&AoTDFactionManager.getInstance().getCapitalMarket().getId().equals(market.getId());
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        cleanupInactiveFleets();
        if(amountOfExpeditions<=0){
            amountOfExpeditions = 0;
        }
        /// Existing save file fix
        if(!Global.getSector().getPlayerMemoryWithoutUpdate().is("$aotd_fix_nova",true)){
            Global.getSector().getPlayerMemoryWithoutUpdate().set("$aotd_fix_nova",true);
            if(amountOfExpeditions >= Math.abs(abyssDivers.size()+techHunterFleets.size())){
                amountOfExpeditions -= (Math.abs(abyssDivers.size()+techHunterFleets.size()));
            }
        }
    }
    public void cleanupInactiveFleets() {
        techHunterFleets.removeIf(fleet -> fleet == null || fleet.isDespawning()|| !fleet.isAlive());
        abyssDivers.removeIf(fleet -> fleet == null || fleet.isDespawning() || !fleet.isAlive());
    }

    public MutableStat getMultiplierOfTechHunterCost() {
        return multiplierOfTechHunterCost;
    }

    public boolean canSentExpedition(){
        return getRemainingOnes()>0;
    }
    public static boolean canDoInifniteTechmining(){
        return Global.getSector().getMemoryWithoutUpdate().is("$aotd_nova_exploraria_update_1",true);
    }
    public static boolean canDoAbyssDiving(){
        return Global.getSector().getMemoryWithoutUpdate().is("$aotd_nova_exploraria_update_2",true);
    }
    public static void setCanDoAbyssDiving(boolean can){
        Global.getSector().getMemoryWithoutUpdate().set("$aotd_nova_exploraria_update_2",can);
    }
    public static void setCanDoInifniteTechmining(){
         Global.getSector().getMemoryWithoutUpdate().set("$aotd_nova_exploraria_update_1",true);
    }

    public static NovaExploraria getNova(){
        MarketAPI market = AoTDFactionManager.getInstance().getCapitalMarket();

        if(market!=null){
            return (NovaExploraria) market.getIndustry("aotd_nova_exploraria");
        }
        return null;
    }
    public static void   finishExpedition(){
        if(getNova()!=null){
            getNova().setAmountOfExpeditions(getNova().amountOfExpeditions-1);
        }
    }
    public  int getCurrentAmountOfExpeditions(){
        return amountOfExpeditions+Math.abs(abyssDivers.size()+techHunterFleets.size());
    }
    @Override
    public void apply() {

    }

    public void setAmountOfExpeditions(int amountOfExpeditions) {
        this.amountOfExpeditions = amountOfExpeditions;
    }
    public MutableStat getMaxAmountOfExpeditions(){
        return maxAmountOfExpeditions;
    }
    public  int getRemainingOnes(){
        return getMaxAmountOfExpeditions().getModifiedInt()-getCurrentAmountOfExpeditions();
    }

    public void sentExpeditionFleet(StarSystemAPI systemToSurvey){
        if(!canSentExpedition())return;
        setAmountOfExpeditions(amountOfExpeditions+1);

        CampaignFleetAPI fleet = createTechHunter(FleetTypes.SCAVENGER_MEDIUM,market.getStarSystem().getLocation(), null, market, false, Misc.random);
        fleet.setFaction(Factions.PLAYER);
        fleet.setNoFactionInName(true);
        fleet.setName("Nova Exploraria Expedition Fleet");
        Long seed = new Random().nextLong();

        fleet.addTag("aotd_expedition");
        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
        RouteManager.RouteData data = new RouteManager.RouteData("nova_exploraria_" + market.getFaction().getId(), market, seed, new RouteManager.OptionalFleetData(market));
        data.addSegment(new RouteManager.RouteSegment(30000f, market.getPrimaryEntity()));
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, false);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, false);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);

        NovaExplorariaExpeditionIntel intel = NovaExplorariaExpeditionIntel.get(systemToSurvey);
        fleet.addScript(new NovaExplorariaExpeditionFleetRouteManager(fleet, data, systemToSurvey,intel));
        Global.getSector().getIntelManager().addIntel(intel);


    }
    public void sentTechHunterFleet(int expectedMonths){
        if(!canSentExpedition())return;

        CampaignFleetAPI fleet = createTechHunter(FleetTypes.SCAVENGER_LARGE,market.getStarSystem().getLocation(), null, market, false, Misc.random);
        fleet.setNoFactionInName(true);
        fleet.setName("Nova Exploraria Tech Hunters Fleet");
        Long seed = new Random().nextLong();

        fleet.addTag("aotd_expedition");
        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
        RouteManager.RouteData data = new RouteManager.RouteData("nova_exploraria_hunter_" + market.getFaction().getId(), market, seed, new RouteManager.OptionalFleetData(market));
        data.addSegment(new RouteManager.RouteSegment(30000f, market.getPrimaryEntity()));
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, false);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, false);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        techHunterFleets.add(fleet);

        fleet.addScript(new TechHuntersFleetRouteManager(fleet, data,expectedMonths));

    }
    public void sentAbyssDivers(int expectedMonths){
        if(!canSentExpedition())return;
        CampaignFleetAPI fleet = createTechHunter(FleetTypes.SCAVENGER_LARGE,market.getStarSystem().getLocation(), null, market, false, Misc.random);
        fleet.setNoFactionInName(true);
        fleet.setName("Nova Exploraria Abyss Delvers");
        Long seed = new Random().nextLong();

        fleet.addTag("aotd_expedition");
        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
        RouteManager.RouteData data = new RouteManager.RouteData("nova_exploraria_hunter_" + market.getFaction().getId(), market, seed, new RouteManager.OptionalFleetData(market));
        data.addSegment(new RouteManager.RouteSegment(30000f, market.getPrimaryEntity()));
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, false);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MAY_GO_INTO_ABYSS, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, false);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        abyssDivers.add(fleet);
        fleet.addScript(new AbyssDelversFleetRouteManager(fleet, data,expectedMonths));



    }
    @Override
    public BaseCapitalButton createButton(float width, float height) {
        return new NovaExplorariaButton(width,height,this);
    }
    public static CampaignFleetAPI createTechHunter(String type, Vector2f locInHyper, RouteManager.RouteData route, MarketAPI source, boolean pirate, Random random) {
        if (random == null) random = new Random();


        if (type == null) {
            WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
            picker.add(FleetTypes.SCAVENGER_SMALL, 10f);
            picker.add(FleetTypes.SCAVENGER_MEDIUM, 15f);
            picker.add(FleetTypes.SCAVENGER_LARGE, 5f);
            type = picker.pick();
        }


        int combat = 0;
        int freighter = 0;
        int tanker = 0;
        int transport = 0;
        int utility = 0;


        if (type.equals(FleetTypes.SCAVENGER_SMALL)) {
            combat = random.nextInt(2) + 1;
            tanker = random.nextInt(2) + 1;
            utility = random.nextInt(2) + 1;
        } else if (type.equals(FleetTypes.SCAVENGER_MEDIUM)) {
            combat = 4 + random.nextInt(5);
            freighter = 4 + random.nextInt(5);
            tanker = 3 + random.nextInt(4);
            transport = random.nextInt(2);
            utility = 2 + random.nextInt(3);
        } else if (type.equals(FleetTypes.SCAVENGER_LARGE)) {
            combat = 16 + random.nextInt(8);
            freighter = 6 + random.nextInt(7);
            tanker = 5 + random.nextInt(6);
            transport = 3 + random.nextInt(8);
            utility = 4 + random.nextInt(5);
        }

        if (pirate) {
//			combat += transport;
//			combat += utility;
            transport = utility = 0;
        }

        combat *= 5f;
        freighter *= 3f;
        tanker *= 3f;
        transport *= 1.5f;

        FleetParamsV3 params = new FleetParamsV3(
                route != null ? route.getMarket() : source,
                locInHyper,
                Factions.PLAYER, // quality will always be reduced by non-market-faction penalty, which is what we want
                route == null ? null : route.getQualityOverride(),
                type,
                combat, // combatPts
                freighter, // freighterPts
                tanker, // tankerPts
                transport, // transportPts
                0f, // linerPts
                utility, // utilityPts
                0f // qualityMod
        );
        if (route != null) {
            params.timestamp = route.getTimestamp();
        }
        params.random = random;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) return null;

        fleet.setFaction(Factions.PLAYER, true);

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SCAVENGER, true);

        if (pirate || true) {
            Misc.makeLowRepImpact(fleet, "scav");
        }

        return fleet;
    }
}
