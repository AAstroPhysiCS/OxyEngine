package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngine.Scene.SceneRuntime.currentBoundedCamera;

public class OxyRenderer3D extends OxyRenderer {

    private static OxyRenderer3D INSTANCE = null;

    private OxyRenderer3D() {
        super();
        type = OxyRendererType.Oxy3D;
    }

    public static OxyRenderer3D getInstance() {
        if (INSTANCE == null) INSTANCE = new OxyRenderer3D();
        return INSTANCE;
    }

    public void render(float ts, OpenGLMesh mesh, OxyCamera camera, OxyShader shader){
        assert shader != null : oxyAssert("Shader is not instantiated.");
        shader.enable();
        shader.setUniformVec3("cameraPos", camera.origin);
        shader.setCamera(camera);
        if (mesh.empty())
            mesh.load();
        mesh.render();
        shader.disable();
    }

    public void render(OpenGLMesh mesh, OxyShader shader){
        assert shader != null : oxyAssert("Shader is not instantiated.");
        shader.enable();
        if (mesh.empty())
            mesh.load();
        mesh.render();
        shader.disable();
    }

    @Override
    public void renderWithCurrentBoundedCamera(float ts, OpenGLMesh mesh, OxyShader shader) {
        render(ts, mesh, currentBoundedCamera, shader);
    }
}
