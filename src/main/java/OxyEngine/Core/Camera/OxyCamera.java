package OxyEngine.Core.Camera;

import OxyEngine.Core.Camera.Controller.OxyCameraController;
import OxyEngineEditor.Sandbox.Components.EntityComponent;
import org.joml.Matrix4f;

public abstract class OxyCamera implements EntityComponent {

    protected final boolean transpose;
    protected Matrix4f viewMatrix, modelMatrix, projectionMatrix, viewMatrixNoTranslation;

    protected OxyCameraController cameraController;

    public OxyCamera(boolean transpose) {
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

    public Matrix4f getViewMatrixNoTranslation() {
        return viewMatrixNoTranslation;
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
}
