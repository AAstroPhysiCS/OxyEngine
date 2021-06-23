package OxyEngine.Core.Camera;

import OxyEngine.Core.Window.*;
import OxyEngine.Scene.SceneRuntime;

public class SceneCamera extends PerspectiveCamera {

    public SceneCamera(float mouseSpeed, float horizontalSpeed, float verticalSpeed, boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(mouseSpeed, horizontalSpeed, verticalSpeed, primary, fovY, aspect, zNear, zFar, transpose);
    }

    public SceneCamera() {
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
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
        viewMatrix.origin(this.origin);
        calcViewMatrixNoTranslation();
    }

    @Override
    public void onEvent(OxyEvent event) {
        //should do nothing
//        OxyEventDispatcher.getInstance().dispatch(OxyMouseEvent.Moved.class, event, this::onMouseMove);
    }

    private void onMouseMove(OxyMouseEvent.Moved event) {
        updatePosition(SceneRuntime.TS);

        oldMouseX = event.getX();
        oldMouseY = event.getY();
    }

    private void updatePosition(float ts) {
        float angle90 = (float) (-rotationRef.y + (Math.PI / 2));
        float angle = -rotationRef.y;
        zoom = 0;
        if (Input.isKeyPressed(KeyCode.GLFW_KEY_W)) {
            positionRef.x += Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (Input.isKeyPressed(KeyCode.GLFW_KEY_S)) {
            positionRef.x -= Math.cos(angle90) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle90) * horizontalSpeed * ts;
        }
        if (Input.isKeyPressed(KeyCode.GLFW_KEY_D)) {
            positionRef.x -= Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z -= Math.sin(angle) * horizontalSpeed * ts;
        }
        if (Input.isKeyPressed(KeyCode.GLFW_KEY_A)) {
            positionRef.x += Math.cos(angle) * horizontalSpeed * ts;
            positionRef.z += Math.sin(angle) * horizontalSpeed * ts;
        }
        if (Input.isKeyPressed(KeyCode.GLFW_KEY_SPACE)) {
            positionRef.y -= verticalSpeed * ts;
        }
        if (Input.isKeyPressed(KeyCode.GLFW_KEY_LEFT_SHIFT)) {
            positionRef.y += verticalSpeed * ts;
        }
    }
}
