package OxyEngineEditor.Sandbox.Components;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Buffer.VertexBuffer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Sandbox.Scene.NativeObjects.OxyNativeObject;

import static OxyEngine.System.OxySystem.oxyAssert;

public class NativeObjectMesh extends Mesh {

    public int indicesX, indicesY, indicesZ;

    private NativeObjectMesh(OxyShader shader, int mode, RenderableComponent component, VertexBuffer vertexBuffer, IndexBuffer indexBuffer) {
        this.renderableComponent = component;
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

        NativeMeshBuilder setRenderableComponent(RenderableComponent renderable);

        NativeObjectMesh create();
    }

    public static class NativeMeshBuilderImpl implements NativeMeshBuilder {

        private BufferTemplate.Attributes[] verticesPointers;
        private int mode = -1;
        private BufferTemplate.Usage usage;
        private OxyShader shader;
        private RenderableComponent component = new RenderableComponent(true);

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
        public NativeMeshBuilderImpl setRenderableComponent(RenderableComponent component) {
            this.component = component;
            return this;
        }

        @Override
        public NativeObjectMesh create() {
            assert mode != -1 && usage != null : oxyAssert("Some arguments not defined!");

            return new NativeObjectMesh(shader, mode, component,
                    new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                            .setVerticesStrideSize(verticesPointers[0].stride() / Float.BYTES)
                            .setUsage(usage)
                            .setAttribPointer(verticesPointers)),
                    new IndexBuffer());
        }
    }

    public void addToBuffer(OxyNativeObject oxyEntity) {
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
