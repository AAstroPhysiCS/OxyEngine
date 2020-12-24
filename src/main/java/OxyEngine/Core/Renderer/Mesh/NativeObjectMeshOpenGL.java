package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLIndexBuffer;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLVertexBuffer;
import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;

public class NativeObjectMeshOpenGL extends OpenGLMesh {

    public int indicesX, indicesY, indicesZ;

    private NativeObjectMeshOpenGL(OxyShader shader, int mode, OpenGLVertexBuffer vertexBuffer, OpenGLIndexBuffer indexBuffer) {
        this.shader = shader;
        this.mode = mode;
        this.indexBuffer = indexBuffer;
        this.vertexBuffer = vertexBuffer;
    }

    interface NativeMeshBuilder {

        NativeMeshBuilder setShader(OxyShader shader);

        NativeMeshBuilder setVerticesBufferAttributes(BufferLayoutAttributes... verticesPointers);

        NativeMeshBuilder setMode(int mode);

        NativeMeshBuilder setUsage(BufferLayoutProducer.Usage usage);

        NativeObjectMeshOpenGL create();
    }

    public static class NativeMeshBuilderImpl implements NativeMeshBuilder {

        private BufferLayoutAttributes[] verticesPointers;
        private int mode = -1;
        private BufferLayoutProducer.Usage usage;
        private OxyShader shader;

        @Override
        public NativeMeshBuilderImpl setShader(OxyShader shader) {
            this.shader = shader;
            return this;
        }

        @Override
        public NativeMeshBuilderImpl setVerticesBufferAttributes(BufferLayoutAttributes... verticesPointers) {
            this.verticesPointers = verticesPointers;
            return this;
        }

        @Override
        public NativeMeshBuilderImpl setMode(int mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public NativeMeshBuilderImpl setUsage(BufferLayoutProducer.Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public NativeObjectMeshOpenGL create() {
            assert mode != -1 && usage != null : oxyAssert("Some arguments not defined!");

            BufferLayoutRecord layout = BufferLayoutProducer.create()
                    .createLayout(VertexBuffer.class)
                        .setStrideSize(verticesPointers[0].stride() / Float.BYTES)
                        .setUsage(usage)
                        .setAttribPointer(verticesPointers)
                        .create()
                    .createLayout(IndexBuffer.class).create()
                    .finalizeLayout();

            return new NativeObjectMeshOpenGL(shader, mode, (OpenGLVertexBuffer) layout.vertexBuffer(), (OpenGLIndexBuffer) layout.indexBuffer());
        }
    }
}
