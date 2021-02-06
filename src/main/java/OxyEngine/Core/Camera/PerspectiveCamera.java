package OxyEngine.Core.Camera;

import OxyEngine.Core.Layers.UILayer;
import org.joml.Matrix4f;

public abstract class PerspectiveCamera extends OxyCamera {

    protected float fovY, zNear, zFar;
    protected float aspect;

    public static float zoom = 56;

    protected static final float zoomSpeed = 250f;

    public PerspectiveCamera() {
        this(0.05f, 7f, 7f, false, 45, (float) UILayer.getWindowHandle().getWidth() / UILayer.getWindowHandle().getHeight(), 1f, 10000f, true);
    }

    public PerspectiveCamera(float mouseSpeed, float horizontalSpeed, float verticalSpeed, boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(mouseSpeed, horizontalSpeed, verticalSpeed, transpose);
        this.primary = primary;
        this.fovY = fovY;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;
    }

    public void setViewMatrixNoTranslation() {
        viewMatrixNoTranslation = new Matrix4f();
        viewMatrixNoTranslation.set(setProjectionMatrix());
        viewMatrixNoTranslation.rotateX(this.getRotation().x);
        viewMatrixNoTranslation.rotateY(this.getRotation().y);
    }

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
