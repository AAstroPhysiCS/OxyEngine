package OxyEngine.Core.Camera;

import org.joml.Matrix4f;

public class SceneCamera extends PerspectiveCamera {

    public SceneCamera(float mouseSpeed, float horizontalSpeed, float verticalSpeed, boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(mouseSpeed, horizontalSpeed, verticalSpeed, primary, fovY, aspect, zNear, zFar, transpose);
    }

    public SceneCamera(){
    }

    @Override
    public Matrix4f setProjectionMatrix() {
        Matrix4f m = new Matrix4f();
        m.perspective((float) Math.toRadians(-fovY), aspect, zNear, zFar);
        return m;
    }

    @Override
    public Matrix4f setModelMatrix() {
        Matrix4f m = new Matrix4f();
        m.translate(-this.getPosition().x, -this.getPosition().y, -this.getPosition().z);
        m.rotateX(this.getRotation().x);
        m.rotateY(this.getRotation().y);
        return m;
    }

    @Override
    public void finalizeCamera(float ts) {
        modelMatrix = setModelMatrix();
        projectionMatrix = setProjectionMatrix();
        viewMatrix = new Matrix4f();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
        viewMatrix.origin(this.origin);
    }
}
