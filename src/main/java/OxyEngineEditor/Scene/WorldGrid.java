package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyMaterial;
import OxyEngineEditor.Scene.NativeObjects.GridFactory;
import OxyEngineEditor.Scene.NativeObjects.OxyNativeObject;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngineEditor.Components.NativeObjectMesh.*;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private final Scene scene;
    private final NativeObjectMesh worldGridMesh;

    public WorldGrid(Scene scene, int size, OxyShader shader) {
        this.scene = scene;
        worldGridMesh = new NativeMeshBuilderImpl()
                .setShader(shader)
                .setMode(GL_LINES)
                .setUsage(BufferTemplate.Usage.STATIC)
                .setVerticesBufferAttributes(attributesVert, attributesTXSlot, attributesColors)
                .create();
        add(size);
        worldGridMesh.initList();
    }

    private void add(int size) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                OxyNativeObject obj = scene.createNativeObjectEntity();
                obj.addComponent(worldGridMesh, new GridFactory(), new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 0.2f)), new TransformComponent(new Vector3f(x, 0, z), new Vector3f(0, 0, 0), 20f));
                obj.initData(null);
            }
        }
    }
}