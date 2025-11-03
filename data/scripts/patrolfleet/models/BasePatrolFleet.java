package data.scripts.patrolfleet.models;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;
import data.scripts.patrolfleet.managers.AoTDFactionPatrolsManager;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;

import java.util.*;

public class BasePatrolFleet extends BasePatrolFleetTemplate {
    public static float DAYS_PER_FP = 5f; //5fp -> 1 day
    MarketAPI tiedTo;
    String nameOfFleet;
    String id;
    boolean inTransit;
    boolean startedProcessOfDecom;
    boolean shouldRemove = false;
    float days = 0f;
    float daysStorage =0;
    boolean decomisioned;
    boolean forcedTransitDays = false;
    boolean grounded = false;
    FleetFactory.PatrolType patrolType;

    public FleetFactory.PatrolType getPatrolType() {
        if(patrolType==null)setPatrolType(FleetFactory.PatrolType.COMBAT);
        return patrolType;
    }

    public void setPatrolType(FleetFactory.PatrolType patrolType) {
        this.patrolType = patrolType;
    }
    public static String getRole(FleetFactory.PatrolType patrolType){
        if(patrolType!=null){
            if(patrolType.equals(FleetFactory.PatrolType.FAST)){
                return "Recon";
            }
            if(patrolType.equals(FleetFactory.PatrolType.COMBAT)){
                return "Homeguard";
            }
            if(patrolType.equals(FleetFactory.PatrolType.HEAVY)){
                return "System defence";
            }
        }
        return "";
    }


    public void clear(){
        getShipsInDecomFleet().clear();
        getShipsForReplacementWhenInPrep().clear();
        assignedShipsThatShouldSpawn.clear();
        data.clear();
    }
    public boolean shouldRemove(){
        return shouldRemove;
    }

    LinkedHashMap<String,Integer>shipsForReplacementWhenInPrep = new LinkedHashMap<>();
    LinkedHashMap<String,Integer>shipsInDecomFleet = new LinkedHashMap<>();

    public LinkedHashMap<String, Integer> getShipsInDecomFleet() {
        return shipsInDecomFleet;
    }

    public boolean isStartedProcessOfDecom() {
        return startedProcessOfDecom;
    }

    public void startProcessOfDecom(float daysTillDecom) {
        startedProcessOfDecom = true;
        daysStorage = daysTillDecom;
        if(shipsInDecomFleet==null)shipsInDecomFleet = new LinkedHashMap<>();
        shipsInDecomFleet.putAll(assignedShipsThatShouldSpawn);
        days=0;
    }
    public void forceTransitDays(){
        forcedTransitDays = true;
    }
    // Call this inside your class
    private int removeShipsByFpGreedySmallestFirst(LinkedHashMap<String, Integer> ships, int fpToRemove) {
        if (ships == null || ships.isEmpty() || fpToRemove <= 0) return 0;

        // Build a working list: [hullId, perShipFP, count], skip 0-FP hulls (civilians, etc.)
        class E { String id; int fp; int count; }
        List<E> list = new ArrayList<>();
        for (Map.Entry<String, Integer> e : ships.entrySet()) {
            String id = e.getKey();
            int count = Math.max(0, e.getValue());
            if (count == 0) continue;
            int fp = Math.max(0, Math.round(FleetPointUtilis.getHullFP(id)));
            if (fp == 0) continue; // ignore 0-FP hulls
            E en = new E();
            en.id = id; en.fp = fp; en.count = count;
            list.add(en);
        }
        if (list.isEmpty()) return 0;

        // Sort by per-ship FP ascending; tie-break by hull id for stability
        list.sort(Comparator.comparingInt((E en) -> en.fp).thenComparing(en -> en.id));

        int remaining = fpToRemove;
        int removedFp = 0;

        // Greedy: take as many of the smallest that fit without overshoot
        for (E en : list) {
            if (remaining <= 0) break;
            if (en.fp > remaining) continue;
            int maxFit = remaining / en.fp;      // how many of this hull we can take
            int take = Math.min(en.count, maxFit);
            if (take <= 0) continue;
            en.count -= take;
            int fp = take * en.fp;
            removedFp += fp;
            remaining -= fp;
        }

        // Optional second pass to catch cases where after reducing remaining,
        // some slightly larger-but-still-fitting entries now fit (still no overshoot).
        if (remaining > 0) {
            for (E en : list) {
                if (remaining <= 0) break;
                if (en.count <= 0) continue;
                if (en.fp > remaining) continue;
                int maxFit = remaining / en.fp;
                int take = Math.min(en.count, maxFit);
                if (take <= 0) continue;
                en.count -= take;
                int fp = take * en.fp;
                removedFp += fp;
                remaining -= fp;
            }
        }

        // Write back mutations to the original map
        for (E en : list) {
            int original = ships.getOrDefault(en.id, 0);
            int removedCount = original - en.count; // how many we took
            if (removedCount <= 0) continue;
            int newCount = original - removedCount;
            if (newCount > 0) ships.put(en.id, newCount);
            else ships.remove(en.id);
        }

        return removedFp; // if you care to log/report what was actually removed
    }

