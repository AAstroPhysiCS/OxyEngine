package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;

import static OxyEngine.System.OxySystem.oxyAssert;

public class NativeObjectMeshOpenGL extends OpenGLMesh {

    public int indicesX, indicesY, indicesZ;

    public NativeObjectMeshOpenGL(OxyPipeline pipeline) {
        assert pipeline != null : oxyAssert("Some arguments not defined!");
        this.mode = pipeline.getRenderPass().getMeshRenderingMode();

        vertexBuffer = VertexBuffer.create(pipeline, MeshUsage.STATIC);
        indexBuffer = IndexBuffer.create(pipeline);
    }
}