package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.Core.Context.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Buffer.VertexBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;

public class NativeMeshOpenGL extends OpenGLMesh {

    public NativeMeshOpenGL(OxyPipeline pipeline) {
        super(pipeline);
        this.mode = pipeline.getRenderPass().getMeshRenderingMode();
        vertexBuffer = VertexBuffer.create(pipeline, MeshUsage.STATIC);
        indexBuffer = IndexBuffer.create(pipeline);
    }

    public void pushVertices(float[] vertices){
        vertexBuffer.addToBuffer(vertices);
    }

    public void pushIndices(int[] indices){
        indexBuffer.addToBuffer(indices);
    }
}