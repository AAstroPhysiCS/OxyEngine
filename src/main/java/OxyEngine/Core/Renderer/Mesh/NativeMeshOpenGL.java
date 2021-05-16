package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Buffer.VertexBuffer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;

import static OxyEngine.System.OxySystem.oxyAssert;

public class NativeMeshOpenGL extends OpenGLMesh {

    public NativeMeshOpenGL(OxyPipeline pipeline) {
        assert pipeline != null : oxyAssert("Some arguments not defined!");
        this.mode = pipeline.getRenderPass().getMeshRenderingMode();
        vertexBuffer = VertexBuffer.create(pipeline, MeshUsage.STATIC);
        indexBuffer = IndexBuffer.create(pipeline);
    }
}