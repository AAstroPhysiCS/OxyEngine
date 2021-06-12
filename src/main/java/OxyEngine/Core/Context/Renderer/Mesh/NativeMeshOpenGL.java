package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.Core.Context.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Buffer.VertexBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;

import static OxyEngine.System.OxySystem.oxyAssert;

public class NativeMeshOpenGL extends OpenGLMesh {

    public NativeMeshOpenGL(OxyPipeline pipeline) {
        assert pipeline != null : oxyAssert("Some arguments not defined!");
        this.mode = pipeline.getRenderPass().getMeshRenderingMode();
        vertexBuffer = VertexBuffer.create(pipeline, MeshUsage.STATIC);
        indexBuffer = IndexBuffer.create(pipeline);
    }
}