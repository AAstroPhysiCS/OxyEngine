package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.InternObjects.GridFactory;
import OxyEngineEditor.Sandbox.Scene.InternObjects.OxyInternObject;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Vector3f;

import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.worldGridMesh;
import static OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh.*;
import static OxyEngineEditor.Sandbox.Sandbox3D.camera;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private final Scene scene;

    public WorldGrid(Scene scene, int size) {
        this.scene = scene;
        worldGridMesh.obj = new GameObjectMesh.GameObjectMeshBuilderImpl()
                .setMode(GL_LINES)
                .setUsage(BufferTemplate.Usage.STATIC)
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlot, attributesColors)
                .create();
        add(size);
        worldGridMesh.obj.initList();
    }

    private void add(int size) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                OxyInternObject e = scene.createInternObjectEntity();
                e.addComponent(camera, worldGridMesh.obj, new GridFactory(), new OxyColor(1.0f, 1.0f, 1.0f, 0.2f), new TransformComponent(new Vector3f(x, 0, z), new Vector3f(0, 0, 0), 20f));
                e.initData();
            }
        }
    }
}