package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Window.WindowHandle;

import static OxyEngine.System.OxySystem.logger;

public class OxyRenderer3D extends OxyRenderer {

    private static OxyRenderer3D INSTANCE = null;

    private OxyRenderer3D(WindowHandle windowHandle) {
        super(windowHandle);
        type = OxyRendererType.Oxy3D;
    }

    public static OxyRenderer3D getInstance(WindowHandle windowHandle) {
        if (INSTANCE == null) INSTANCE = new OxyRenderer3D(windowHandle);
        return INSTANCE;
    }

    @Override
    public void render(float ts, Mesh mesh, OxyCamera camera) {
        shader.enable();
        OxyRenderer.currentBoundedCamera = camera;
        shader.setUniformVec3("cameraPos", camera.getCameraController().origin);
        camera.finalizeCamera(ts);
        shader.setCamera(camera);
        if (shader == null) {
            logger.severe("Shader is not instantiated.");
            throw new NullPointerException("Shader is not instantiated.");
        }
        if (mesh.empty()) {
            mesh.load();
        }
        mesh.render();
        shader.disable();
    }

    @Override
    public void render(float ts, Mesh mesh) {
        if(currentBoundedCamera != null)
            render(ts, mesh, currentBoundedCamera);
    }
}
