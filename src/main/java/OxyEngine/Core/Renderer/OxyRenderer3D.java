package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.System.OxyTimestep;

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
    public void render(OxyTimestep ts, Mesh mesh, OxyCamera camera) {
        shader.enable();
        OxyRenderer.currentBoundedCamera = camera;
        shader.setCamera(ts, camera);
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
    public void render(OxyTimestep ts, Mesh mesh) {
        render(ts, mesh, currentBoundedCamera);
    }
}
