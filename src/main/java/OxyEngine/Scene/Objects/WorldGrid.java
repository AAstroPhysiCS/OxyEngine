package OxyEngine.Scene.Objects;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.VertexBuffer;
import OxyEngine.Core.Context.CullMode;
import OxyEngine.Core.Context.Renderer.Mesh.MeshRenderMode;
import OxyEngine.Core.Context.Renderer.Mesh.NativeMeshOpenGL;
import OxyEngine.Core.Context.OxyRenderPass;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderType;
import OxyEngine.Scene.Objects.Model.OxyNativeObject;
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
        build(scene, size);
    }

    public static void initPipeline() {
        OxyShader gridShader = ShaderLibrary.get("OxyGrid");

        OxyRenderPass gridRenderPass = OxyRenderPass.createBuilder(SceneRenderer.getInstance().getMainFrameBuffer())
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

    protected final float[] vertexPos = new float[]{
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
    };

    protected final int vertexSize = 32;

    private int indicesPtr = 0;
    private int indicesX, indicesY, indicesZ;

    private void build(Scene scene, int gridSize) {

        final int finalSize = gridSize * gridSize * 4;
        TransformComponent[] components = new TransformComponent[finalSize];

        int index = 0;
        for (int x = -gridSize; x < gridSize; x++) {
            for (int z = -gridSize; z < gridSize; z++) {
                components[index] = new TransformComponent(new Vector3f(x, 0, z), 2f);
                components[index].transform = new Matrix4f()
                        .scale(components[index].scale)
                        .translate(components[index].position)
                        .rotateX(components[index].rotation.x)
                        .rotateY(components[index].rotation.y)
                        .rotateZ(components[index].rotation.z);
                index++;
            }
        }

        float[] vertices = new float[finalSize * vertexSize];
        int[] indices = new int[6 * gridSize * components.length];

        index = 0;
        for (TransformComponent component : components) {
            Matrix4f c = component.transform;
            for (int i = 0; i < vertexPos.length; ) {
                Vector4f transformed = new Vector4f(vertexPos[i++], vertexPos[i++], vertexPos[i++], 1.0f).mul(c);
                vertices[index++] = transformed.x;
                vertices[index++] = transformed.y;
                vertices[index++] = transformed.z;
            }
            int[] indicesTemplate = new int[]{
                    indicesX, 1 + indicesY, 3 + indicesZ,
                    3 + indicesX, indicesY, 2 + indicesZ,
            };
            for (int i : indicesTemplate) {
                indices[indicesPtr++] = i;
            }
            indicesX += 4;
            indicesY += 4;
            indicesZ += 4;
        }

        var worldGridMesh = new NativeMeshOpenGL(gridPipeline);
        grid = scene.createNativeObjectEntity(vertices, indices);
        grid.addComponent(worldGridMesh);
        worldGridMesh.load(gridPipeline);
        SceneRenderer.getInstance().updateNativeEntities();
    }

    public static OxyPipeline getPipeline() {
        return gridPipeline;
    }
}