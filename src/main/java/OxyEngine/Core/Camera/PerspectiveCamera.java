package OxyEngine.Core.Camera;

import org.joml.Matrix4f;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;

public abstract class PerspectiveCamera extends OxyCamera {

    protected float fovY, zNear, zFar;
    protected float aspect;

    public static float zoom = 56;

    protected static final float zoomSpeed = 250f;

    public PerspectiveCamera() {
        this(0.05f, 7f, 7f, false, 45, (float) ACTIVE_SCENE.getFrameBuffer().getWidth() / ACTIVE_SCENE.getFrameBuffer().getHeight(), 1f, 10000f, true);
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
        viewMatrixNoTranslation.set(getProjectionMatrix());
        viewMatrixNoTranslation.rotateX(-this.getRotation().x);
        viewMatrixNoTranslation.rotateY(-this.getRotation().y);
    }
    
    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    public float getAspect() {
        return aspect;
    }

    public float getFovY() {
        return fovY;
    }

    public float getZFar() {
        return zFar;
    }

    public float getZNear() {
        return zNear;
    }
}
