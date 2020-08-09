package OxyEngine.Core.Camera;

import OxyEngine.Core.Camera.Controller.OxyCameraController;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import org.joml.Matrix4f;

public abstract class OxyCamera implements EntityComponent {

    protected final int viewMatrixLocation, projectionMatrixLocation, modelMatrixLocation;
    protected final boolean transpose;
    protected Matrix4f viewMatrix, modelMatrix, projectionMatrix;

    protected OxyCameraController cameraController;

    public OxyCamera(int viewMatrixLocation, int projectionMatrixLocation, int modelMatrixLocation, boolean transpose) {
        this.viewMatrixLocation = viewMatrixLocation;
        this.modelMatrixLocation = modelMatrixLocation;
        this.projectionMatrixLocation = projectionMatrixLocation;
        this.transpose = transpose;
    }

    public abstract Matrix4f setProjectionMatrix();

    public abstract Matrix4f setModelMatrix();

    public abstract void finalizeCamera(float ts);

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

    public int getViewMatrixLocation() {
        return viewMatrixLocation;
    }

    public int getModelMatrixLocation() {
        return modelMatrixLocation;
    }

    public int getProjectionMatrixLocation() {
        return projectionMatrixLocation;
    }
}
