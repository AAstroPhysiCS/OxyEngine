package OxyEngineEditor.Components;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Buffer.VertexBuffer;
import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class NativeObjectMesh extends Mesh {

    public static final BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
    public static final BufferTemplate.Attributes attributesTXSlot = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
    public static final BufferTemplate.Attributes attributesColors = new BufferTemplate.Attributes(OxyShader.COLOR, 4, GL_FLOAT, false, 8 * Float.BYTES, 4 * Float.BYTES);

    public int indicesX, indicesY, indicesZ;

    private NativeObjectMesh(OxyShader shader, int mode, VertexBuffer vertexBuffer, IndexBuffer indexBuffer) {
        this.shader = shader;
        this.mode = mode;
        this.indexBuffer = indexBuffer;
        this.vertexBuffer = vertexBuffer;
    }

    interface NativeMeshBuilder {

        NativeMeshBuilder setShader(OxyShader shader);

        NativeMeshBuilder setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers);

        NativeMeshBuilder setMode(int mode);

        NativeMeshBuilder setUsage(BufferTemplate.Usage usage);

        NativeObjectMesh create();
    }

    public static class NativeMeshBuilderImpl implements NativeMeshBuilder {

        private BufferTemplate.Attributes[] verticesPointers;
        private int mode = -1;
        private BufferTemplate.Usage usage;
        private OxyShader shader;

        @Override
        public NativeMeshBuilderImpl setShader(OxyShader shader) {
            this.shader = shader;
            return this;
        }

        @Override
        public NativeMeshBuilderImpl setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers) {
            this.verticesPointers = verticesPointers;
            return this;
        }

        @Override
        public NativeMeshBuilderImpl setMode(int mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public NativeMeshBuilderImpl setUsage(BufferTemplate.Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public NativeObjectMesh create() {
            assert mode != -1 && usage != null : oxyAssert("Some arguments not defined!");

            return new NativeObjectMesh(shader, mode,
                    new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                            .setVerticesStrideSize(attributesVert.stride() / Float.BYTES)
                            .setUsage(usage)
                            .setAttribPointer(verticesPointers)),
                    new IndexBuffer());
        }
    }
}
