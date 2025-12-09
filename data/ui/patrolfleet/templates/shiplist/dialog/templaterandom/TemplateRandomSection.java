package data.ui.patrolfleet.templates.shiplist.dialog.templaterandom;

import ashlib.data.plugins.info.ShipInfoGenerator;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.ui.patrolfleet.templates.filter.TemplateManufactureFilter;
import data.ui.patrolfleet.templates.shiplist.components.ShipPanelData;
import data.ui.patrolfleet.templates.shiplist.components.ShipUIData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class TemplateRandomSection implements ExtendedUIPanelPlugin {
   public TemplateManufactureFilter filter;
    public ShipTypeCounter frig,dest,cru,cap;
    ButtonAPI autoGenerate;
    CustomPanelAPI mainPanel,contentPanel;

    public TemplateRandomSection(float width,float height){
        mainPanel = Global.getSettings().createCustom(width,height,this);
        createUI();
    }
    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }
    public boolean reqGeneration =false;
    @Override
    public void createUI() {
        if(contentPanel!=null){
            mainPanel.removeComponent(contentPanel);
        }
        contentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight(),null);
        TooltipMakerAPI tooltip = contentPanel.createUIElement(contentPanel.getPosition().getWidth(),contentPanel.getPosition().getHeight(),false);
        float width = contentPanel.getPosition().getWidth();
        filter = new TemplateManufactureFilter(width,350){
            @Override
            public void pruneMap(LinkedHashMap<String, Integer> map) {
                map.clear();
                map.putAll(ShipPanelData.getShipManInfo());
            }
        };
        filter.setHeaderOverride("Choose manufactures to pull from");
        filter.createUI();
        tooltip.addCustom(filter.getMainPanel(),1f);
        CustomPanelAPI row = Global.getSettings().createCustom(width,20,null);
        float spaceTakenByOne =178;
        float seperatorX = 10;
        float increment = spaceTakenByOne+seperatorX;
        frig = new ShipTypeCounter(32, ShipAPI.HullSize.FRIGATE);
        dest = new ShipTypeCounter(32, ShipAPI.HullSize.DESTROYER);
        cru = new ShipTypeCounter(32, ShipAPI.HullSize.CRUISER);
        cap = new ShipTypeCounter(32, ShipAPI.HullSize.CAPITAL_SHIP);
        float spaceTaken = spaceTakenByOne*4+(seperatorX*3);
        float startX = width/2 - (spaceTaken/2);
        row.addComponent(cap.getMainPanel()).inTL(startX,0);
        row.addComponent(cru.getMainPanel()).inTL(startX+increment,0);
        row.addComponent(dest.getMainPanel()).inTL(startX+(increment*2),0);
        row.addComponent(frig.getMainPanel()).inTL(startX+(increment*3),0);
        tooltip.setParaFont(Fonts.ORBITRON_20AA);
        tooltip.addPara("Choose ships in fleet", Misc.getTooltipTitleAndLightHighlightColor(),5f).setAlignment(Alignment.MID);
        tooltip.addCustom(row,25f);
        row = Global.getSettings().createCustom(width,30,null);
        TooltipMakerAPI tooltipInDenial = row.createUIElement(width,30,false);
        autoGenerate = tooltipInDenial.addButton("Auto-generate",null,Misc.getBasePlayerColor(),Misc.getDarkPlayerColor(),Alignment.MID,CutStyle.TL_BR,width/4,30,0f);
        row.addUIElement(tooltipInDenial).inTL(width/2-(width/8),0);
        tooltip.addCustom(row,10f);
        contentPanel.addUIElement(tooltip).inTL(0,0);
        mainPanel.addComponent(contentPanel).inTL(0,0);
    }

    @Override
    public void clearUI() {

    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {
        if(autoGenerate!=null&&autoGenerate.isChecked()){
            autoGenerate.setChecked(false);
            reqGeneration = true;
        }
    }
    public LinkedHashMap<String, Integer> generateData() {
        reqGeneration = false;

        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        Random rand = Misc.random;

        int amountFrig = frig.current;
        int amountDest = dest.current;
        int amountCru  = cru.current;
        int amountCap  = cap.current;

        // Base pool
        List<ShipHullSpecAPI> specs = ShipPanelData.getLearnedShipPackages(false).stream().filter(x->!x.getHints().contains(ShipHullSpecAPI.ShipTypeHints.CIVILIAN)).toList();

        // Optional manufacturer filter
        if (!filter.chosenManu.isEmpty()) {
            // Assumes ShipPanelData.arrayContains(Collection<String> haystack, String needle)
            specs = specs.stream()
                    .filter(x -> ShipPanelData.arrayContains(filter.chosenManu, x.getManufacturer()))
                    .toList();
        }

        // Partition by hull size
        List<ShipHullSpecAPI> filteredFrig = specs.stream()
                .filter(x -> x.getHullSize() == ShipAPI.HullSize.FRIGATE)
                .toList();

        List<ShipHullSpecAPI> filteredDest = specs.stream()
                .filter(x -> x.getHullSize() == ShipAPI.HullSize.DESTROYER)
                .toList();

        List<ShipHullSpecAPI> filteredCru = specs.stream()
                .filter(x -> x.getHullSize() == ShipAPI.HullSize.CRUISER)
                .toList();

        List<ShipHullSpecAPI> filteredCap = specs.stream()
                .filter(x -> x.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP)
                .toList();

        // Fill map; order will be FRIGATE -> DESTROYER -> CRUISER -> CAPITAL
        addRandom(map, filteredFrig, amountFrig, rand);
        addRandom(map, filteredDest, amountDest, rand);
        addRandom(map, filteredCru,  amountCru,  rand);
        addRandom(map, filteredCap,  amountCap,  rand);

        return map;
    }

    private static void addRandom(LinkedHashMap<String, Integer> out,
                                  List<ShipHullSpecAPI> pool,
                                  int count,
                                  Random rand) {
        if (pool == null || pool.isEmpty() || count <= 0) return;

        // .toList() from streams can be unmodifiable; avoid structural ops on 'pool'
        int n = pool.size();
        for (int i = 0; i < count; i++) {
            ShipHullSpecAPI pick = pool.get(rand.nextInt(n));
            String hullId = pick.getHullId(); // or pick.getHullName() if you want display names
            out.merge(hullId, 1, Integer::sum);
        }
    }


    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
