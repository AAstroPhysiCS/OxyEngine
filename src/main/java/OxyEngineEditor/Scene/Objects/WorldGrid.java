package OxyEngineEditor.Scene.Objects;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Native.GridFactory;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private final Scene scene;
    private final NativeObjectMeshOpenGL worldGridMesh;

    public WorldGrid(Scene scene, int size) {
        this.scene = scene;
        OxyShader shader = new OxyShader("shaders/OxyGrid.glsl");
        worldGridMesh = new NativeObjectMeshOpenGL(shader, GL_LINES, BufferLayoutProducer.Usage.STATIC,
                NativeObjectMeshOpenGL.attributeVert, NativeObjectMeshOpenGL.attributeTXSlot);
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