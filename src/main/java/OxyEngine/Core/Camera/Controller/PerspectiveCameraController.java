package OxyEngine.Core.Camera.Controller;

import OxyEngine.Tools.Ref;
import OxyEngineEditor.UI.Panels.SceneHierarchyPanel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import OxyEngineEditor.UI.OxyUISystem;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class PerspectiveCameraController extends OxyCameraController {

    public PerspectiveCameraController(Ref<Vector3f> translationRef, Ref<Vector3f> rotationRef, float mouseSpeed, float horizontalSpeed, float verticalSpeed) {
        super(translationRef, rotationRef, mouseSpeed, horizontalSpeed, verticalSpeed);
    }

    public PerspectiveCameraController(Ref<Vector3f> translationRef, Ref<Vector3f> rotationRef) {
        super(translationRef, rotationRef, 0.05f, 7f, 7f);
    }

    private void rotate() {
        float dx = (float) (OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos() - oldMouseX);
        float dy = (float) (OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos() - oldMouseY);

        rotationRef.obj.x += (-dy * mouseSpeed) / 16;
        rotationRef.obj.y += (-dx * mouseSpeed) / 16;
    }

    private void updateRotationFree() {
        if (ScenePanel.focusedWindowDragging) rotate();

        oldMouseX = OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos();
        oldMouseY = OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos();
    }

    private void updateRotationSwipe() {
        if (OxyUISystem.OxyEventSystem.mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_MIDDLE] && (ScenePanel.focusedWindowDragging || SceneHierarchyPanel.focusedWindowDragging))
            rotate();

        oldMouseX = OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos();
        oldMouseY = OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos();
    }

    private void updatePosition(float ts) {
        if (!ScenePanel.focusedWindow) return;
        float angle90 = (float) (rotationRef.obj.y + (Math.PI / 2));
        float angle = rotationRef.obj.y;
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_W]) {
            positionRef.obj.x += Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.obj.z += Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_S]) {
            positionRef.obj.x -= Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.obj.z -= Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_D]) {
            positionRef.obj.x += Math.cos(angle) * horizontalSpeed * ts;
            positionRef.obj.z += Math.sin(angle) * horizontalSpeed * ts;
        }
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_A]) {
            positionRef.obj.x -= Math.cos(angle) * horizontalSpeed * ts;
            positionRef.obj.z -= Math.sin(angle) * horizontalSpeed * ts;
        }
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_SPACE]) {
            positionRef.obj.y -= verticalSpeed * ts;
        }
        if (OxyUISystem.OxyEventSystem.keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_SHIFT]) {
            positionRef.obj.y += verticalSpeed * ts;
        }
    }

    @Override
    public void update(float ts, Mode mode) {
        if (mode == Mode.SWIPE) updateRotationSwipe();
        else updateRotationFree();
        updatePosition(ts);
    }
}
