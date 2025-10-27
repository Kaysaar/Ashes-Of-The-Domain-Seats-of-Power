package data.ui.patrolfleet.templates.filter;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;
import data.scripts.patrolfleet.models.PatrolShipData;
import data.scripts.patrolfleet.managers.PatrolTemplateManager;
import data.ui.patrolfleet.templates.TemplatePanel;
import data.ui.patrolfleet.templates.components.TemplateSearchBarComparator;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateFilterPanel implements ExtendedUIPanelPlugin {
    public CustomPanelAPI mainPanel, componentPanel;
    TextFieldAPI searchBar;
    TemplateManufactureFilter filterManu;
    String prevText;
    public ButtonAPI createNew, createCopy,missingModsChk,unlearnedChk;
    boolean showIncompat = false;
    boolean showUnavailable = false;
    public TemplateFilterPanel(float width, float height) {
        mainPanel = Global.getSettings().createCustom(width, height, this);

    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    @Override
    public void createUI() {
        ArrayList<String >validToHighlight = new ArrayList<>();
        if (componentPanel != null) {
            mainPanel.removeComponent(componentPanel);
        }
        UILinesRenderer renderer = new UILinesRenderer(0f);
        componentPanel = Global.getSettings().createCustom(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), renderer);
        renderer.setPanel(componentPanel);

        TooltipMakerAPI test = componentPanel.createUIElement(componentPanel.getPosition().getWidth(), componentPanel.getPosition().getHeight() - 425, false);
        test.setParaFont(Fonts.ORBITRON_24AA);
        float tWidth = componentPanel.getPosition().getWidth();
        createNew = test.addButton("Create new template", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, tWidth - 15, 35, 5f);
        createCopy = test.addButton("Create template from copy", null, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, tWidth - 15, 35, 10f);
        test.addPara("Filters", Misc.getTooltipTitleAndLightHighlightColor(), 6f).setAlignment(Alignment.MID);
        searchBar = test.addTextField(componentPanel.getPosition().getWidth() - 10, 25, Fonts.ORBITRON_20AA, 5f);
        searchBar.setText(prevText);
         missingModsChk = test.addCheckbox(
                200f, 20f,
                "Show invalid templates",
                null,
                ButtonAPI.UICheckboxSize.SMALL,
                10f
        );

// 2) Templates unusable because faction hasn't learned all required ships/prints
         unlearnedChk = test.addCheckbox(
                200f, 20f,
                "Show locked templates",
                null,
                ButtonAPI.UICheckboxSize.SMALL,
                10f
        );
         unlearnedChk.setChecked(showUnavailable);
         missingModsChk.setChecked(showIncompat);
        test.setParaFont(Fonts.DEFAULT_SMALL);
        if(filterManu!=null){
            validToHighlight.addAll(filterManu.chosenManu);
        }
        filterManu = new TemplateManufactureFilter(mainPanel.getPosition().getWidth(), componentPanel.getPosition().getHeight() - test.getHeightSoFar() - 10){
            @Override
            public void pruneMap(LinkedHashMap<String, Integer> map) {
                if (map == null || map.isEmpty()) return;

                LinkedHashSet<String> manusPresent = new LinkedHashSet<>();
                for (BasePatrolFleetTemplate tmpl : PatrolTemplateManager.templates.values()) {
                    Collection<String> m = tmpl.getManufactures();
                    if (m != null) manusPresent.addAll(m);
                }

                // Drop all keys not present in manusPresent
                map.keySet().removeIf(k -> !manusPresent.contains(k)&&!k.equals("All designs"));
                validToHighlight.removeIf(k->!manusPresent.contains(k));
            }

            @Override
            public void onChange() {
                TemplatePanel.forceRequestUpdateListOnly =true;
            }
        };
        filterManu.chosenManu.addAll(validToHighlight);
        filterManu.createUI();
        test.addCustom(filterManu.getMainPanel(), 5f);

        componentPanel.addUIElement(test).inTL(0, 0);


        mainPanel.addComponent(componentPanel).inTL(0, 0);
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
        if (searchBar != null) {
            if (!searchBar.getText().equals(prevText)) {
                TemplatePanel.forceRequestUpdateListOnly = true;
                prevText = searchBar.getText();
            }
        }
        if(createCopy!=null){
            BasePatrolFleetTemplate tem = getTemplateFromClipBoard();

            if(tem!=null&&!createCopy.isEnabled()){
                createCopy.setEnabled(true);
            } else if (createCopy.isEnabled()&&tem==null) {
                createCopy.setEnabled(false);
            }

            if(createCopy.isChecked()){
                createCopy.setChecked(false);
                PatrolTemplateManager.templates.put(tem.getNameOfTemplate(),tem);
                PatrolTemplateManager.saveAllExistingTemplates();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(null), null);
                TemplatePanel.forceRequestUpdateListOnly = true;
                this.createUI();
            }
            if(missingModsChk!=null){
                if(missingModsChk.isChecked()){
                    if(!showIncompat){
                        showIncompat = true;
                        TemplatePanel.forceRequestUpdateListOnly = true;
                    }

                }
                else{
                    if(showIncompat){
                        showIncompat = false;
                        TemplatePanel.forceRequestUpdateListOnly = true;
                    }
                }
            }
            if(unlearnedChk!=null){
                if(unlearnedChk.isChecked()){
                    if(!showUnavailable){
                        showUnavailable = true;
                        TemplatePanel.forceRequestUpdateListOnly = true;
                    }

                }
                else{
                    if(showUnavailable){
                        showUnavailable = false;
                        TemplatePanel.forceRequestUpdateListOnly = true;
                    }
                }
            }
        }
    }

    private static BasePatrolFleetTemplate getTemplateFromClipBoard() {
        try {
            String res = readTextFromClipboard();
            if(AshMisc.isStringValid(res)){
                String[]splitted = res.split(",");
                if(splitted.length==3){
                    String name  = splitted[0];
                    String data = splitted[1];
                    String modsReq = splitted[2];

                    if (AshMisc.isStringValid(name)){
                        BasePatrolFleetTemplate template = new BasePatrolFleetTemplate();
                        template.nameOfTemplate = name;
                        ArrayList<String> entries = AshMisc.loadEntries(data ,";");
                        for (String entry : entries) {
                            String[] parts = entry.split(":");
                            PatrolShipData dataa = new PatrolShipData(parts[0]);
                            int amounts = Integer.parseInt(parts[1]);
                            template.assignedShipsThatShouldSpawn.put(parts[0], amounts);
                            template.data.put(parts[0], dataa);
                        }
                        ArrayList<String> entriesMod = AshMisc.loadEntries(modsReq, ";");
                        for (String string : entriesMod) {
                            String[] sp = string.split("<&>");
                            template.modsReq.put(sp[0],sp[1]);
                        }

                        return template;
                    }


                }
            }

        } catch (Exception e) {

        }
        return null;
    }

    public static String readTextFromClipboard() throws Exception {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);

        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return (String) contents.getTransferData(DataFlavor.stringFlavor);
        } else {
            return null;
        }
    }
    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
    public LinkedHashMap<String, BasePatrolFleetTemplate>getFilteredTemplates(){
        LinkedHashMap<String,BasePatrolFleetTemplate>templates = new LinkedHashMap<>();
        LinkedHashMap<String,BasePatrolFleetTemplate>survivors = new LinkedHashMap<>(PatrolTemplateManager.templates);

        for (BasePatrolFleetTemplate template : PatrolTemplateManager.templates.values()) {
            if(!template.isTemplateValidForModList()&&!showIncompat){
                survivors.remove(template.getNameOfTemplate());
            } else if (!template.doesKnowAllShips()&&!showUnavailable) {
                survivors.remove(template.getNameOfTemplate());
            }
        }
        if(!filterManu.chosenManu.isEmpty()){
            for (BasePatrolFleetTemplate value : PatrolTemplateManager.templates.values()) {
                LinkedHashSet<String>man = value.getManufactures();
                boolean found = false;
                for (String e : filterManu.chosenManu) {
                    if(man.contains(e)){
                        found = true; break;
                    }
                }
                if(!found){
                    survivors.remove(value.getNameOfTemplate());
                }
            }
        }


        if(AshMisc.isStringValid(prevText)){
            int threshold = 2; // Adjust the threshold based on your tolerance for misspellings
            TemplateSearchBarComparator comparator = new TemplateSearchBarComparator(prevText, threshold);
            survivors.values().stream().filter(x->comparator.isValid(x.getNameOfTemplate())).sorted(comparator).forEach(x->templates.put(x.getNameOfTemplate(),x));
            return templates;
        }

        return survivors.entrySet().stream()
                .sorted(new Comparator<Map.Entry<String, BasePatrolFleetTemplate>>() {
                    @Override
                    public int compare(Map.Entry<String, BasePatrolFleetTemplate> o1, Map.Entry<String, BasePatrolFleetTemplate> o2) {
                        return Integer.compare((int) o2.getValue().getTotalFleetPoints(), (int) o1.getValue().getTotalFleetPoints());
                    }
                }) // Sort by value descending
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // Preserve order
                ));



    }
}
