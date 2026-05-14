package data.ui.basecomps;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.lwjgl.input.Mouse;

import java.util.List;

public class RightMouseTooltipMoverV2 implements ExtendUIPanelPlugin {

    CustomPanelAPI panelOfTooltip;
    CustomPanelAPI absolutePanel;
    float width;
    float height;
    public float startingX = 0;
    private float startingY = 0;
    public boolean isDraggingWithRightMouse = false;
    private float trueWidth = 0f;
    float prevX = -1f;
    private float startingXOfRight = -1;
    float currOffset = 0f;
    float initalOffset = 0f;
    private float targetX = 0f;
    private boolean isMoving = false;
    private float velocityX = 0f;
    private  float acceleration = 2100;
    private  float maxSpeed = 1400;
    private float decelerationDistance = 150f; // Distance to begin slowing down

    float leftBorderX;
    float rightBorderX;


    public void setBorders(float left, float right) {
        leftBorderX = left;
        rightBorderX = right;
        currOffset = leftBorderX;
    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    public void init(CustomPanelAPI panelAPI, CustomPanelAPI absolutePanel) {
        this.absolutePanel = absolutePanel;
        panelOfTooltip = panelAPI;
        leftBorderX = -panelOfTooltip.getPosition().getWidth() + 20;
        rightBorderX = panelOfTooltip.getPosition().getWidth() - 20;
        currOffset = leftBorderX;

    }

    public void setCurrOffset(float offset) {
        currOffset = offset;
    }


    @Override
    public void renderBelow(float alphaMult) {

    }


    @Override
    public void render(float alphaMult) {


    }

    public boolean isMoving() {
        return isMoving;
    }

    public void moveBy(float distance) {
        if (distance == 0) return;
        float desiredTarget = currOffset + distance;

        moveTo(desiredTarget); // Reuses the existing moveTo logic
    }

    public void moveTo(float targetX) {
        // Clamp the target to stay within bounds
        this.targetX = Math.max(leftBorderX, Math.min(rightBorderX, targetX));
        float distance = Math.abs(this.targetX - currOffset);
        maxSpeed = Math.max(distance*1.5f,1400);

        decelerationDistance = Math.abs(this.targetX - currOffset) / 5f;
        isMoving = true;
    }


    @Override
    public void advance(float amount) {
        if (panelOfTooltip == null) return;

        if (isMoving) {
            float distance = targetX - currOffset;
            float direction = Math.signum(distance); // +1 if moving right, -1 if left
            float absDistance = Math.abs(distance);

            // Compute speed ramping down near target
            float desiredSpeed = maxSpeed;
            if (absDistance < decelerationDistance) {
                desiredSpeed = maxSpeed * (absDistance / decelerationDistance);
            }

            // Accelerate toward desiredSpeed
            velocityX += direction * acceleration * amount;

            // Clamp to desired speed
            if (Math.abs(velocityX) > desiredSpeed) {
                velocityX = direction * desiredSpeed;
            }

            // Apply movement
            currOffset += velocityX * amount;

            // Snap to target if close
            if (absDistance < 1f) {
                currOffset = targetX;
                velocityX = 0f;
                isMoving = false;
            }

            // Clamp to within left/right borders
            if (checkIfPanelIsGoingTooFar(currOffset)) {
                currOffset = pickNearest(currOffset);
                velocityX = 0f;
                isMoving = false;
            }

            // Apply the offset
            panelOfTooltip.getPosition().setXAlignOffset(currOffset);
        } else {

            detectIfRightMouse();
            handleMouseDragging();
            panelOfTooltip.getPosition().setXAlignOffset(currOffset);

        }
    }


    @Override
    public void processInput(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {

        }
    }

    @Override
    public void buttonPressed(Object buttonId) {

    }

    public void handleMouseDragging() {
        if (isDraggingWithRightMouse) {
            if (startingXOfRight == -1) {
                startingXOfRight = Global.getSettings().getMouseX();
            } else {

                float currX = Global.getSettings().getMouseX();
                float offeset = currOffset;
                currOffset = offeset - (startingXOfRight - currX);
                if (checkIfPanelIsGoingTooFar(currOffset)) {
                    currOffset = pickNearest(currOffset);
                }
                initalOffset = currOffset;
                startingXOfRight = currX;
            }

        } else {
            startingXOfRight = -1;
        }

    }

    public void detectIfRightMouse() {
        if (Mouse.isButtonDown(1)) {
            if (absolutePanel != null) {
                float x = absolutePanel.getPosition().getX();
                float y = absolutePanel.getPosition().getY();
                float width = absolutePanel.getPosition().getWidth();
                float height = absolutePanel.getPosition().getHeight();
                float xMouse = Global.getSettings().getMouseX();
                float yMouse = Global.getSettings().getMouseY();
                if (xMouse > x && xMouse < x + width) {
                    if (yMouse > y && yMouse < y + height) {
                        isDraggingWithRightMouse = true;
                    } else {
                        isDraggingWithRightMouse = false;
                        startingXOfRight = -1;
                    }
                } else {
                    isDraggingWithRightMouse = false;
                    startingXOfRight = -1;

                }
            }

        } else {
            isDraggingWithRightMouse = false;
        }


    }

    public boolean checkIfPanelIsGoingTooFar(float targetedOffset) {
        return (targetedOffset >= rightBorderX || targetedOffset <= leftBorderX);
    }

    public float pickNearest(float targetOffset) {
        if (targetOffset >= 0) {
            return rightBorderX;
        }
        return leftBorderX;
    }

    @Override
    public CustomPanelAPI getMainPanel() {
        return panelOfTooltip;
    }

    @Override
    public void createUI() {

    }

}
