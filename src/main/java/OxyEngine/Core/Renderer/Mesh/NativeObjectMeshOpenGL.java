package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLIndexBuffer;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLVertexBuffer;
import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;

public class NativeObjectMeshOpenGL extends OpenGLMesh {

    public int indicesX, indicesY, indicesZ;

    public NativeObjectMeshOpenGL(OxyShader shader, int mode, BufferLayoutProducer.Usage usage, BufferLayoutAttributes... attributes) {
        this.shader = shader;
        this.mode = mode;

        assert mode != -1 && usage != null : oxyAssert("Some arguments not defined!");

        BufferLayoutRecord layout = BufferLayoutProducer.create()
                .createLayout(VertexBuffer.class)
                    .setStrideSize(attributes[0].stride() / Float.BYTES)
                    .setUsage(usage)
                    .setAttribPointer(attributes)
                    .create()
                .createLayout(IndexBuffer.class).create()
                .finalizeLayout();

        vertexBuffer = (OpenGLVertexBuffer) layout.vertexBuffer();
        indexBuffer = (OpenGLIndexBuffer) layout.indexBuffer();
    }
}
