package data.scripts.patrolfleet.models;

import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import data.industry.AoTDMilitaryBase;
import data.scripts.patrolfleet.utilis.FleetPointUtilis;
import data.scripts.patrolfleet.utilis.TemplateUtilis;

import java.util.LinkedHashMap;

public class BasePatrolFleet extends BasePatrolFleetTemplate {
    MarketAPI tiedTo;
    String nameOfFleet;
    String id;
    boolean inTransit;
    float daysLeftForTransit;
    boolean decomisioned;
    public BasePatrolFleet(BasePatrolFleetTemplate template) {
        super(new LinkedHashMap<>(template.assignedShipsThatShouldSpawn),template.nameOfTemplate);
        this.id = Misc.genUID();
    }

    public BasePatrolFleet(LinkedHashMap<String,Integer>ships,String nameOfFleet){
        super(ships,nameOfFleet);
        this.id = Misc.genUID();
    }
    public int getFPTaken(){
        return (int) FleetPointUtilis.getFPOfAllShipsInFleet(assignedShipsThatShouldSpawn);
    }
    public void setFleetName(String nameOfFleet) {
        this.nameOfFleet = nameOfFleet;
    }
    public String getCurrentStatus(){
        if(decomisioned){
            return "In process of de-commission";
        }
        if(tiedTo==null){
            return "On stand-by, ready to be assigned";
        }
        else{
            if(inTransit){
                return "In Transit : "+ AshMisc.convertDaysToString((int) daysLeftForTransit);
            }
            else{
                if(AoTDMilitaryBase.isPatroling(id,tiedTo)){
                    return "Currently on Patrol Duty";
                }
                else{
                    return "In-preparation for patrol duty";
                }
            }
        }

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
