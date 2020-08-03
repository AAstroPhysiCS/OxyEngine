package OxyEngine.Core.Camera;

import OxyEngine.Core.Camera.Controller.PerspectiveCameraController;
import OxyEngine.System.OxyTimestep;
import OxyEngine.Tools.Ref;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class PerspectiveCamera extends OxyCamera {

    protected final float fovY, zNear, zFar;
    protected float aspect;

    public PerspectiveCamera(float fovY, float aspect, float zNear, float zFar, int location, boolean transpose) {
        this(fovY, aspect, zNear, zFar, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), location, transpose);
    }

    public PerspectiveCamera(float fovY, float aspect, float zNear, float zFar, Vector3f translation, Vector3f rotation, int location, boolean transpose) {
        super(location, transpose);
        this.fovY = fovY;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;

        cameraController = new PerspectiveCameraController(new Ref<>(translation), new Ref<>(rotation));
    }

    public PerspectiveCamera(float fovY, float aspect, float zNear, float zFar, Ref<Vector3f> translation, Ref<Vector3f> rotation, int location, boolean transpose) {
        this(fovY, aspect, zNear, zFar, translation.obj, rotation.obj, location, transpose);
    }

    @Override
    public abstract Matrix4f setModelMatrix();

    @Override
    public abstract Matrix4f setProjectionMatrix();

    @Override
    public abstract void finalizeCamera(OxyTimestep ts);

    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }
}
