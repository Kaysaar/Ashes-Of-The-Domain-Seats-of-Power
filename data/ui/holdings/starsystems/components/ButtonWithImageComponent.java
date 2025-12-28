package data.ui.holdings.starsystems.components;

import ashlib.data.plugins.ui.models.resizable.ButtonComponent;
import ashlib.data.plugins.ui.models.resizable.ImageViewer;

public class ButtonWithImageComponent extends ButtonComponent {
    public ButtonWithImageComponent(float width, float height,String imageName) {
        super(width, height);
        addComponent(new ImageViewer(width,height,imageName),0,0);
    }


}
