package data.ui.basecomps;

import ashlib.data.plugins.misc.AshMisc;
import ashlib.data.plugins.ui.models.resizable.ButtonComponent;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;
import ashlib.data.plugins.ui.models.resizable.LabelComponent;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.util.Misc;
import data.misc.ReflectionUtilis;

public class FactionFlagButtonComponent extends ButtonComponent {
    LabelComponent component;
    ImageViewer viewer;
    String lastSavedFlag;
    Object prevValue;
    boolean logo = false;
    public FactionFlagButtonComponent(float width, float height) {
        super(width, height);
        createUI();

    }
    public FactionFlagButtonComponent(float width, float height,boolean logo) {
        super(width, height);
        this.logo = logo;
        createUI();

    }

    public void createUI() {
        float width = originalWidth;
        float height = originalHeight;
        component = new LabelComponent(Fonts.ORBITRON_20AA, 20, Global.getSector().getPlayerFaction().getDisplayNameLong(), Misc.getTextColor().brighter(), width * 4, 30);

        String crestName = Global.getSector().getPlayerFaction().getCrest();
        if(logo){
            crestName = Global.getSector().getPlayerFaction().getLogo();
        }
        viewer = new ImageViewer(width, height, crestName);
        addComponent(viewer, 0, 0);
        addComponent(component, ((width / 2) - (component.getTextWidth() / 2)), -22);
    }

    @Override
    public void performActionOnClick(boolean isRightClick) {
        prevValue = ReflectionUtilis.getPrivateVariable("dialogType", Global.getSector().getCampaignUI());
        ReflectionUtilis.setPrivateVariableFromSuperclass("dialogType", Global.getSector().getCampaignUI(), null);
        lastSavedFlag = Global.getSector().getPlayerFaction().getCrest();
        Global.getSector().getCampaignUI().showPlayerFactionConfigDialog();

    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (component != null) {
            if (!component.getText().equals(Global.getSector().getPlayerFaction().getDisplayNameLong())) {
                removeComponent(component);
                removeComponent(viewer);
                createUI();
            } else if (AshMisc.isStringValid(lastSavedFlag) && !lastSavedFlag.equals(Global.getSector().getPlayerFaction().getCrest())) {
                lastSavedFlag = null;
                removeComponent(component);
                removeComponent(viewer);
                createUI();
            }
        }
        if(ReflectionUtilis.getPrivateVariable("dialogType", Global.getSector().getCampaignUI())==null&&prevValue!=null){
            ReflectionUtilis.setPrivateVariableFromSuperclass("dialogType", Global.getSector().getCampaignUI(), prevValue);
            prevValue=null;

        }

    }
}
