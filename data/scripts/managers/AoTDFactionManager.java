package data.scripts.managers;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import data.conditions.AoTDFactionCapital;
import data.scripts.factiongoals.BaseFactionGoal;
import data.scripts.factiongoals.MilitaryGoal;
import data.scripts.factiongoals.ProsperityGoal;
import data.scripts.factiongoals.ResearchAndExplorationGoal;
import data.scripts.models.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static data.scripts.patrolfleet.utilis.FleetPointUtilis.printStats;


public class AoTDFactionManager {
    public ArrayList<BaseFactionPolicy> currentFactionPolicies = new ArrayList<>();
    public HashSet<String> copyOfPolicies = new HashSet<>();
    public MutableStat availablePolicies = new MutableStat(0f);
    public ArrayList<CycleTimelineEvents> cycles = new ArrayList<>();
    public HashMap<TimelineEventType, MutableStat> goalStats = new HashMap<>();
    public HashMap<TimelineEventType, BaseFactionGoal> goalsScripts = new HashMap<>();
    public HashSet<Integer>intervalsForEachPolicySlot = new HashSet<>();
    public HashSet<Integer>intervalsForAdditionalAdmin = new HashSet<>();
    public int currentXP = 0;
    public String capitalID;//We do id here so it is not directly tied to market

    public int getMaxLevel(){
        int curr = 0;
        for (Integer policy : levels.keySet()) {
            curr = policy;
        }
        return curr;
    }
    public void addIntervalForPolicySlot(int level){
        intervalsForEachPolicySlot.add(level);
    }
    public void addIntervalForAdditionalAdmin(int level){
        intervalsForAdditionalAdmin.add(level);
    }

    public String getCapitalID() {
        return capitalID;
    }
    public boolean didDeclaredCapital(){
        return capitalID!= null;
    }
    public boolean doesControlCapital(){
        return didDeclaredCapital()&&getMarketsUnderPlayer().stream().anyMatch(x->x.getPrimaryEntity().getId().equals(capitalID));
    }
    public void setCapitalID(String capitalID) {
        this.capitalID = capitalID;
    }
    public void addXP(int xp) {
        int levelBefore = getEffectiveLevel();

        int maxXP = 0;
        for (Integer value : levels.values()) {
            maxXP += value;
        }

        int xpToAdd = Math.min(xp, maxXP - currentXP);

        if (xpToAdd > 0) {
            currentXP += xpToAdd;
            Global.getSector().getCampaignUI().addMessage(
                    "Gained " + xpToAdd + " Faction XP",
                    Misc.getPositiveHighlightColor()
            );
        }

        if (currentXP > maxXP) {
            currentXP = maxXP;
        }

        int levelAfter = getEffectiveLevel();
        if (levelBefore != levelAfter) {
            Global.getSector().getCampaignUI().addMessage(
                    "Reached level " + levelAfter + " : Check Faction Tab to see new bonuses!",
                    Misc.getPositiveHighlightColor()
            );
        }
    }

    public MarketAPI getCapitalMarket(){
        return getMarketsUnderPlayer().stream().filter(x->x.getPrimaryEntity().getId().equals(capitalID)).findFirst().orElse(null);
    }
    public boolean isCaptialMarket(MarketAPI market){
        return capitalID!=null&&market.getPrimaryEntity().getId().equals(capitalID);
    }

    public LinkedHashMap<Integer,Integer>levels = new LinkedHashMap<>();
    public void addLevel(int level,int threshold){
        levels.put(level,threshold);
    }

    public MutableStat getXpPointsPerMonth() {
        return xpPointsPerMonth;
    }

    public int getEffectiveXP(){
        int currXP = currentXP;
        int maxXP = 0;
        for (Map.Entry<Integer, Integer> entry : levels.entrySet()) {
            if(currXP>=entry.getValue()){
                currXP -= entry.getValue();
                maxXP = entry.getKey();
            }
            else{
                return currXP;
            }

        }
        return 0;
    }

