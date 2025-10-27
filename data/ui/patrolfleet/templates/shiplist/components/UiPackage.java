package data.ui.patrolfleet.templates.shiplist.components;

import ashlib.data.plugins.rendering.FighterIconRenderer;
import ashlib.data.plugins.rendering.ShipRenderer;
import ashlib.data.plugins.rendering.WeaponSpriteRenderer;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;


public class UiPackage {

    transient CustomPanelAPI panelPackage;
    transient ShipRenderer render;
    transient ShipHullSpecAPI option;
    ButtonAPI button ;

    public UiPackage(CustomPanelAPI panel, ShipRenderer render, ShipHullSpecAPI option, ButtonAPI button) {
        this.panelPackage = panel;
        this.render = render;
        this.option = option;
        this.button = button;


    }


    public ShipRenderer getRender() {
        return render;
    }

    public CustomPanelAPI getPanelPackage() {
        return panelPackage;
    }

    public ShipHullSpecAPI getOption() {
        return option;
    }
}
