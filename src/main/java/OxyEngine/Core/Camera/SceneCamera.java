package OxyEngine.Core.Camera;

import OxyEngine.Core.Window.Event;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SceneCamera extends PerspectiveCamera {

    private final Matrix4f transform;
    private final Vector3f origin = new Vector3f();

    public SceneCamera(Matrix4f transform, float mouseSpeed, float horizontalSpeed, float verticalSpeed, boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(mouseSpeed, horizontalSpeed, verticalSpeed, primary, fovY, aspect, zNear, zFar, transpose);
        this.transform = transform;
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    public SceneCamera(Matrix4f transform) {
        this.transform = transform;
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

        Vector3f rotation = new Vector3f();
        Quaternionf quat = new Quaternionf();
        transform.getUnnormalizedRotation(quat);
        quat.getEulerAnglesXYZ(rotation);

        Vector3f position = new Vector3f();
        transform.getTranslation(position);

        modelMatrix.rotateX(-rotation.x);
        modelMatrix.rotateY(-rotation.y);
        modelMatrix.translate(-position.x, -position.y, -position.z);
    }

    private void calcViewMatrixNoTranslation() {
        viewMatrixNoTranslation.set(getProjectionMatrix());

        Vector3f rotation = new Vector3f();
        Quaternionf quat = new Quaternionf();
        transform.getUnnormalizedRotation(quat);
        quat.getEulerAnglesXYZ(rotation);

        viewMatrixNoTranslation.rotateX(-rotation.x);
        viewMatrixNoTranslation.rotateY(-rotation.y);
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
    public void onEvent(Event event) {
    }
}