    public void advance(float amount){
        if(tiedTo.getFaction()==null||!tiedTo.getFaction().isPlayerFaction()){
            shouldRemove = true;
            return;
        }
        if(isDecomisioned()&&!AoTDMilitaryBase.isPatroling(id,getTiedTo())&&!startedProcessOfDecom){
            float days = getFPTaken()/ AoTDFactionPatrolsManager.getInstance().getDaysPerFP().getModifiedValue();
            startProcessOfDecom(days);
            return;
        }
        if(isInTransit()||isStartedProcessOfDecom()){
            days+= Global.getSector().getClock().convertToDays(amount);
            int fpPointsSupposued = getFpTakenWithoutDecomDuringDecom();
            int current = geTotalFpTaken();
            int alreadyConsumed = fpPointsSupposued-current;
            if(alreadyConsumed>0){
                removeShipsByFpGreedySmallestFirst(shipsInDecomFleet,alreadyConsumed);
            }
            if(days>=daysStorage){
                if(isInTransit()&&forcedTransitDays){
                    forcedTransitDays = false;
                    setInTransit(false);
                    return;
                }
                if(isDecomisioned()&&isStartedProcessOfDecom()){
                    shouldRemove = true;
                    daysStorage=0;
                    shipsInDecomFleet.clear();
                    days =0;
                }
            }
        }


    }

    public int getFpTakenWithoutDecomDuringDecom(){
        return Math.round(FleetPointUtilis.getFPOfAllShipsInFleet(shipsInDecomFleet));
    }
    public float getPercentageOfDecomProgress(){
        if(!isStartedProcessOfDecom()){
            return 0f;
        }
        if(daysStorage==0)return 1f;
        return days/daysStorage;
    }
    public boolean isInTransit() {
        return inTransit;
    }

    public void setInTransit(boolean inTransit) {
        if(!inTransit){
            days =0;
            daysStorage =0;
        }
        this.inTransit = inTransit;
    }
    public void setInTransit(boolean inTransit,float daysSupposedForTransit) {
        if(!inTransit){
            days =0;
            daysStorage =0;
        }
        else{
            days = 0;
            daysStorage = daysSupposedForTransit;
        }
        this.inTransit = inTransit;
    }


    public LinkedHashMap<String, Integer> getShipsForReplacementWhenInPrep() {
        if(shipsForReplacementWhenInPrep==null)shipsForReplacementWhenInPrep = new LinkedHashMap<>();
        return shipsForReplacementWhenInPrep;
    }

    public void performReplacement(){
        if(!getShipsForReplacementWhenInPrep().isEmpty()){
            assignedShipsThatShouldSpawn.clear();
            assignedShipsThatShouldSpawn.putAll(getShipsForReplacementWhenInPrep());
            getShipsForReplacementWhenInPrep().clear();
        }
    }

    public void setDecomisioned(boolean decomisioned) {
        this.decomisioned = decomisioned;
    }

    public boolean isDecomisioned() {
        return decomisioned;
    }

    public BasePatrolFleet(BasePatrolFleetTemplate template) {
        super(new LinkedHashMap<>(template.assignedShipsThatShouldSpawn),template.nameOfTemplate);
        this.id = Misc.genUID();
    }

    public BasePatrolFleet(LinkedHashMap<String,Integer>ships,String nameOfFleet){
        super(ships,nameOfFleet);
        this.id = Misc.genUID();
    }
    public int getFPTaken(){
        int points = Math.round(FleetPointUtilis.getFPOfAllShipsInFleet(assignedShipsThatShouldSpawn));
        return points - Math.round(getPercentageOfDecomProgress()*points);
    }
    public int getFPTakenIgnoreDecom(){
        return Math.round(FleetPointUtilis.getFPOfAllShipsInFleet(assignedShipsThatShouldSpawn));
    }
    public int geTotalFpTaken(){
        if(getFPTakenByReplacement()==0){
            return getFPTaken();
        }
        int diff = getFPTaken()-getFPTakenByReplacement();
        return getFPTaken()-diff;
    }
    public boolean isValidToSpawn(){
        return !isInTransit()&&!isDecomisioned()&&!AoTDMilitaryBase.isPatroling(id,tiedTo)&&!isGrounded();
    }

    public int getFPTakenByReplacement(){
        return (int) FleetPointUtilis.getFPOfAllShipsInFleet(getShipsForReplacementWhenInPrep());
    }
    public void setFleetName(String nameOfFleet) {
        this.nameOfFleet = nameOfFleet;
    }
    public String getCurrentStatus(){
        if(decomisioned){
            if(startedProcessOfDecom){
                return "In process of de-commission";
            }
            return "Preparing for de-commission";
        }
        if(tiedTo==null){
            return "On stand-by";
        }
        else{
            if(inTransit){
                return "In Transit to "+tiedTo.getName();
            }
            else{
                boolean hasInd = false;
                for (Industry industry : tiedTo.getIndustries()) {
                    if(AoTDMilitaryBase.industriesValidForBase.contains(industry.getSpec().getId())){
                        hasInd = true;
                        break;
                    }
                }
                if(grounded){
                    if(AoTDMilitaryBase.isPatroling(id,tiedTo)){
                        return "Returning to Base";
                    }
                    return "Grounded! Lack of FP";
                }
                if(!hasInd){
                    return "Grounded! Build necessary structure!";
                }
                if(AoTDMilitaryBase.isPatroling(id,tiedTo)){
                    return "Currently on Patrol Duty";
                }
                else{
                    return "Preparing for Patrol";
                }
            }
        }

    }

    public boolean isGrounded() {
        return grounded;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public float getDaysTillSomething(){
        return daysStorage - days;
    }
    public boolean isTiedToMarket(MarketAPI tiedTo){
        if(this.tiedTo==null)return false;
        return this.tiedTo.getId().equals(tiedTo.getId());
    }

    public String getNameOfFleet() {
        return nameOfFleet;
    }

    public String getId() {
        return id;
    }

    public MarketAPI getTiedTo() {
        return tiedTo;
    }

    public void setTiedTo(MarketAPI tiedTo) {
        this.tiedTo = tiedTo;
    }
}
