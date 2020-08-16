package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;

import static OxyEngine.System.OxySystem.oxyAssert;

public class OxyRenderer3D extends OxyRenderer {

    private static OxyRenderer3D INSTANCE = null;

    private OxyRenderer3D(WindowHandle windowHandle) {
        super(windowHandle);
        type = OxyRendererType.Oxy3D;
    }

    @Override
    public void begin(OxyShader shader) {
        currentShader = shader;
    }

    @Override
    public void end(OxyShader shader) {
        currentShader.disable();

    }

    public static OxyRenderer3D getInstance(WindowHandle windowHandle) {
        if (INSTANCE == null) INSTANCE = new OxyRenderer3D(windowHandle);
        return INSTANCE;
    }

    @Override
    public void render(float ts, Mesh mesh, OxyCamera camera) {
        OxyShader shader = mesh.getShader();
        assert shader != null : oxyAssert("Shader is not instantiated.");
        shader.enable();
        camera.finalizeCamera(ts);
        shader.setUniformVec3("cameraPos", camera.getCameraController().origin);
        shader.setCamera(camera);
        OxyRenderer.currentBoundedCamera = camera;
        if (mesh.empty())
            mesh.load();
        mesh.render();
        shader.disable();
    }

    @Override
    public void render(float ts, Mesh mesh) {
        render(ts, mesh, currentBoundedCamera);
    }
}
