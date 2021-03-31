package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Buffer.Platform.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class NativeObjectMeshOpenGL extends OpenGLMesh {

    public static final BufferLayoutAttributes attributeVert = new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 0, 0);

    public int indicesX, indicesY, indicesZ;

    public NativeObjectMeshOpenGL(MeshRenderMode mode, BufferLayoutConstructor.Usage usage, BufferLayoutAttributes... attributes) {
        this.mode = mode;

        assert mode != null && usage != null : oxyAssert("Some arguments not defined!");

        BufferLayoutRecord layout = BufferLayoutConstructor.create()
                .createLayout(VertexBuffer.class)
                .setUsage(usage)
                .setAttribPointer(attributes)
                .create()
                .createLayout(IndexBuffer.class).create()
                .finalizeRecord();

        vertexBuffer = (OpenGLVertexBuffer) layout.vertexBuffer();
        indexBuffer = (OpenGLIndexBuffer) layout.indexBuffer();
    }

    public NativeObjectMeshOpenGL(MeshRenderMode mode, BufferLayoutRecord record) {
        this.mode = mode;
        assert mode != null : oxyAssert("Some arguments not defined!");
        vertexBuffer = (OpenGLVertexBuffer) record.vertexBuffer();
        indexBuffer = (OpenGLIndexBuffer) record.indexBuffer();
        normalsBuffer = (OpenGLNormalsBuffer) record.normalsBuffer();
        tangentBuffer = (OpenGLTangentBuffer) record.tangentBuffer();
        textureBuffer = (OpenGLTextureBuffer) record.textureBuffer();
    }
}