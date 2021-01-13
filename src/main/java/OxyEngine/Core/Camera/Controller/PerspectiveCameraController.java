package OxyEngine.Core.Camera.Controller;

import OxyEngineEditor.UI.Panels.SceneHierarchyPanel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Vector3f;

import static OxyEngine.System.OxyEventSystem.*;
import static org.lwjgl.glfw.GLFW.*;

public class PerspectiveCameraController extends OxyCameraController {

    public PerspectiveCameraController(Vector3f translationRef, Vector3f rotationRef, float mouseSpeed, float horizontalSpeed, float verticalSpeed) {
        super(translationRef, rotationRef, mouseSpeed, horizontalSpeed, verticalSpeed);
    }

    public PerspectiveCameraController(Vector3f translationRef, Vector3f rotationRef) {
        super(translationRef, rotationRef, 0.05f, 7f, 7f);
    }

    private void rotate() {
        float dx = (float) (mouseCursorPosDispatcher.getXPos() - oldMouseX);
        float dy = (float) (mouseCursorPosDispatcher.getYPos() - oldMouseY);

        rotationRef.x += (-dy * mouseSpeed) / 16;
        rotationRef.y += (-dx * mouseSpeed) / 16;
    }

    private void updateRotationFree(float ts) {
        if (ScenePanel.focusedWindowDragging)
            rotate();

        updatePosition(ts);

        oldMouseX = mouseCursorPosDispatcher.getXPos();
        oldMouseY = mouseCursorPosDispatcher.getYPos();
    }

    private void updateRotationSwipe() {
        if ((ScenePanel.hoveredWindow || SceneHierarchyPanel.focusedWindowDragging) && mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_MIDDLE]) {
            rotate();
        }

        if (keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_SHIFT] &&
                mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_RIGHT] &&
                ScenePanel.hoveredWindow) {
            float dx = (float) (mouseCursorPosDispatcher.getXPos() - oldMouseX);
            float dy = (float) (mouseCursorPosDispatcher.getYPos() - oldMouseY);
            float angle90 = rotationRef.y;
            positionRef.x += Math.cos(angle90) * (-dx * mouseSpeed);
            positionRef.z += Math.sin(angle90) * (-dx * mouseSpeed);
            positionRef.y += (-dy * mouseSpeed);
        }

        oldMouseX = mouseCursorPosDispatcher.getXPos();
        oldMouseY = mouseCursorPosDispatcher.getYPos();
    }

    private void updatePosition(float ts) {
        if (!ScenePanel.hoveredWindow) return;
        float angle90 = (float) (rotationRef.y + (Math.PI / 2));
        float angle = rotationRef.y;
        if (keyEventDispatcher.getKeys()[GLFW_KEY_W]) {
            positionRef.x += Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_S]) {
            positionRef.x -= Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_D]) {
            positionRef.x += Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_A]) {
            positionRef.x -= Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_SPACE]) {
            positionRef.y -= verticalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_SHIFT]) {
            positionRef.y += verticalSpeed * ts;
        }
    }

    @Override
    public void update(float ts, Mode mode) {
        OxyCameraController.ts = ts;
        if (mode == Mode.SWIPE) updateRotationSwipe();
        else updateRotationFree(ts);
    }
}