    public float getProgressXP(){
        int currXP = currentXP;
        for (Map.Entry<Integer, Integer> entry : levels.entrySet()) {
            if(currXP>=entry.getValue()){
                currXP -= entry.getValue();
            }
            else{
                return (float) currXP /entry.getValue();
            }

        }
        return 0;
    }
    public int getEffectiveLevel(){
        int currXP = currentXP;
        int level = 1;
        for (Map.Entry<Integer, Integer> entry : levels.entrySet()) {
            level = entry.getKey();
            if(currXP>=entry.getValue()){
                currXP -= entry.getValue();
            }
            else{
                break;
            }

        }
        return level;
    }


    //    public long getTotalSizeRealistic(){
//        long size = 0;
//        for (MarketAPI marketAPI : AoTDFactionManager.getMarketsUnderPlayer()) {
//            long sizeExtracted = getRealisticSizeOfMarket(marketAPI);
//            size+=sizeExtracted;
//
//        }
//        return size;
//    }
//    private long getRealisticSizeOfMarket(MarketAPI marketAPI) {
//        long sizeExtracted = (long) Math.pow(10,marketAPI.getSize());
//        if(Misc.getMaxMarketSize(marketAPI)<sizeExtracted){
//            sizeExtracted+= (long) ((long) Math.pow(10,marketAPI.getSize())* marketAPI.getPopulation().getWeightValue());
//        }
//        return sizeExtracted;
//    }
//
//    public float getPercentageOfPopulationOnMarketRealistic(MarketAPI market){
//        if(getTotalSizeRealistic()==0)return 0;
//        return (float) (float)getRealisticSizeOfMarket(market) / getTotalSizeRealistic();
//    }
    public static float getTotalSize(FactionAPI faction) {
        float size = 0;
        if (faction.isPlayerFaction()) {
            for (MarketAPI marketAPI : AoTDFactionManager.getMarketsUnderPlayer()) {
                float sizeExtracted = getSizeOfMarket(marketAPI);
                size += sizeExtracted;

            }
        } else {
            for (MarketAPI marketAPI : Misc.getFactionMarkets(faction)) {
                float sizeExtracted = getSizeOfMarket(marketAPI);
                size += sizeExtracted;

            }
        }

        return size;
    }


    public static float getSizeOfMarket(MarketAPI marketAPI) {
        return marketAPI.getSize();
    }

    public static float getSizeOfSector() {
        int size = 0;
        for (MarketAPI allMarket : getAllMarkets()) {
            size += allMarket.getSize();
        }
        return size;
    }

    public static float getPercentageOfPopulationOnMarket(MarketAPI market) {
        if (getTotalSize(market.getFaction()) == 0) return 0;
        return (float) (float) getSizeOfMarket(market) / getTotalSize(market.getFaction());
    }

    public static float getPercentageOfFactionInSector(FactionAPI faction) {
        if (getTotalSize(faction) == 0) return 0;

        return getTotalSize(faction) / getSizeOfSector();
    }

    public LinkedHashMap<String, Color> marketColors = new LinkedHashMap<>(); //This is for UI for population so it does not change colors on flowchart every time

    public Color getMarketColor(String marketID) {
        if (marketColors == null) marketColors = new LinkedHashMap<>();
        if (marketColors.containsKey(marketID)) {
            return marketColors.get(marketID);
        }

        // Generate a unique, non-similar, non-black color
        Color newColor;
        int attempts = 0;
        do {
            newColor = generateRandomColor();
            attempts++;
            if (attempts > 10000) {
                break;
            }
        } while (isTooSimilar(newColor) || isTooDark(newColor));

        marketColors.put(marketID, newColor);
        return newColor;
    }

