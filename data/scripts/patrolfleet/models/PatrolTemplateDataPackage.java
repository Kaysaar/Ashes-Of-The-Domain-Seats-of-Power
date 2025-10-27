package data.scripts.patrolfleet.models;

public class PatrolTemplateDataPackage {
    public String name,data,modsReq;
    public PatrolTemplateDataPackage(String name, String data, String modsReq) {
        this.name = name;
        this.data = data;
        this.modsReq = modsReq;
    }

    @Override
    public String toString() {
        return name+","+data+","+modsReq;
    }
}
