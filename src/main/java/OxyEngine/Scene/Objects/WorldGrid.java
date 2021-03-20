package OxyEngine.Scene.Objects;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Buffer.BufferLayoutProducer;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scene.Objects.Native.NativeObjectFactory;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.Scene;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private final Scene scene;
    private final NativeObjectMeshOpenGL worldGridMesh;

    public static final OxyShader shader = new OxyShader("shaders/OxyGrid.glsl");

    public WorldGrid(Scene scene, int size) {
        this.scene = scene;
        worldGridMesh = new NativeObjectMeshOpenGL(GL_LINES, BufferLayoutProducer.Usage.STATIC,
                NativeObjectMeshOpenGL.attributeVert);
        add(size);
        worldGridMesh.addToQueue();
    }

    private void add(int size) {
        OxyNativeObject mainObj = scene.createNativeObjectEntity(size * size * 4);
        mainObj.setFactory(new GridFactory());
        mainObj.addComponent(shader, worldGridMesh);
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                mainObj.pushVertexData(new TransformComponent(new Vector3f(x, 0, z), 2f));
            }
        }
    }

    private static class GridFactory implements NativeObjectFactory {

        protected final float[] vertexPos;
        protected final int vertexSize = 32;

        public GridFactory() {
            vertexPos = new float[]{
                    -0.5f, 0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
            };
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

            if (e.vertices == null) e.vertices = new float[size * vertexSize];
            for (int i = 0; i < vertexPos.length; ) {
                Vector4f transformed = new Vector4f(vertexPos[i++], vertexPos[i++], vertexPos[i++], 1.0f).mul(c.transform);
                e.vertices[vertPtr++] = transformed.x;
                e.vertices[vertPtr++] = transformed.y;
                e.vertices[vertPtr++] = transformed.z;
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