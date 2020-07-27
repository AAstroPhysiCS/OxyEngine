package OxyEngineEditor.Sandbox.OxyComponents;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.OxyObjects.GameObjectType;
import OxyEngineEditor.Sandbox.OxyObjects.OxyEntity;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class GameObjectMesh extends Mesh {

    public static final BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
    public static final BufferTemplate.Attributes attributesTXCoords = new BufferTemplate.Attributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
    public static final BufferTemplate.Attributes attributesTXSlots = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOTS, 1, GL_FLOAT, false, 6 * Float.BYTES, 5 * Float.BYTES);

    public int indicesX, indicesY, indicesZ;

    private final GameObjectType type;

    private GameObjectMesh(int mode, GameObjectType type, VertexBuffer vertexBuffer, IndexBuffer indexBuffer, FrameBuffer frameBuffer) {
        this.mode = mode;
        this.indexBuffer = indexBuffer;
        this.vertexBuffer = vertexBuffer;
        this.frameBuffer = frameBuffer;
        this.type = type;
    }

    interface GameObjectMeshBuilder {

        GameObjectMeshBuilder setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers);

        GameObjectMeshBuilder setGameObjectType(GameObjectType gameObjectType);

        GameObjectMeshBuilder runOnFrameBuffer(WindowHandle windowHandle);

        GameObjectMeshBuilder setMode(int mode);

        GameObjectMeshBuilder setUsage(BufferTemplate.Usage usage);

        GameObjectMesh create();
    }

    public static class GameObjectMeshBuilderImpl implements GameObjectMeshBuilder {

        private GameObjectType gameObjectType;
        private static FrameBuffer frameBuffer;
        private BufferTemplate.Attributes[] verticesPointers;
        private int mode = -1;
        private BufferTemplate.Usage usage;

        @Override
        public GameObjectMeshBuilderImpl setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers) {
            this.verticesPointers = verticesPointers;
            return this;
        }

        @Override
        public GameObjectMeshBuilderImpl setGameObjectType(GameObjectType gameObjectType) {
            this.gameObjectType = gameObjectType;
            return this;
        }

        @Override
        public GameObjectMeshBuilderImpl runOnFrameBuffer(WindowHandle windowHandle) {
            if (frameBuffer != null)
                throw new IllegalArgumentException("Frame buffer already bound to a another mesh instance");
            frameBuffer = new FrameBuffer(windowHandle.getWidth(), windowHandle.getHeight());
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
        public GameObjectMesh create() {
            if (mode == -1 || gameObjectType == null || usage == null)
                throw new IllegalArgumentException("Some arguments not defined!");

            return new GameObjectMesh(mode, gameObjectType,
                    new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                            .setVerticesStrideSize(6)
                            .setUsage(usage)
                            .setAttribPointer(verticesPointers)),
                    new IndexBuffer(), frameBuffer);
        }
    }

    public void add(OxyEntity oxyEntity) {
        vertexBuffer.addToBuffer(oxyEntity);
        indexBuffer.addToBuffer(oxyEntity);

        load();
    }

    public void add(List<OxyEntity> list) {
        vertexBuffer.addToBuffer(OxyEntity.sumAllVertices(list, type));
        indexBuffer.addToBuffer(OxyEntity.sumAllIndices(list, type));

        load();
    }

    public void add(float[] vertices, int[] indices) {
        vertexBuffer.addToBuffer(vertices);
        indexBuffer.addToBuffer(indices);

        load();
    }

    public GameObjectType getOxyObjectType() {
        return type;
    }
}
