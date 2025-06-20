package data.scripts.models;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.intel.EventOccuredIntel;
import data.scripts.managers.AoTDFactionManager;

import java.util.ArrayList;

public class BaseFactionTimelineEvent {
    public static int multiplier = 3;
    public int cycle;
    public int day;
    public int month;
    public boolean gotReward = false;
    public String entityId;
   public String lastSavedName;
    public int romanNumeral;

    public String getEntityId() {
        return entityId;
    }
    public String getName(){
        return lastSavedName;
    }

    public void setDate(int cycle, int day, int month) {
        this.cycle = cycle;
        this.day = day;
        this.month = month;
    }

    public void setRomanNumeral(int romanNumeral) {
        this.romanNumeral = romanNumeral;
    }

    public void initEntityMemory(String entityId) {
        this.entityId = entityId;
    }

    public String getTitleOfEvent() {
        return "";
    }

    public String getImagePath() {
        return Global.getSettings().getIndustrySpec(Industries.POPULATION).getImageName();
    }

    public void grantReward() {
        if (!gotReward) {
            gotReward = true;
            executeReward();
        }
    }

    public void executeReward() {


    }
    public  String  getRomanNumeral(){
        return toRoman(romanNumeral);
    }
    public void createDetailedTooltipOnHover(TooltipMakerAPI tooltip) {
    }

    public void createPointSection(TooltipMakerAPI tooltip) {
        if (getEventType() == TimelineEventType.UNIQUE) {
            tooltip.addPara("Gain %s points towards all faction goals!", 5f, AoTDFactionManager.getColorForEvent(getEventType()).brighter(), BaseFactionTimelineEvent.multiplier*getPointsForGoal() + "");
        } else {
            tooltip.addPara("Gain %s points towards %s", 5f, AoTDFactionManager.getColorForEvent(getEventType()).brighter(), BaseFactionTimelineEvent.multiplier*getPointsForGoal() + "", AoTDFactionManager.getStringType(getEventType()));

        }
    }

    public void createSmallNoteForEvent(TooltipMakerAPI tooltip) {

    }

    public ArrayList<MutableStat> getEventsAffected() {
        ArrayList<MutableStat> existing = new ArrayList<>();
        if (getEventType() == TimelineEventType.UNIQUE) {
            existing.addAll(AoTDFactionManager.getInstance().getGoalStats().values().stream().toList());
        }
        else{
            existing.add(AoTDFactionManager.getInstance().getGoalStat(getEventType()));
        }

        return existing;
    }
    public void applyEffects(ArrayList<MutableStat> goalStats){
        goalStats.forEach(x->x.modifyFlat(getID(),getPointsForGoal()*multiplier));
    }
    public String getID(){
        return this.getClass().getSimpleName();
    }

    public TimelineEventType getEventType() {
        return TimelineEventType.RESEARCH_AND_EXPLORATION;
    }

    public void createIntelEntryForUnlocking() {
        EventOccuredIntel intel = new EventOccuredIntel(this);
        Global.getSector().getIntelManager().addIntel(intel);
        intel.endAfterDelay(5f);
        AoTDFactionManager.getInstance().addXP((int) (BaseFactionTimelineEvent.multiplier*getPointsForGoal()*10));
    }

    public int getCycle() {
        return cycle;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public void updateDataUponEntryOfUI() {
        //This func is called for example when you change market name or anything that requires name
    }

    public int getPointsForGoal() {
        return 30;
    }
    protected String toRoman(int number) {
        String[] romans = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
        int[] values = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                result.append(romans[i]);
            }
        }
        return result.toString();
    }
    public boolean checkForCondition(){
        return false;
    }

}
