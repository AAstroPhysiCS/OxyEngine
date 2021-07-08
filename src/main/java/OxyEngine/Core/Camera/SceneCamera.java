package OxyEngine.Core.Camera;

import OxyEngine.Core.Window.*;
import OxyEngine.Scene.SceneRuntime;
import org.joml.Matrix4f;

public class SceneCamera extends PerspectiveCamera {

    public SceneCamera(float mouseSpeed, float horizontalSpeed, float verticalSpeed, boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(mouseSpeed, horizontalSpeed, verticalSpeed, primary, fovY, aspect, zNear, zFar, transpose);
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    public SceneCamera() {
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    private void calcProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective((float) Math.toRadians(fovY), aspect, zNear, zFar);
    }

    private void calcModelMatrix() {
        modelMatrix.identity();
        modelMatrix.rotateX(-this.getRotation().x);
        modelMatrix.rotateY(-this.getRotation().y);
        modelMatrix.translate(-this.getPosition().x, -this.getPosition().y, -this.getPosition().z);
    }

    @Override
    public void update() {
        calcProjectionMatrix();
        calcModelMatrix();
        calcViewMatrixNoTranslation();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
        viewMatrix.origin(this.origin);
    }

    private void updateUniformBuffer() {
        cameraUniformBuffer.setData(0, getViewMatrix());
        cameraUniformBuffer.setData(64, getViewMatrixNoTranslation());
        cameraUniformBuffer.setData(128, origin);
    }

    @Override
    public void onEvent(OxyEvent event) {
        OxyEventDispatcher dispatcher = OxyEventDispatcher.getInstance();
        dispatcher.dispatch(OxyMouseEvent.Moved.class, event, this::onMouseMove);
        dispatcher.dispatch(OxyKeyEvent.Press.class, event, this::onKeyPress);
    }

    private void onKeyPress(OxyKeyEvent.Press event) {
        updatePosition(event, SceneRuntime.TS);
    }

    private void onMouseMove(OxyMouseEvent.Moved event) {
        update();
        updateUniformBuffer();

        oldMouseX = event.getX();
        oldMouseY = event.getY();
    }

    private void updatePosition(OxyKeyEvent.Press event, float ts) {
        float angle90 = (float) (-rotationRef.y + (Math.PI / 2));
        float angle = -rotationRef.y;
        zoom = 0;
        if (event.getKeyCode().equals(KeyCode.GLFW_KEY_W)) {
            positionRef.x += Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (event.getKeyCode().equals(KeyCode.GLFW_KEY_S)) {
            positionRef.x -= Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (event.getKeyCode().equals(KeyCode.GLFW_KEY_D)) {
            positionRef.x -= Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle) * horizontalSpeed * ts;
        }
        if (event.getKeyCode().equals(KeyCode.GLFW_KEY_A)) {
            positionRef.x += Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle) * horizontalSpeed * ts;
        }
        if (event.getKeyCode().equals(KeyCode.GLFW_KEY_SPACE)) {
            positionRef.y -= verticalSpeed * ts;
        }
        if (event.getKeyCode().equals(KeyCode.GLFW_KEY_LEFT_SHIFT)) {
            positionRef.y += verticalSpeed * ts;
        }
        update();
        updateUniformBuffer();
    }
}
