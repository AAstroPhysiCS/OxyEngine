package OxyEngine.Core.Camera;

import OxyEngine.Core.Camera.Controller.OxyCameraController;
import org.joml.Matrix4f;

public abstract class OxyCamera {

    protected final int location;
    protected final boolean transpose;
    protected Matrix4f viewMatrix, modelMatrix, projectionMatrix;

    protected OxyCameraController cameraController;

    public OxyCamera(int location, boolean transpose) {
        this.location = location;
        this.transpose = transpose;
    }

    public abstract Matrix4f setProjectionMatrix();

    public abstract Matrix4f setModelMatrix();

    public abstract void finalizeCamera();

    public boolean isTranspose() {
        return transpose;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public OxyCameraController getCameraController() {
        return cameraController;
    }

    public int getLocation() {
        return location;
    }
}
