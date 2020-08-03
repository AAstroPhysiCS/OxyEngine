package OxyEngine.Core.Camera;

import OxyEngine.Core.Camera.Controller.OxyCameraController;
import OxyEngine.System.OxyTimestep;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.UI.Layers.SceneLayer;
import imgui.ImGui;
import imgui.ImGuiIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PerspectiveCameraComponent extends PerspectiveCamera implements EntityComponent {

    public static int zoom = 50;
    public final boolean primary = false;

    public PerspectiveCameraComponent(float fovY, float aspect, float zNear, float zFar, int location, boolean transpose, Vector3f center, Vector3f rotation) {
        super(fovY, aspect, zNear, zFar, center, rotation, location, transpose);
    }

    @Override
    public Matrix4f setModelMatrix() {
        Matrix4f m = new Matrix4f();
        m.translate(0, 0, -zoom);
        m.rotateX(cameraController.getRotation().x);
        m.rotateY(cameraController.getRotation().y);
        m.translate(-cameraController.getPosition().x, -cameraController.getPosition().y, -cameraController.getPosition().z);
        m.origin(cameraController.origin);
        return m;
    }

    @Override
    public Matrix4f setProjectionMatrix() {
        Matrix4f m = new Matrix4f();
        m.perspective(fovY, aspect, zNear, zFar);
        return m;
    }

    @Override
    public void finalizeCamera(OxyTimestep ts) {
        ImGuiIO io = ImGui.getIO();
        if(SceneLayer.focusedWindow)
            zoom += io.getMouseWheel();

        cameraController.update(ts, OxyCameraController.Mode.SWIPE);
        modelMatrix = setModelMatrix();
        projectionMatrix = setProjectionMatrix();
        viewMatrix = new Matrix4f();
        viewMatrix.set(projectionMatrix);
        viewMatrix.mul(modelMatrix);
    }

    public boolean isPrimary() {
        return primary;
    }
}
