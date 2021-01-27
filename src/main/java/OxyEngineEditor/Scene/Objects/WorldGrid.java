package OxyEngineEditor.Scene.Objects;

import OxyEngine.Components.OxyMaterialIndex;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterialPool;
import OxyEngineEditor.Scene.Objects.Native.NativeObjectFactory;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.Scene;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private final Scene scene;
    private final NativeObjectMeshOpenGL worldGridMesh;

    private static final OxyShader shader = new OxyShader("shaders/OxyGrid.glsl");

    public WorldGrid(Scene scene, int size) {
        this.scene = scene;
        worldGridMesh = new NativeObjectMeshOpenGL(GL_LINES, BufferLayoutProducer.Usage.STATIC,
                NativeObjectMeshOpenGL.attributeVert, NativeObjectMeshOpenGL.attributeTXSlot);
        add(size);
        worldGridMesh.addToQueue();
    }

    private void add(int size) {
        OxyNativeObject mainObj = scene.createNativeObjectEntity(size * size * 4);
        mainObj.setFactory(new GridFactory());
        int index = OxyMaterialPool.addMaterial(new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 0.2f)));
        mainObj.addComponent(shader, worldGridMesh, new OxyMaterialIndex(index));
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                mainObj.pushVertexData(new TransformComponent(new Vector3f(x, 0, z), 2f));
            }
        }
    }

    private static class GridFactory extends NativeObjectFactory {

        protected final float[] vertexPos;

        public GridFactory() {
            vertexPos = new float[]{
                    -0.5f, 0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
            };
            this.vertexSize = 32;
        }
        int vertPtr = 0;

        public void constructData(OxyNativeObject e, int size) {
            TransformComponent c = e.get(TransformComponent.class);

            c.transform = new Matrix4f()
                    .scale(c.scale)
                    .translate(c.position)
                    .rotateX(c.rotation.x)
                    .rotateY(c.rotation.y)
                    .rotateZ(c.rotation.z);

            if (e.vertices == null) e.vertices = new float[vertexSize * size];
            for (int i = 0; i < vertexPos.length; ) {
                Vector4f transformed = new Vector4f(vertexPos[i++], vertexPos[i++], vertexPos[i++], 1.0f).mul(c.transform);
                e.vertices[vertPtr++] = transformed.x;
                e.vertices[vertPtr++] = transformed.y;
                e.vertices[vertPtr++] = transformed.z;
                e.vertices[vertPtr++] = 0;
            }
        }
        int indicesPtr = 0;

        public void initData(OxyNativeObject e, NativeObjectMeshOpenGL mesh) {
            int[] indices = new int[]{
                    mesh.indicesX, 1 + mesh.indicesY, 3 + mesh.indicesZ,
                    3 + mesh.indicesX, mesh.indicesY, 2 + mesh.indicesZ,
            };
            if(e.indices == null) e.indices = new int[indices.length * e.getSize()];
            for (int index : indices) {
                e.indices[indicesPtr++] = index;
            }
            mesh.indicesX += 4;
            mesh.indicesY += 4;
            mesh.indicesZ += 4;
        }
    }

}