package OxyEngine.Core.Camera;

import OxyEngine.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.SceneHierarchyPanel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import imgui.ImGui;
import imgui.ImGuiIO;
import org.joml.Matrix4f;

import static OxyEngine.System.OxyEventSystem.*;
import static org.lwjgl.glfw.GLFW.*;

public class EditorCamera extends PerspectiveCamera {

    public EditorCamera(boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(0.05f, 20f, 20f, primary, fovY, aspect, zNear, zFar, transpose);
    }

    @Override
    public Matrix4f setProjectionMatrix() {
        Matrix4f m = new Matrix4f();
        m.setPerspective((float) Math.toRadians(fovY), aspect, zNear, zFar);
        return m;
    }

    @Override
    public Matrix4f setModelMatrix() {
        Matrix4f m = new Matrix4f();
        m.translate(0, 0, -zoom);
        m.rotateX(-this.getRotation().x);
        m.rotateY(-this.getRotation().y);
        m.translate(-this.getPosition().x, -this.getPosition().y, -this.getPosition().z);
        return m;
    }

    @Override
    public void finalizeCamera(float ts) {
        ImGuiIO io = ImGui.getIO();
        if (ScenePanel.hoveredWindow) {
            if (io.getMouseWheel() > 0) {
                zoom += zoomSpeed * ts;
            } else if (io.getMouseWheel() < 0) {
                zoom += -zoomSpeed * ts;
            }
            if (zoom >= 500) zoom = 500;
            if (zoom <= -500) zoom = -500;
        }

        update(Mode.SWIPE);
        modelMatrix = setModelMatrix();
        projectionMatrix = setProjectionMatrix();
        viewMatrix = new Matrix4f();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
        viewMatrix.origin(this.origin);
    }

    public enum Mode {
        SWIPE(), FREE()
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
        if ((ScenePanel.hoveredWindow || SceneHierarchyPanel.focusedWindowDragging) &&
                mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_RIGHT] && !keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_SHIFT]) {
            rotate();
        }

        if (keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_SHIFT] &&
                mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_RIGHT] &&
                ScenePanel.hoveredWindow) {
            float dx = (float) (mouseCursorPosDispatcher.getXPos() - oldMouseX);
            float dy = (float) (mouseCursorPosDispatcher.getYPos() - oldMouseY);
            float angle90 = rotationRef.y;
            positionRef.x += Math.cos(angle90) * (-dx * mouseSpeed);
            positionRef.z -= Math.sin(angle90) * (-dx * mouseSpeed);
            positionRef.y -= (-dy * mouseSpeed);
        }

        oldMouseX = mouseCursorPosDispatcher.getXPos();
        oldMouseY = mouseCursorPosDispatcher.getYPos();
    }

    private void updatePosition(float ts) {
        float angle90 = (float) (-rotationRef.y + (Math.PI / 2));
        float angle = -rotationRef.y;
        zoom = 0;
        if (keyEventDispatcher.getKeys()[GLFW_KEY_W]) {
            positionRef.x += Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_S]) {
            positionRef.x -= Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_D]) {
            positionRef.x -= Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_A]) {
            positionRef.x += Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle) * horizontalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_SPACE]) {
            positionRef.y -= verticalSpeed * ts;
        }
        if (keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_SHIFT]) {
            positionRef.y += verticalSpeed * ts;
        }
    }

    private void update(Mode mode) {
        if (mode == Mode.SWIPE) updateRotationSwipe();
        else updateRotationFree(SceneRuntime.TS);
    }

}
