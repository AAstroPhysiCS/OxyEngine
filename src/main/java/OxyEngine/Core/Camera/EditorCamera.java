package OxyEngine.Core.Camera;

import OxyEngine.Core.Window.*;
import OxyEngine.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.SceneHierarchyPanel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import imgui.ImGui;
import imgui.ImGuiIO;
import org.joml.Matrix4f;

public class EditorCamera extends PerspectiveCamera {

    public EditorCamera(boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(0.05f, 20f, 20f, primary, fovY, aspect, zNear, zFar, transpose);
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    private void calcProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.setPerspective((float) Math.toRadians(fovY), aspect, zNear, zFar);
    }

    private void calcModelMatrix() {
        modelMatrix.identity();
        modelMatrix.translate(0, 0, -zoom);
        modelMatrix.rotateX(-this.getRotation().x);
        modelMatrix.rotateY(-this.getRotation().y);
        modelMatrix.translate(-this.getPosition().x, -this.getPosition().y, -this.getPosition().z);
    }

    @Override
    public void update() {
        calcProjectionMatrix();
        calcModelMatrix();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
        viewMatrix.origin(this.origin);
        calcViewMatrixNoTranslation();
    }

    @Override
    public void onEvent(OxyEvent event) {
        OxyEventDispatcher dispatcher = OxyEventDispatcher.getInstance();
        dispatcher.dispatch(OxyMouseEvent.Moved.class, event, this::onMouseMove);
        dispatcher.dispatch(OxyMouseEvent.Scroll.class, event, this::onMouseScroll);
    }

    private void onMouseMove(OxyMouseEvent.Moved event) {
        updateRotationSwipe();
    }

    private void onMouseScroll(OxyMouseEvent.Scroll event){
        ImGuiIO io = ImGui.getIO();
        if (ScenePanel.hoveredWindow) {
            if (io.getMouseWheel() > 0) {
                zoom += zoomSpeed * SceneRuntime.TS;
            } else if (io.getMouseWheel() < 0) {
                zoom += -zoomSpeed * SceneRuntime.TS;
            }
            if (zoom >= 500) zoom = 500;
            if (zoom <= -500) zoom = -500;
        }
    }

    private void rotate() {
        float dx = (float) (Input.getMouseX() - oldMouseX);
        float dy = (float) (Input.getMouseY() - oldMouseY);

        rotationRef.x += (-dy * mouseSpeed) / 16;
        rotationRef.y += (-dx * mouseSpeed) / 16;
    }

    private void updateRotationSwipe() {
        if ((ScenePanel.hoveredWindow || SceneHierarchyPanel.focusedWindowDragging) &&
                Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_RIGHT) && !Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT)) {
            rotate();
        }

        if (Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT) &&
                Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_RIGHT) &&
                ScenePanel.hoveredWindow) {
            float dx = (float) (Input.getMouseX() - oldMouseX);
            float dy = (float) (Input.getMouseY() - oldMouseY);
            float angle90 = rotationRef.y;
            positionRef.x += Math.cos(angle90) * (-dx * mouseSpeed);
            positionRef.z -= Math.sin(angle90) * (-dx * mouseSpeed);
            positionRef.y -= (-dy * mouseSpeed);
        }

        oldMouseX = Input.getMouseX();
        oldMouseY = Input.getMouseY();
    }
}
