package OxyEngine.Core.Camera;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Window.*;
import OxyEngineEditor.UI.Panels.SceneHierarchyPanel;
import OxyEngineEditor.UI.Panels.ScenePanel;
import imgui.ImGui;
import imgui.ImGuiIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class EditorCamera extends PerspectiveCamera {

    protected final Vector3f positionRef;
    protected final Vector3f rotationRef;
    public final Vector3f origin = new Vector3f();

    public EditorCamera(Vector3f position, Vector3f rotation, boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(0.05f, 20f, 20f, primary, fovY, aspect, zNear, zFar, transpose);
        this.positionRef = position;
        this.rotationRef = rotation;
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
        modelMatrix.rotateX(-rotationRef.x);
        modelMatrix.rotateY(-rotationRef.y);
        modelMatrix.translate(-positionRef.x, -positionRef.y, -positionRef.z);
    }

    private void calcViewMatrixNoTranslation() {
        viewMatrixNoTranslation.set(getProjectionMatrix());
        viewMatrixNoTranslation.rotateX(-rotationRef.x);
        viewMatrixNoTranslation.rotateY(-rotationRef.y);
    }

    @Override
    public void update() {
        calcProjectionMatrix();
        calcModelMatrix();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
        viewMatrix.origin(this.origin);
        calcViewMatrixNoTranslation();

        cameraUniformBuffer.setData(0, getViewMatrix());
        cameraUniformBuffer.setData(64, getViewMatrixNoTranslation());
        cameraUniformBuffer.setData(128, origin);
    }

    @Override
    public void onEvent(Event event) {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        dispatcher.dispatch(MouseEvent.Moved.class, event, this::onMouseMove);
        dispatcher.dispatch(MouseEvent.Scroll.class, event, this::onMouseScroll);
    }

    private void onMouseMove(MouseEvent.Moved event) {
        updateRotationSwipe(event);
        update();
    }

    private void onMouseScroll(MouseEvent.Scroll event){

        cameraUniformBuffer.setData(0, getViewMatrix());
        cameraUniformBuffer.setData(64, getViewMatrixNoTranslation());
        cameraUniformBuffer.setData(128, origin);

        ImGuiIO io = ImGui.getIO();
        if (ScenePanel.hoveredWindow) {
            if (io.getMouseWheel() > 0) {
                zoom += zoomSpeed * Renderer.TS;
            } else if (io.getMouseWheel() < 0) {
                zoom += -zoomSpeed * Renderer.TS;
            }
            if (zoom >= 500) zoom = 500;
            if (zoom <= -500) zoom = -500;
        }
        update();
    }

    private void rotate() {
        float dx = (float) (Input.getMouseX() - oldMouseX);
        float dy = (float) (Input.getMouseY() - oldMouseY);

        rotationRef.x += (-dy * mouseSpeed) / 16;
        rotationRef.y += (-dx * mouseSpeed) / 16;
    }

    private void updateRotationSwipe(MouseEvent.Moved event) {
        if ((ScenePanel.hoveredWindow || SceneHierarchyPanel.focusedWindowDragging) &&
                Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_RIGHT) && !Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT)) {
            rotate();
        }

        if (Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT) &&
                Input.isMouseButtonPressed(MouseCode.GLFW_MOUSE_BUTTON_RIGHT) &&
                ScenePanel.hoveredWindow) {
            float dx = (float) (event.getX() - oldMouseX);
            float dy = (float) (event.getY() - oldMouseY);
            float angle90 = rotationRef.y;
            positionRef.x += Math.cos(angle90) * (-dx * mouseSpeed);
            positionRef.z -= Math.sin(angle90) * (-dx * mouseSpeed);
            positionRef.y -= (-dy * mouseSpeed);
        }

        oldMouseX = event.getX();
        oldMouseY = event.getY();
    }
}
