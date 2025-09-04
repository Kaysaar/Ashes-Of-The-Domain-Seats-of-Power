package data.ui.timeline;

import ashlib.data.plugins.ui.plugins.UILinesRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.misc.ProductionUtil;
import data.misc.ReflectionUtilis;
import data.scripts.managers.AoTDFactionManager;
import data.scripts.models.BaseFactionTimelineEvent;
import data.scripts.models.CycleTimelineEvents;
import data.ui.basecomps.ExtendUIPanelPlugin;
import data.ui.basecomps.RightMouseInterceptor;
import data.ui.basecomps.RightMouseTooltipMoverV2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class FactionTimelineViewerComponent implements ExtendUIPanelPlugin {
    public CustomPanelAPI mainPanel;
    CustomPanelAPI dummy;
    UILinesRenderer renderer;
    RightMouseTooltipMoverV2 mover;
    TooltipMakerAPI tooltip;
    CustomPanelAPI content;
    RightMouseInterceptor interceptor = new RightMouseInterceptor();
    ButtonAPI left, right;
    ButtonAPI takeScreenshot;
    SpriteAPI sprite = Global.getSettings().getSprite("rendering","GlitchSquare");
    ScreenActionBlocker blocker;
    private boolean exportInProgress = false;
    private int exportTileIndex = 0;
    private int exportTiles = 0;
    private int exportTileWidth = 0;
    private int exportContentW = 0;
    private int exportContentH = 0;
    private int settleFrames = 0;
    private boolean requestCaptureThisFrame = false;

    private BufferedImage exportImage;
    private Object exportOutFile;
    private String exportPath;

    public FactionTimelineViewerComponent(float width, float height) {
        renderer = new UILinesRenderer(0f);
        mainPanel = Global.getSettings().createCustom(width, height, this);
        interceptor = new RightMouseInterceptor();
        renderer.setPanel(mainPanel);
        init();
    }

    public void init() {
        dummy = Global.getSettings().createCustom(
                mainPanel.getPosition().getWidth(),
                mainPanel.getPosition().getHeight(), null);

        CustomPanelAPI blocker = Global.getSettings().createCustom(
                mainPanel.getPosition().getWidth(),
                mainPanel.getPosition().getHeight(), interceptor);

        tooltip = mainPanel.createUIElement(
                mainPanel.getPosition().getWidth(),
                mainPanel.getPosition().getHeight(), true);

        tooltip.addSpacer(mainPanel.getPosition().getHeight() * 2);
        createUI();

        dummy.getPosition().setSize(
                Math.max(content.getPosition().getWidth(), mainPanel.getPosition().getWidth()),
                dummy.getPosition().getHeight());

        tooltip.addCustom(dummy, 0f).getPosition().inTL(0, 0);

        mainPanel.addUIElement(tooltip).inTL(0, 0);
        interceptor.setPanelPos(blocker);
        mainPanel.addComponent(blocker).inTL(0, 0);

        mover = new RightMouseTooltipMoverV2();
        mover.init(dummy, mainPanel);
        left = tooltip.addButton("<<", "<<", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 20, 40, 0f);
        right = tooltip.addButton(">>", ">>", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 20, 40, 0f);
        takeScreenshot = tooltip.addButton("Export Timeline to Png", ">>", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 170, 40, 0f);
        left.getPosition().inTL(mainPanel.getPosition().getCenterX()-left.getPosition().getWidth()-10, mainPanel.getPosition().getHeight()-left.getPosition().getHeight()-5);
        right.getPosition().inTL(mainPanel.getPosition().getCenterX()+right.getPosition().getWidth()+10, mainPanel.getPosition().getHeight()-right.getPosition().getHeight()-5);
        takeScreenshot.getPosition().inTL(mainPanel.getPosition().getWidth()-takeScreenshot.getPosition().getWidth()-5,mainPanel.getPosition().getHeight()-right.getPosition().getHeight()-5);
        tooltip.setHeightSoFar(0f);
        mover.setBorders(-dummy.getPosition().getWidth() + mainPanel.getPosition().getWidth(), 0);
        mover.setCurrOffset(0f);
        if (tooltip.getExternalScroller() != null) {
            ReflectionUtilis.invokeMethodWithAutoProjection("setMaxShadowHeight", tooltip.getExternalScroller(), 0f);
            ReflectionUtilis.invokeMethodWithAutoProjection("setShowScrollbars", tooltip.getExternalScroller(), false);
        }
    }

    public void createUI() {
        if (content != null) {
            dummy.removeComponent(content);
        }
        float calWidth = 25;
        AoTDFactionManager.getInstance().getCycles().forEach(x->x.getEventsDuringCycle().forEach(BaseFactionTimelineEvent::updateDataUponEntryOfUI));
        ArrayList<FactionCycleShowcase> generatedShowcases = new ArrayList<>();
        for (CycleTimelineEvents cycle : AoTDFactionManager.getInstance().getCycles()) {
            FactionCycleShowcase showcase = new FactionCycleShowcase(cycle,dummy.getPosition().getHeight());
            generatedShowcases.add(showcase);
            calWidth+=showcase.getGeneratedWidth()+FactionCycleShowcase.spacerBetweenEvents;
        }
        content = Global.getSettings().createCustom(calWidth, dummy.getPosition().getHeight(), null);
        TooltipMakerAPI tip = content.createUIElement(calWidth, content.getPosition().getHeight(), false);
        CustomPanelAPI dash = new DashLinePanel(Math.max(mainPanel.getPosition().getWidth(),content.getPosition().getWidth()),content.getPosition().getHeight()).getMainPanel();
        tip.addCustom(dash,0f).getPosition().inTL(0,0);
        float currOffset =25;
        for (FactionCycleShowcase showcase : generatedShowcases) {
            tip.addCustom(showcase.getMainPanel(),0f).getPosition().inTL(currOffset,0);
            currOffset+=showcase.generatedWidth+FactionCycleShowcase.spacerBetweenEvents;
        }
        generatedShowcases.clear();
        content.addUIElement(tip).inTL(0, 0);
        dummy.addComponent(content).inTL(0, 0);
    }

    @Override
    public CustomPanelAPI getMainPanel() { return mainPanel; }
    @Override
    public void positionChanged(PositionAPI position) {}
    @Override
    public void renderBelow(float alphaMult) {
        if(exportInProgress){
            sprite.setColor(Color.BLACK);
            sprite.setSize(mainPanel.getPosition().getWidth(),mainPanel.getPosition().getHeight());
            sprite.renderAtCenter(mainPanel.getPosition().getCenterX(),mainPanel.getPosition().getCenterY());
        }
    }

    @Override
    public void render(float alphaMult) {

        if (exportInProgress && requestCaptureThisFrame) {
            requestCaptureThisFrame = false;
            captureSlice();
        }
        if(!exportInProgress){
            renderer.render(alphaMult);
        }
    }

    @Override
    public void advance(float amount) {

        if(takeScreenshot!=null){
            takeScreenshot.setEnabled(!exportInProgress&&left.isEnabled());
            if(takeScreenshot.isChecked()){
                takeScreenshot.setChecked(false);
                blocker = new ScreenActionBlocker();
                String name = Global.getSector().getPlayerFaction().getDisplayName()+"_"+Global.getSector().getClock().getShortDate()+"_timeline.png";
                generateImage(PathManager.getTimelineScreenshotsPath()+name);

            }

        }
        mover.advance(amount);
        if (exportInProgress) {
            if (settleFrames > 0) {
                settleFrames--;
            } else {
                requestCaptureThisFrame = true;
            }
        }
        if(left!=null){
            if(mover.isMoving()&&left.isEnabled()){
                left.setEnabled(false);
            } else if (!mover.isMoving()) {
                left.setEnabled(true);
            }
            if(!mover.isMoving()){
                if(left.isChecked()){
                    left.setChecked(false);
                    mover.moveBy(mainPanel.getPosition().getWidth());
                }
            }
        }
        if(right!=null){
            if(mover.isMoving()&&right.isEnabled()){
                right.setEnabled(false);

            } else if (!mover.isMoving()) {
                right.setEnabled(true);
            }
            if(!mover.isMoving()){
                if(right.isChecked()){
                    right.setChecked(false);
                    mover.moveBy(-mainPanel.getPosition().getWidth());
                }
            }
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {}
    @Override
    public void buttonPressed(Object buttonId) {}

    private int capW = 0, capH = 0;
    private int yOffset = 205;
    private int writtenUntil = 0;

    private int getCurrentLeftX() {
        int leftX = Math.round(-dummy.getPosition().getX());
        if (leftX < 0) leftX = 0;
        int maxLeft = Math.max(0, exportContentW - capW);
        if (leftX > maxLeft) leftX = maxLeft;
        return leftX;
    }
    private void jumpToLeftX(int leftX) {
        mover.setCurrOffset(-leftX);
    }

    public void generateImage(String absolutePath) {
        if (exportInProgress) return;
        if (content == null || mainPanel == null || dummy == null) {
            createUI();
        }
        capW = Math.round(mainPanel.getPosition().getWidth());
        capH = Math.round(mainPanel.getPosition().getHeight()-70);
        exportContentW = Math.max((int) content.getPosition().getWidth(), capW);
        exportContentH = capH;
        exportTileWidth = capW;
        exportTiles = (int) Math.ceil(exportContentW / (float) exportTileWidth);
        exportTileIndex = 0;
        writtenUntil = 0;
        requestCaptureThisFrame = false;
        exportImage = new BufferedImage(exportContentW, exportContentH, BufferedImage.TYPE_INT_ARGB);
        exportPath = absolutePath;
        exportOutFile =  ReflectionUtilis.getFile(absolutePath);
        ReflectionUtilis.invokeMethodWithAutoProjection("createNewFile",exportOutFile);
        jumpToLeftX(0);
        settleFrames = 1;
        exportInProgress = true;
        if (left != null) left.setEnabled(false);
        if (right != null) right.setEnabled(false);
    }

    private void captureSlice() {
        int baseW = capW;
        int viewH = capH;

        int leftX = getCurrentLeftX();
        int remaining = exportContentW - writtenUntil;
        if (remaining <= 0) { finish(); return; }

        int readW = Math.min(baseW, remaining);

        PositionAPI pos = mainPanel.getPosition();
        final int screenH = (int) Global.getSettings().getScreenHeight();
        int scX = Math.round(pos.getX());
        int scY = screenH - Math.round(pos.getY() + pos.getHeight());
        scY += yOffset;
        if (scY + viewH > screenH) scY = screenH - viewH;
        if (scY < 0) scY = 0;

        if (readW < baseW) scX += (baseW - readW);

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glReadBuffer(GL11.GL_BACK);
        ByteBuffer buf = BufferUtils.createByteBuffer(readW * viewH * 4);
        GL11.glReadPixels(scX, scY, readW, viewH, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);

        boolean empty = true;
        for (int k = 0; k < Math.min(2048, buf.capacity()); k++) { if (buf.get(k) != 0) { empty = false; break; } }
        if (empty) {
            buf.rewind();
            GL11.glReadBuffer(GL11.GL_FRONT);
            GL11.glReadPixels(scX, scY, readW, viewH, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);
        }

        for (int y = 0; y < viewH; y++) {
            int targetY = (viewH - 1 - y);
            int rowBase = y * readW * 4;
            for (int x = 0; x < readW; x++) {
                int i = rowBase + (x * 4);
                int b = buf.get(i)   & 0xFF;
                int g = buf.get(i+1) & 0xFF;
                int r = buf.get(i+2) & 0xFF;
                int a = 0xFF;
                exportImage.setRGB(writtenUntil + x, targetY, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        writtenUntil += readW;

        if (writtenUntil >= exportContentW) {
            finish();
            return;
        }

        int nextLeft = writtenUntil;
        int maxLeft = Math.max(0, exportContentW - baseW);
        if (nextLeft > maxLeft) nextLeft = maxLeft;
        jumpToLeftX(nextLeft);
        settleFrames = 1;
    }

    private void finish() {
        try {
            Class<?> fileClz = exportOutFile.getClass();
            Boolean ok = (Boolean) ReflectionUtilis.invokeStaticExact(
                    ImageIO.class,
                    "write",
                    new Class<?>[]{java.awt.image.RenderedImage.class, String.class, fileClz},
                    exportImage, "png", exportOutFile
            );
            if (ok == null || !ok) Global.getLogger(getClass()).error("PNG writer for 'png' not found; nothing written.");
            else Global.getLogger(getClass()).info("Timeline PNG saved to: " + exportPath);
        } catch (Throwable e) {
            Global.getLogger(getClass()).error("Failed to save timeline PNG", e);
        } finally {
            exportInProgress = false;
            exportImage = null;
            exportOutFile = null;
            if (left != null) left.setEnabled(true);
            if (right != null) right.setEnabled(true);
            jumpToLeftX(0);
            ProductionUtil.getCoreUI().removeComponent(blocker.mainPanel);
            blocker = null;
        }
    }

    public void generateImageToAbsolutePath(String absolutePath) {
        generateImage(absolutePath);
    }
}
