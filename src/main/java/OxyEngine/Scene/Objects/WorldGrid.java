package OxyEngine.Scene.Objects;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Renderer.Buffer.VertexBuffer;
import OxyEngine.Core.Renderer.CullMode;
import OxyEngine.Core.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderPass;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Renderer.Pipeline.ShaderType;
import OxyEngine.Scene.Objects.Native.NativeObjectFactory;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.Scene;
import OxyEngine.Scene.SceneRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class WorldGrid {

    public static OxyNativeObject grid;
    private static OxyPipeline gridPipeline;

    public WorldGrid(Scene scene, int size) {
        initPipeline();
        var worldGridMesh = new NativeObjectMeshOpenGL(gridPipeline);
        add(worldGridMesh, scene, size);
        worldGridMesh.addToBuffer(gridPipeline);
        SceneRenderer.getInstance().updateNativeEntities();
    }

    public static void initPipeline(){
        OxyShader gridShader = OxyShader.createShader("OxyGrid", "shaders/OxyGrid.glsl");

        OxyRenderPass gridRenderPass = OxyRenderPass.createBuilder(SceneRenderer.getInstance().getFrameBuffer())
                .renderingMode(MeshRenderMode.LINES)
                .setCullFace(CullMode.BACK)
                .create();

        gridPipeline = OxyPipeline.createNewPipeline(OxyPipeline.createNewSpecification()
                .setDebugName("Grid Pipeline")
                .setRenderPass(gridRenderPass)
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(VertexBuffer.class)
                        .set(OxyShader.VERTICES, ShaderType.Float3)
                )
                .createLayout(OxyPipeline.createNewPipelineLayout()
                        .targetBuffer(IndexBuffer.class)
                )
                .setShader(gridShader));
    }

    public static OxyPipeline getPipeline() {
        return gridPipeline;
    }

    private void add(NativeObjectMeshOpenGL worldGridMesh, Scene scene, int size) {
        grid = scene.createNativeObjectEntity(size * size * 4);
        grid.setFactory(new GridFactory());
        grid.addComponent(worldGridMesh);
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                grid.pushVertexData(new TransformComponent(new Vector3f(x, 0, z), 2f));
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
            if (e.indices == null) e.indices = new int[indices.length * e.getSize()];
            for (int index : indices) {
                e.indices[indicesPtr++] = index;
            }
            mesh.indicesX += 4;
            mesh.indicesY += 4;
            mesh.indicesZ += 4;
        }
    }
}