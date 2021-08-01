package OxyEngine.Core.Camera;

import OxyEngine.Core.Context.Renderer.Mesh.UniformBuffer;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Matrix4f;

public abstract class PerspectiveCamera extends OxyCamera {

    protected float fovY, zNear, zFar;
    protected float aspect;

    protected static UniformBuffer cameraUniformBuffer;

    public static float zoom = 56;

    protected static final float zoomSpeed = 250f;

    public PerspectiveCamera() {
        this(0.05f, 30f, 30f, false, 45, ScenePanel.windowSize.x / ScenePanel.windowSize.y, 1f, 10000f, true);
    }

    public PerspectiveCamera(float mouseSpeed, float horizontalSpeed, float verticalSpeed, boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        super(mouseSpeed, horizontalSpeed, verticalSpeed, transpose);
        this.primary = primary;
        this.fovY = fovY;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;
        viewMatrixNoTranslation = new Matrix4f();
        cameraUniformBuffer = UniformBuffer.create((4 * 4 * Float.BYTES) * 2 + (3 * Float.BYTES), 0);
    }

    public void calcViewMatrixNoTranslation() {
        viewMatrixNoTranslation.set(getProjectionMatrix());
        viewMatrixNoTranslation.rotateX(-this.getRotation().x);
        viewMatrixNoTranslation.rotateY(-this.getRotation().y);
    }

    public static void disposeUniformBuffer() {
        cameraUniformBuffer.dispose();
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
