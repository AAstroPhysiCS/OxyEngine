package OxyEngine.Core.Renderer;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.OxyObjects.GameObject;
import OxyEngine.Core.OxyComponents.GameObjectMeshComponent;
import OxyEngine.Core.Renderer.Buffer.MeshComponent;
import OxyEngine.Core.OxyComponents.ModelMeshComponent;
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
    public void render(MeshComponent meshComponent, OxyCamera camera) {
        shader.enable();
        OxyRenderer.currentBoundedCamera = camera;
        shader.setCamera(camera);
        if (shader == null) {
            logger.severe("Shader is not instantiated.");
            throw new NullPointerException("Shader is not instantiated.");
        }
        if(meshComponent instanceof GameObjectMeshComponent m)
            renderImpl(m, camera);
        else if(meshComponent instanceof ModelMeshComponent m)
            renderImpl(m, camera);
        shader.disable();
    }

    @Override
    public void render(MeshComponent meshComponent) {
        render(meshComponent, currentBoundedCamera);
    }

    @Override
    protected void renderImpl(GameObjectMeshComponent mesh, OxyCamera camera) {
        if(mesh.getOxyEntityList() == null || mesh.getOxyObjectType() == null) {
            logger.severe("Object list or type not instantiated/defined.");
            throw new NullPointerException("Object list or type not instantiated/defined.");
        }
        if (mesh.empty()) {
            mesh.getVertexBuffer().setVertices(GameObject.sumAllVertices(mesh.getOxyEntityList(), mesh.getOxyObjectType()));
            mesh.getIndexBuffer().setIndices(GameObject.sumAllIndices(mesh.getOxyEntityList(), mesh.getOxyObjectType()));
            mesh.load();
        }
        mesh.render();
        Stats.totalShapeCount = mesh.getOxyEntityList().size();
    }

    @Override
    protected void renderImpl(ModelMeshComponent mesh, OxyCamera camera) {
        if(mesh.empty())
            mesh.load();
        mesh.render();
    }
}
