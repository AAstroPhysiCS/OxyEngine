package OxyEngineEditor.Scene.Objects;

import OxyEngine.Core.Renderer.Buffer.BufferLayoutAttributes;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Native.GridFactory;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private final Scene scene;
    private final NativeObjectMeshOpenGL worldGridMesh;

    public WorldGrid(Scene scene, int size) {
        this.scene = scene;
        OxyShader shader = new OxyShader("shaders/OxyGrid.glsl");
        worldGridMesh = new NativeMeshBuilderImpl()
                .setShader(shader)
                .setMode(GL_LINES)
                .setUsage(BufferLayoutProducer.Usage.STATIC)
                .setVerticesBufferAttributes(
                        new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 4 * Float.BYTES, 0),
                        new BufferLayoutAttributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 4 * Float.BYTES, 3 * Float.BYTES)
                )
                .create();
        add(size);
        worldGridMesh.initList();
    }

    private void add(int size) {
        OxyNativeObject mainObj = scene.createNativeObjectEntity(size * size * 4);
        mainObj.addComponent(worldGridMesh, new GridFactory(), new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 0.2f)));
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                mainObj.pushVertexData(new TransformComponent(new Vector3f(x, 0, z), 2f));
            }
        }
    }
}