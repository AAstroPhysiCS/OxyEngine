package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
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

    /**
     * Draws automatically with the bounded texture.
     */
    @Override
    public void render(Mesh mesh, OxyCamera camera) {
        shader.enable();
        OxyRenderer.currentBoundedCamera = camera;
        shader.setCamera(camera);
        if (shader == null) {
            logger.severe("Shader is not instantiated.");
            throw new NullPointerException("Shader is not instantiated.");
        }
        if(mesh instanceof GameObjectMesh m)
            renderImpl(m, camera);
        else if(mesh instanceof ModelMesh m)
            renderImpl(m, camera);
        shader.disable();
    }

    @Override
    public void render(Mesh mesh) {
        render(mesh, currentBoundedCamera);
    }

    @Override
    protected void renderImpl(GameObjectMesh mesh, OxyCamera camera) {
        if(mesh.getOxyObjectType() == null) {
            logger.severe("Object list or type not instantiated/defined.");
            throw new NullPointerException("Object list or type not instantiated/defined.");
        }
        if (mesh.empty()) {
            mesh.load();
        }
        mesh.render();
//        Stats.totalShapeCount = mesh.getOxyEntityList().size();
    }

    @Override
    protected void renderImpl(ModelMesh mesh, OxyCamera camera) {
        if(mesh.empty())
            mesh.load();
        mesh.render();
    }
}
