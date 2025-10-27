package data.ui.patrolfleet.overview.fleetview;

import ashlib.data.plugins.info.ShipInfoGenerator;
import ashlib.data.plugins.ui.models.CustomButton;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.models.BasePatrolFleet;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FleetButtonComponent extends CustomButton {
    public static LinkedHashMap<ShipAPI.HullSize,Float>buttonScale = new LinkedHashMap<>();
    static {
        buttonScale.put(ShipAPI.HullSize.FRIGATE,0.7f);
        buttonScale.put(ShipAPI.HullSize.DESTROYER,0.85f);
        buttonScale.put(ShipAPI.HullSize.CRUISER,0.95f);
        buttonScale.put(ShipAPI.HullSize.CAPITAL_SHIP,1f);
    }
    public boolean templateButton = false;
    public FleetButtonComponent(float width, float height, BasePatrolFleet buttonData,boolean templateButton) {
        super(width, height, buttonData, 0f, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor());
        this.templateButton = templateButton;
    }

    @Override
    public void createButtonContent(TooltipMakerAPI tooltip) {
        float maxHeight = height-10;
        float y = 5;
        BasePatrolFleet data = getData();
        LabelAPI label =  tooltip.addPara(data.getNameOfFleet(),0f);
        float widthOfLabel = label.computeTextWidth(label.getText());
        float textHeight = label.computeTextHeight(label.getText());
        float scale = widthOfLabel/230;
        if(scale<=1){
            scale =1;
        }
        label.autoSizeToWidth(230);
        label.getPosition().inTL(10,(height/2)-((scale*textHeight)/2));

        float startX =0;
        float startXPerm =  235;
        float seperator = -7f;
        if(templateButton) {

            float available = width-startXPerm;
            CustomPanelAPI custom = Global.getSettings().createCustom(available,maxHeight,null);
            TooltipMakerAPI customTooltip = custom.createUIElement(custom.getPosition().getWidth(),custom.getPosition().getHeight(),false);
            LinkedHashMap<String,Integer>ships =new LinkedHashMap<>(data.assignedShipsThatShouldSpawn);
            sortShipsByFPDescInPlace(ships);
            for (Map.Entry<String, Integer> e : ships.entrySet()) {
                boolean reachedDest = false;
                for (int i = 0; i < e.getValue(); i++) {
                    ShipHullSpecAPI spec = Global.getSettings().getHullSpec(e.getKey());
                    int boxSize = (int) Math.floor(maxHeight*buttonScale.get(spec.getHullSize()));
                    if(startX+boxSize>=available){
                        LabelAPI l =  tooltip.addPara(". . .",Color.ORANGE,0f);
                        l.getPosition().inTL(available+startXPerm+5,height/2-(l.computeTextHeight(l.getText())/2));
                        reachedDest =true;
                        break;
                    }
                    CustomPanelAPI ship = ShipInfoGenerator.getShipImage(spec,boxSize,null).one;
                    customTooltip.addCustom(ship,0f).getPosition().inTL(startX,(maxHeight-boxSize)/2);
                    startX+=boxSize+seperator;
                }
                if(reachedDest){
                    break;
                }
                ;
            }
            float rest = available-startX;
            if(rest<=0)rest=0;
            custom.addUIElement(customTooltip).inTL(-5,0);
            tooltip.addCustom(custom,0f).getPosition().inTL(startXPerm+(rest/2),y);

        }
        else{
            float available = width-startXPerm-400;
            float statusPositionStart = width-300;
            float pointsFP = width-100;
            CustomPanelAPI custom = Global.getSettings().createCustom(available,maxHeight,null);
            TooltipMakerAPI customTooltip = custom.createUIElement(custom.getPosition().getWidth(),custom.getPosition().getHeight(),false);
            LinkedHashMap<String,Integer>ships =new LinkedHashMap<>(data.assignedShipsThatShouldSpawn);
            sortShipsByFPDescInPlace(ships);
            for (Map.Entry<String, Integer> e : ships.entrySet()) {
                boolean reachedDest = false;
                for (int i = 0; i < e.getValue(); i++) {
                    ShipHullSpecAPI spec = Global.getSettings().getHullSpec(e.getKey());
                    int boxSize = (int) Math.floor(maxHeight*buttonScale.get(spec.getHullSize()));
                    if(startX+boxSize>=available){
                        LabelAPI l =  tooltip.addPara(". . .",Color.ORANGE,0f);
                        l.getPosition().inTL(available+startXPerm+5,height/2-(l.computeTextHeight(l.getText())/2));
                        reachedDest =true;
                        break;
                    }
                    CustomPanelAPI ship = ShipInfoGenerator.getShipImage(spec,boxSize,null).one;
                    customTooltip.addCustom(ship,0f).getPosition().inTL(startX,(maxHeight-boxSize)/2);
                    startX+=boxSize+seperator;
                }
                if(reachedDest){
                    break;
                }
                ;
            }
            float rest = available-startX;
            if(rest<=0)rest=0;
            custom.addUIElement(customTooltip).inTL(-5,0);
            tooltip.addCustom(custom,0f).getPosition().inTL(startXPerm+(rest/2),y);
            LabelAPI status = tooltip.addPara(data.getCurrentStatus(),Misc.getTooltipTitleAndLightHighlightColor(),0f);
            status.autoSizeToWidth(200);
            status.getPosition().inTL(statusPositionStart+100-(status.computeTextWidth(status.getText())/2),height/2-(status.computeTextHeight(status.getText())/2));

            status = tooltip.addPara(""+data.getFPTaken(),Color.ORANGE,0f);
            status.autoSizeToWidth(100);
            status.getPosition().inTL(pointsFP+50-(status.computeTextWidth(status.getText())/2),height/2-(status.computeTextHeight(status.getText())/2));
        }


    }
    public BasePatrolFleet getData(){return (BasePatrolFleet) buttonData;
    }

    @Override
    public void clearUI() {

    }

    public void sortShipsByFPDescInPlace(LinkedHashMap<String,Integer>ships) {
        if (ships == null || ships.isEmpty()) return;

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(ships.entrySet());

        entries.sort((e1, e2) -> {
            // 1) Hull size priority (Capital -> ... -> Fighter -> Default/unknown)
            int r1 = hullRank(getHullSize(e1.getKey()));
            int r2 = hullRank(getHullSize(e2.getKey()));
            if (r1 != r2) return Integer.compare(r1, r2);

            // 2) FP descending within the same hull size
            float fp1 = getHullFP(e1.getKey());
            float fp2 = getHullFP(e2.getKey());
            int cmp = Float.compare(fp2, fp1);
            if (cmp != 0) return cmp;

            // 3) Stable tie-breaker
            return e1.getKey().compareTo(e2.getKey());
        });

        LinkedHashMap<String, Integer> reordered = new LinkedHashMap<>(entries.size());
        for (Map.Entry<String, Integer> e : entries) {
            reordered.put(e.getKey(), e.getValue());
        }
        ships.clear();
        ships.putAll(reordered);
    }
    public float getHullFP(String hullId) {
        try {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            return spec != null ? spec.getFleetPoints() : 0f;
        } catch (Throwable t) {
            return 0f;
        }
    }
    public ShipAPI.HullSize getHullSize(String hullId) {
        try {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            return spec != null ? spec.getHullSize() : ShipAPI.HullSize.FRIGATE;
        } catch (Throwable t) {
            return ShipAPI.HullSize.DEFAULT;
        }
    }
    // Make sure you have: import com.fs.starfarer.api.combat.ShipAPI;
    private int hullRank(ShipAPI.HullSize size) {
        if (size == null) return 5;
        switch (size) {
            case CAPITAL_SHIP:
                return 0;
            case CRUISER:
                return 1;
            case DESTROYER:
                return 2;
            case FRIGATE:
                return 3;
            case FIGHTER:
                return 4;
            case DEFAULT:
            default:
                return 5;
        }
    }
}
