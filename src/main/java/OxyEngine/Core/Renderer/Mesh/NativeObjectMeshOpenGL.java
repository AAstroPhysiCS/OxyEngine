package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Buffer.Platform.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class NativeObjectMeshOpenGL extends OpenGLMesh {

    public static final BufferLayoutAttributes attributeVert = new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 4 * Float.BYTES, 0);
    public static final BufferLayoutAttributes attributeTXSlot = new BufferLayoutAttributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 4 * Float.BYTES, 3 * Float.BYTES);

    public int indicesX, indicesY, indicesZ;

    public NativeObjectMeshOpenGL(int mode, BufferLayoutProducer.Usage usage, BufferLayoutAttributes... attributes) {
        this.mode = mode;

        assert mode != -1 && usage != null : oxyAssert("Some arguments not defined!");

        BufferLayoutRecord layout = BufferLayoutProducer.create()
                .createLayout(VertexBuffer.class)
                    .setStrideSize(attributes[0].stride() / Float.BYTES)
                    .setUsage(usage)
                    .setAttribPointer(attributes)
                .create()
                .createLayout(IndexBuffer.class).create()
                .finalizeRecord();

        vertexBuffer = (OpenGLVertexBuffer) layout.vertexBuffer();
        indexBuffer = (OpenGLIndexBuffer) layout.indexBuffer();
    }

    public NativeObjectMeshOpenGL(int mode, BufferLayoutRecord record) {
        this.mode = mode;
        assert mode != -1 : oxyAssert("Some arguments not defined!");
        vertexBuffer = (OpenGLVertexBuffer) record.vertexBuffer();
        indexBuffer = (OpenGLIndexBuffer) record.indexBuffer();
        normalsBuffer = (OpenGLNormalsBuffer) record.normalsBuffer();
        tangentBuffer = (OpenGLTangentBuffer) record.tangentBuffer();
        textureBuffer = (OpenGLTextureBuffer) record.textureBuffer();
    }
}