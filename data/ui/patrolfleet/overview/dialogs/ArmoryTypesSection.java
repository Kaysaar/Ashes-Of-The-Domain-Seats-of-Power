package data.ui.patrolfleet.overview.dialogs;

import ashlib.data.plugins.ui.models.ExtendedUIPanelPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.tot.produciton.specs.AoTDProductionSpec;
import data.kaysaar.aotd.tot.produciton.specs.AoTDProductionSpecManager;
import data.kaysaar.aotd.tot.ui.onhover.ButtonOnHoverInfo;
import data.kaysaar.aotd.vok.Ids.AoTDTechIds;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;

public class ArmoryTypesSection implements ExtendedUIPanelPlugin {
    public ButtonAPI ship, weapon, fighter;
    public ButtonAPI curr = null;
    public CustomPanelAPI mainPanel;
    public boolean needsUpdate = false;

    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return mainPanel;
    }

    public ArmoryTypesSection() {
        LabelAPI label = Global.getSettings().createLabel("", Fonts.DEFAULT_SMALL);

        mainPanel = Global.getSettings().createCustom(500, 20, this);
        TooltipMakerAPI tooltip = mainPanel.createUIElement(mainPanel.getPosition().getWidth(), mainPanel.getPosition().getHeight(), false);
        ship = tooltip.addButton("Ships", AoTDProductionSpec.AoTDProductionSpecType.SHIP, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TOP, label.computeTextWidth("ships") + 70, 20, 0f);
        ship.getPosition().inTL(0, 0);
        ship.setShortcut(Keyboard.KEY_Q,false);
        tooltip.addTooltipToPrevious(new ButtonOnHoverInfo(400, false, null, "Here you can prioritize ships to be used in faction fleets that are not part of the patrol system.", null, null, null, "Ship priority selection"), TooltipMakerAPI.TooltipLocation.BELOW, false);

        weapon = tooltip.addButton("Weapons", AoTDProductionSpec.AoTDProductionSpecType.WEAPON, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TOP, label.computeTextWidth("weapons") + 70, 20, 0f);
        tooltip.addTooltipToPrevious(new ButtonOnHoverInfo(400, false, null, "Here you can prioritize weapons to be used on ships in faction fleets.", null, null, null, "Weapon priority selection"), TooltipMakerAPI.TooltipLocation.BELOW, false);
        weapon.setShortcut(Keyboard.KEY_W,false);
        fighter = tooltip.addButton("Fighters", AoTDProductionSpec.AoTDProductionSpecType.FIGHTER, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TOP, label.computeTextWidth("fighters") + 70, 20, 0f);
        tooltip.addTooltipToPrevious(new ButtonOnHoverInfo(400, false, null, "Here you can prioritize fighters to be used on ships in faction fleets.", null, null, null, "Fighter priority selection"), TooltipMakerAPI.TooltipLocation.BELOW, false);
        fighter.setShortcut(Keyboard.KEY_E,false);
        curr = ship;
        curr.highlight();
        weapon.getPosition().rightOfMid(ship, 1);
        fighter.getPosition().rightOfMid(weapon, 1);
        mainPanel.addUIElement(tooltip).inTL(0, 1);

    }

    @Override
    public void createUI() {

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
        if (ship != null) {
            if (ship.isChecked()) {
                ship.setChecked(false);
                curr.unhighlight();
                curr = ship;
                curr.highlight();
                setNeedsUpdate(true);
            }
            if (weapon.isChecked()) {
                weapon.setChecked(false);
                curr.unhighlight();
                curr = weapon;
                curr.highlight();
                setNeedsUpdate(true);
            }
            if (fighter.isChecked()) {
                fighter.setChecked(false);
                curr.unhighlight();
                curr = fighter;
                curr.highlight();
                setNeedsUpdate(true);
            }
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
