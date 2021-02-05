package OxyEngine.Core.Camera;

import OxyEngine.Core.Camera.Controller.OxyCameraController;
import OxyEngine.Core.Camera.Controller.PerspectiveCameraController;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngineEditor.UI.Panels.ScenePanel;
import imgui.ImGui;
import imgui.ImGuiIO;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PerspectiveCamera extends OxyCamera {

    protected float fovY, zNear, zFar;
    protected float aspect;

    public static float zoom = 800 * 0.06f;
    public final boolean primary;

    public PerspectiveCamera(boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose) {
        this(primary, fovY, aspect, zNear, zFar, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), transpose);
    }

    public PerspectiveCamera(float windowWidth, float windowHeight){
        this(true, Math.toRadians(50), windowWidth / windowHeight, 1f, 10000f, true, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
    }

    public PerspectiveCamera(boolean primary, float fovY, float aspect, float zNear, float zFar, Vector3f translation, Vector3f rotation, boolean transpose) {
        super(transpose);
        this.primary = primary;
        this.fovY = fovY;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;

        cameraController = new PerspectiveCameraController(translation, rotation);
    }

    public PerspectiveCamera(boolean primary, float fovY, float aspect, float zNear, float zFar, boolean transpose, Vector3f center, Vector3f rotation) {
        this(primary, fovY, aspect, zNear, zFar, center, rotation, transpose);
    }

    @Override
    public Matrix4f setModelMatrix() {
        Matrix4f m = new Matrix4f();
        m.translate(0, 0, -zoom);
        m.rotateX(cameraController.getRotation().x);
        m.rotateY(cameraController.getRotation().y);
        m.translate(-cameraController.getPosition().x, -cameraController.getPosition().y, -cameraController.getPosition().z);
        return m;
    }

    @Override
    public Matrix4f setProjectionMatrix() {
        Matrix4f m = new Matrix4f();
        m.perspective(fovY, aspect, zNear, zFar);
        return m;
    }

    public void setViewMatrixNoTranslation() {
        viewMatrixNoTranslation = new Matrix4f();
        viewMatrixNoTranslation.set(setProjectionMatrix());
        viewMatrixNoTranslation.rotateX(cameraController.getRotation().x);
        viewMatrixNoTranslation.rotateY(cameraController.getRotation().y);
    }

    static final float zoomSpeed = 250f;

    @Override
    public void finalizeCamera(float ts) {
        ImGuiIO io = ImGui.getIO();
        if (ScenePanel.hoveredWindow) {
            if (io.getMouseWheel() > 0) {
                zoom += zoomSpeed * ts;
            } else if (io.getMouseWheel() < 0) {
                zoom += -zoomSpeed * ts;
            }
            if (zoom >= 500) zoom = 500;
            if (zoom <= -500) zoom = -500;
        }

        cameraController.update(ts, OxyCameraController.Mode.SWIPE);
        modelMatrix = setModelMatrix();
        projectionMatrix = setProjectionMatrix();
        viewMatrix = new Matrix4f();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
        viewMatrix.origin(cameraController.origin);

        //For skybox
        setViewMatrixNoTranslation();
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

    public boolean isPrimary() {
        return primary;
    }
}
