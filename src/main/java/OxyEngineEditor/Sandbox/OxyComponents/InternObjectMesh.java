package OxyEngineEditor.Sandbox.OxyComponents;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Sandbox.Scene.InternObjects.OxyInternObject;

import static OxyEngine.System.OxySystem.oxyAssert;

public class InternObjectMesh extends Mesh {

    public int indicesX, indicesY, indicesZ;

    private InternObjectMesh(OxyShader shader, int mode, boolean renderable, VertexBuffer vertexBuffer, IndexBuffer indexBuffer) {
        this.renderable = renderable;
        this.shader = shader;
        this.mode = mode;
        this.indexBuffer = indexBuffer;
        this.vertexBuffer = vertexBuffer;
    }

    interface InternMeshBuilder {

        InternMeshBuilder setShader(OxyShader shader);

        InternMeshBuilder setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers);

        InternMeshBuilder setMode(int mode);

        InternMeshBuilder setUsage(BufferTemplate.Usage usage);

        InternMeshBuilder isRenderable(boolean renderable);

        InternObjectMesh create();
    }

    public static class InternMeshBuilderImpl implements InternMeshBuilder {

        private BufferTemplate.Attributes[] verticesPointers;
        private int mode = -1;
        private BufferTemplate.Usage usage;
        private OxyShader shader;
        private boolean renderable = true;

        @Override
        public InternMeshBuilderImpl setShader(OxyShader shader) {
            this.shader = shader;
            return this;
        }

        @Override
        public InternMeshBuilderImpl setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers) {
            this.verticesPointers = verticesPointers;
            return this;
        }

        @Override
        public InternMeshBuilderImpl setMode(int mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public InternMeshBuilderImpl setUsage(BufferTemplate.Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public InternMeshBuilderImpl isRenderable(boolean renderable) {
            this.renderable = renderable;
            return this;
        }

        @Override
        public InternObjectMesh create() {
            assert mode != -1 && usage != null : oxyAssert("Some arguments not defined!");

            return new InternObjectMesh(shader, mode, renderable,
                    new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                            .setVerticesStrideSize(verticesPointers[0].stride() / Float.BYTES)
                            .setUsage(usage)
                            .setAttribPointer(verticesPointers)),
                    new IndexBuffer());
        }
    }

    public void addToBuffer(OxyInternObject oxyEntity) {
        entities.add(oxyEntity);
        vertexBuffer.addToBuffer(oxyEntity);
        indexBuffer.addToBuffer(oxyEntity);

        load();
    }

    public void addToBuffer(float[] vertices, int[] indices) {
        vertexBuffer.addToBuffer(vertices);
        indexBuffer.addToBuffer(indices);

        load();
    }
}