    private Color generateRandomColor() {
        Random rand = new Random();
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private boolean isTooDark(Color color) {
        // Consider it black or too dark if brightness is too low
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb[2] < 0.1f; // brightness too low (value is 0.0 to 1.0)
    }

    private boolean isTooSimilar(Color color) {
        for (Color existing : marketColors.values()) {
            if (colorDistance(color, existing) < 100.0) {
                return true;
            }
        }
        return false;
    }

    private double colorDistance(Color c1, Color c2) {
        int rDiff = c1.getRed() - c2.getRed();
        int gDiff = c1.getGreen() - c2.getGreen();
        int bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }


    public static int maxPerCategory = 1000;
    public static final String memKey = "$aotd_faction_manager";

    public HashMap<TimelineEventType, MutableStat> getGoalStats() {
        return goalStats;
    }

    public MutableStat getGoalStat(TimelineEventType type) {
        return goalStats.get(type);
    }

    public ArrayList<CycleTimelineEvents> getCycles() {
        return cycles;
    }

    public MutableStat xpPointsPerMonth = new MutableStat(0f);

    public void addCycle(int cycle) {
        if (cycles.stream().noneMatch(x -> x.getRecordedCycle() == cycle)) {
            cycles.add(new CycleTimelineEvents(cycle));
        }
    }

    public void addEventToTimeline(BaseFactionTimelineEvent event) {
        CampaignClockAPI clock = Global.getSector().getClock();
        event.setDate(clock.getCycle(), clock.getDay(), clock.getMonth());
        event.updateDataUponEntryOfUI();
        getCycle(Global.getSector().getClock().getCycle()).addNewEvent(event);
    }

    public void removeCycle(int cycle) {
        cycles.removeIf(x -> x.getRecordedCycle() == cycle);
    }
    public int getLevelThreshold(int level){
        return levels.get(level);
    }

    public CycleTimelineEvents getCycle(int cycle) {
        addCycle(cycle);
        return cycles.stream().filter(x -> x.getRecordedCycle() == cycle).findFirst().get();
    }

    public ArrayList<BaseFactionPolicy> getCurrentFactionPolicies() {
        return currentFactionPolicies;
    }

    public HashSet<String> getCopyOfPolicies() {
        return copyOfPolicies;
    }

    public MutableStat getAvailablePolicies() {
        return availablePolicies;
    }

    public void reportMonthEnd() {
       addXP( getXpPointsPerMonth().getModifiedInt());
    }

    public static AoTDFactionManager getInstance() {
        if (Global.getSector().getPersistentData().get(memKey) == null) {
            setInstance();
        }
        return (AoTDFactionManager) Global.getSector().getPersistentData().get(memKey);
    }

    public BaseFactionGoal getScriptForGoal(TimelineEventType type) {
        return goalsScripts.get(type);
    }

    public void setCurrentXP(int currentXP) {
        this.currentXP = currentXP;
    }

    public static void setInstance() {
        AoTDFactionManager manager = new AoTDFactionManager();
        manager.goalStats.put(TimelineEventType.MILITARY, new MutableStat(0f));
        manager.goalStats.put(TimelineEventType.PROSPERITY, new MutableStat(0f));
        manager.goalStats.put(TimelineEventType.RESEARCH_AND_EXPLORATION, new MutableStat(0f));

        manager.goalsScripts.put(TimelineEventType.MILITARY, new MilitaryGoal());
        manager.goalsScripts.put(TimelineEventType.PROSPERITY, new ProsperityGoal());
        manager.goalsScripts.put(TimelineEventType.RESEARCH_AND_EXPLORATION, new ResearchAndExplorationGoal());

        for (int i = 1; i < 9; i++) {
            manager.addLevel(i,2000);
        };
        manager.addIntervalForPolicySlot(1);
        manager.addIntervalForPolicySlot(3);
        manager.addIntervalForPolicySlot(5);
        manager.addIntervalForPolicySlot(8);

        manager.addIntervalForAdditionalAdmin(1);
        manager.addIntervalForAdditionalAdmin(3);
        manager.addIntervalForAdditionalAdmin(5);
        manager.addIntervalForAdditionalAdmin(7);
        manager.addIntervalForAdditionalAdmin(8);
        Global.getSector().getPersistentData().put(memKey, manager);

    }
    public int getNextLevelForPolicySlot(int level) {
        int effectiveLevel = level+1;
        int lev = getMaxLevel();
        printStats();
        for (Integer object : intervalsForEachPolicySlot) {
            if (object>=effectiveLevel) {
                return object;
            }
        }
        return lev;
    }
    public int getNextLevelForAdminSlot(int level) {
        int effectiveLevel = level+1;
        int lev = getMaxLevel();
        for (Integer object : intervalsForAdditionalAdmin) {
            if (object>=effectiveLevel) {
                return object;
            }
        }
        return lev;
    }
    public int getPolicySlotsFromLevel(int level) {
        int amount =0;
        for (Integer i : intervalsForEachPolicySlot) {
            if(level>=i){
                amount++;
            }
        }
        return amount;
    }
    public int getAdminsForEachLevel(int level) {
        int amount =0;
        for (Integer i : intervalsForAdditionalAdmin) {
            if(level>=i){
                amount++;
            }
        }
        return amount;
    }
    public BaseFactionPolicy getPolicyFromList(String id) {
        return currentFactionPolicies.stream().filter(x -> x.getSpec().getId().equals(id)).findFirst().orElse(FactionPolicySpecManager.getSpec(id).getPlugin());
    }

    public boolean doesHavePolicyEnabled(String policyID) {
        return currentFactionPolicies.stream().anyMatch(x -> x.getSpec().getId().equals(policyID));
    }

    public boolean doesHavePolicyInCopy(String policyID) {
        return copyOfPolicies.contains(policyID);
    }

    public void advance(float amount) {
        AoTDFactionCapital.applyToCapital();
        AoTDFactionCapital.applyPenalties();
        AoTDFactionManager.getInstance().getAvailablePolicies().modifyFlat("aotd_level_bonus", getPolicySlotsFromLevel(getEffectiveLevel()));
        Global.getSector().getPlayerStats().getAdminNumber().modifyFlat("aotd_level_bonus", getAdminsForEachLevel(getEffectiveLevel()),"Faction Experience");
        currentFactionPolicies.forEach(x -> x.advance(amount));
        currentFactionPolicies.forEach(BaseFactionPolicy::unapplyPolicy);
        currentFactionPolicies.forEach(BaseFactionPolicy::applyPolicy);
        cycles.forEach(x -> x.getEventsDuringCycle().forEach(y -> y.applyEffects(y.getEventsAffected())));
        goalsScripts.values().forEach(x -> x.advance(amount));
        getMarketsUnderPlayer().stream().filter(x->!x.hasCondition("aotd_handle_polciies")).forEach(x->x.addCondition("aotd_handle_polciies"));
        Global.getSector().getEconomy().getMarketsCopy().stream().filter(x->!x.getFaction().isPlayerFaction()).forEach(x->{
            if(x.hasCondition("aotd_handle_polciies")) {
                x.getCondition("aotd_handle_polciies").getPlugin().unapply(null);
                x.removeCondition("aotd_handle_polciies");
                x.removeCondition("aotd_ai_legal");
                x.removeCondition("aotd_followers_ludd");
            }

        });
    }

    public void addNewPolicy(String id) {
        boolean policyExists = doesHavePolicyEnabled(id);
        if (!policyExists) {
            BaseFactionPolicy policy = FactionPolicySpecManager.getSpec(id).getPlugin();
            currentFactionPolicies.add(policy);
            policy.applyPolicyEffectAfterChangeInUI(false);
            policy.applyPolicy();
        }
    }

    public void removePolicy(String id) {
        boolean policyExists = doesHavePolicyEnabled(id);
        if (policyExists) {
            currentFactionPolicies.stream()
                    .filter(x -> x.getSpec().getId().equals(id))
                    .findFirst()
                    .ifPresent(x -> {
                        x.applyPolicyEffectAfterChangeInUI(true);
                        x.unapplyPolicy();
                        getMarketsUnderPlayer().forEach(y->{
                            if(y.hasCondition("aotd_handle_polciies")) {
                                y.getCondition("aotd_handle_polciies").getPlugin().unapply(null);
                            }

                        });
                    });

            currentFactionPolicies.removeIf(x -> x.getSpec().getId().equals(id));
            getMarketsUnderPlayer().forEach(y->{
                if(y.hasCondition("aotd_handle_polciies")) {
                    y.getCondition("aotd_handle_polciies").getPlugin().apply(null);
                }

            });
        }
    }

    public void addPolicyToCopy(String id) {
        copyOfPolicies.add(id);
    }

    public void removePolicyFromCopy(String id) {
        copyOfPolicies.remove(id);
    }

    public void applyChangesFromUI() {
        List<BaseFactionPolicy> toRemove = new ArrayList<>(getCurrentFactionPolicies().stream()
                .filter(x -> !getCopyOfPolicies().contains(x.getSpec().getId()))
                .toList());

        toRemove.forEach(x -> removePolicy(x.getSpec().getId()));
        toRemove.clear();
        getCopyOfPolicies().stream().filter(x -> !doesHavePolicyEnabled(x)).forEach(this::addNewPolicy);
        getCopyOfPolicies().clear();
        copyOfPolicies.clear();
    }

    public void updateListBeforeUI() {
        copyOfPolicies.clear();
        currentFactionPolicies.forEach(x -> copyOfPolicies.add(x.getSpec().getId()));
    }

    public ArrayList<FactionPolicySpec> getSpecsForAvailableUI() {
        ArrayList<FactionPolicySpec> specs = new ArrayList<>(FactionPolicySpecManager.getFactionPolicySpecs().values());
        specs.removeIf(x -> !canUsePolicyInUI(x.getPlugin()));
        specs.removeIf(x->x.hasTag("hidden"));
        specs.removeIf(x -> copyOfPolicies.contains(x.getId()));

        return specs;
    }

    public void gainXP(float amount) {
        currentXP += (int) amount;
    }

    public boolean canUsePolicyInUI(BaseFactionPolicy policy) {
        boolean canUse = true;
        for (String incompatiblePolicyId : policy.getSpec().getIncompatiblePolicyIds()) {
            if (AoTDFactionManager.getInstance().doesHavePolicyInCopy(incompatiblePolicyId)) {
                canUse = false;
                break;
            }
        }


        return canUse & policy.showInUI() && copyOfPolicies.stream().noneMatch(x -> FactionPolicySpecManager.getSpec(x).getIncompatiblePolicyIds().contains(policy.getSpec().getId()));
    }

    public static String getStringType(TimelineEventType type) {
        String str = type.toString().toLowerCase().replace('_', ' ');
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static Color getColorForEvent(TimelineEventType type) {
        if (type == TimelineEventType.UNIQUE) {
            return new Color(107, 60, 143);
        }
        if (type == TimelineEventType.MILITARY) {
            return new Color(137, 68, 59);

        }
        if (type == TimelineEventType.RESEARCH_AND_EXPLORATION) {
            return new Color(62, 131, 191);

        }
        if (type == TimelineEventType.PROSPERITY) {
            return new Color(194, 152, 57);

        }
        return null;
    }

    public static String getFactionName(FactionAPI faction) {

        return faction.getDisplayName().substring(0, 1).toUpperCase() + faction.getDisplayName().substring(1);
    }

    public static List<MarketAPI> getMarketsUnderPlayer() {
        return Misc.getFactionMarkets(Factions.PLAYER).stream().filter(x -> !x.hasTag("nex_playerOutpost")).toList();
    }

    public static ArrayList<FactionAPI> getAllFactionsRelevant() {
        return new ArrayList<>(Global.getSector().getAllFactions().stream()
                .filter(x -> (x.isShowInIntelTab() || x.isPlayerFaction()) && AoTDFactionManager.getTotalSize(x) > 0).sorted((a, b) -> Integer.compare((int) AoTDFactionManager.getTotalSize(b), (int) AoTDFactionManager.getTotalSize(a))) // sort descending
                .toList());
    }

    public static List<MarketAPI> getAllMarkets() {
        return Global.getSector().getEconomy().getMarketsCopy().stream().filter(x -> !x.hasTag("nex_playerOutpost") && x.getFaction() != null && (x.getFaction().isShowInIntelTab()||x.getFaction().isPlayerFaction()) && x.isInEconomy()).toList();
    }

    public static boolean doesHaveMonopolyOverCommodities(int threshold, String... commodities) {

        return Arrays.stream(commodities).allMatch(x ->getMarketSharePercentage(x,Global.getSector().getPlayerFaction()) >= threshold);
    }

    public static int getMarketSharePercentage(String commodityID,FactionAPI faction) {
        MarketAPI market = Global.getSector().getEconomy().getMarketsCopy().get(0);
        return market.getCommodityData(commodityID).getCommodityMarketData().getMarketSharePercent(faction);
    }
    public static HashSet<String> getAllCommoditiesInCirculation(){
        HashSet<String>strs =new HashSet<>();
        MarketAPI market = Global.getSector().getEconomy().getMarketsCopy().get(0);

        Global.getSettings().getAllCommoditySpecs().stream().filter(x->market.getCommodityData(x.getId()).getCommodityMarketData().getMarketValue()!=0).forEach(x->strs.add(x.getId()));
        return strs;
    }
    public static LinkedHashSet<String> getAllCommoditiesInCirculationSorted(FactionAPI faction, boolean descending) {
        MarketAPI market = Global.getSector().getEconomy().getMarketsCopy().get(0); // Adjust this if you want to consider multiple markets
        Comparator<String> comparator = Comparator.comparingDouble(
                id -> market.getCommodityData(id).getCommodityMarketData().getMarketSharePercent(faction)
        );

        if (descending) {
            comparator = comparator.reversed();
        }

        return Global.getSettings().getAllCommoditySpecs().stream()
                .map(CommoditySpecAPI::getId)
                .filter(id -> market.getCommodityData(id).getCommodityMarketData().getMarketValue() != 0)
                .sorted(comparator)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static int getProfitsFromCommodityAcrossColonies(String commodityID,FactionAPI faction) {
        int profit = 0;
        for (MarketAPI marketAPI : getMarketsForFaction(faction)) {
            profit+=marketAPI.getCommodityData(commodityID).getExportIncome();
        }
        return profit;


    }
    public static int getTotalMarketValue(String commodityID) {
        MarketAPI market = Global.getSector().getEconomy().getMarketsCopy().get(0); // Adjust this if you want to consider multiple markets
        return (int) market.getCommodityData(commodityID).getCommodityMarketData().getMarketValue();


    }
    public static LinkedHashMap<FactionAPI, Integer> getMarketSharePercentageForEachFactionSorted(String commodityId) {
        MarketAPI market = Global.getSector().getEconomy().getMarketsCopy().get(0);

        Map<FactionAPI, Integer> original = market.getCommodityData(commodityId)
                .getCommodityMarketData()
                .getMarketSharePercentPerFaction();

        return original.entrySet().stream()
                .sorted(Map.Entry.<FactionAPI, Integer>comparingByValue().reversed()) // Sort by value descending
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // Preserve order
                ));
    }


    public static List<MarketAPI> getMarketsForFaction(FactionAPI faction) {
        List<MarketAPI> markets;
        if(faction.isPlayerFaction()){
            markets = getMarketsUnderPlayer();
        }
        else{
            markets = Misc.getFactionMarkets(faction);
        }
        return markets;
    }
}
