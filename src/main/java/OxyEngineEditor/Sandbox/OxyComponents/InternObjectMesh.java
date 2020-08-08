package OxyEngineEditor.Sandbox.OxyComponents;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Sandbox.Scene.InternObjects.OxyInternObject;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class InternObjectMesh extends Mesh {

    public static final BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 10 * Float.BYTES, 0);
    public static final BufferTemplate.Attributes attributesTXCoords = new BufferTemplate.Attributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 10 * Float.BYTES, 3 * Float.BYTES);
    public static final BufferTemplate.Attributes attributesTXSlot = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 10 * Float.BYTES, 5 * Float.BYTES);
    public static final BufferTemplate.Attributes attributesColors = new BufferTemplate.Attributes(OxyShader.COLOR, 4, GL_FLOAT, false, 10 * Float.BYTES, 6 * Float.BYTES);

    public int indicesX, indicesY, indicesZ;

    private InternObjectMesh(int mode, VertexBuffer vertexBuffer, IndexBuffer indexBuffer) {
        this.mode = mode;
        this.indexBuffer = indexBuffer;
        this.vertexBuffer = vertexBuffer;
    }

    interface GameObjectMeshBuilder {

        GameObjectMeshBuilder setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers);

        GameObjectMeshBuilder setMode(int mode);

        GameObjectMeshBuilder setUsage(BufferTemplate.Usage usage);

        InternObjectMesh create();
    }

    public static class GameObjectMeshBuilderImpl implements GameObjectMeshBuilder {

        private BufferTemplate.Attributes[] verticesPointers;
        private int mode = -1;
        private BufferTemplate.Usage usage;

        @Override
        public GameObjectMeshBuilderImpl setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers) {
            this.verticesPointers = verticesPointers;
            return this;
        }

        @Override
        public GameObjectMeshBuilderImpl setMode(int mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public GameObjectMeshBuilderImpl setUsage(BufferTemplate.Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public InternObjectMesh create() {
            if (mode == -1 || usage == null)
                throw new IllegalArgumentException("Some arguments not defined!");

            return new InternObjectMesh(mode,
                    new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                            .setVerticesStrideSize(attributesVert.stride() / Float.BYTES)
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
