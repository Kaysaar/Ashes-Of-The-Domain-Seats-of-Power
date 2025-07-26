package data.route;

import com.fs.starfarer.api.Script;

public class SurveyFleetFinisher implements Script {
    NovaExplorariaExpeditionFleetRouteManager manager;
    public SurveyFleetFinisher(NovaExplorariaExpeditionFleetRouteManager manager){
        this.manager = manager;
    }
    @Override
    public void run() {
        manager.setReturning(true);
    }
}
