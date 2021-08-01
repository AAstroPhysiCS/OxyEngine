package OxyEngine.Core.Camera;

import OxyEngine.Core.Window.*;
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

        updateUniformBuffer();
    }

    private void updateUniformBuffer() {
        cameraUniformBuffer.setData(0, getViewMatrix());
        cameraUniformBuffer.setData(64, getViewMatrixNoTranslation());
        cameraUniformBuffer.setData(128, origin);
    }

    @Override
    public void onEvent(OxyEvent event) {
    }
}
