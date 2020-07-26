package OxyEngine.Core.OxyComponents;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.Core.OxyObjects.GameObject;
import OxyEngine.Core.OxyObjects.OxyEntity;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class GameObjectMeshComponent extends MeshComponent {

    public int indicesX, indicesY, indicesZ;

    private final GameObject.Type type;
    private final List<OxyEntity> oxyEntityList = new ArrayList<>();

    private GameObjectMeshComponent(int mode, GameObject.Type type, VertexBuffer vertexBuffer, IndexBuffer indexBuffer, FrameBuffer frameBuffer) {
        this.mode = mode;
        this.indexBuffer = indexBuffer;
        this.vertexBuffer = vertexBuffer;
        this.frameBuffer = frameBuffer;
        this.type = type;
    }

    public interface BufferAttributes {
        BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        BufferTemplate.Attributes attributesTXCoords = new BufferTemplate.Attributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        BufferTemplate.Attributes attributesTXSlots = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOTS, 1, GL_FLOAT, false, 6 * Float.BYTES, 5 * Float.BYTES);
    }

    interface GameObjectMeshBuilder {

        GameObjectMeshBuilder setVerticesBufferAttributes(BufferTemplate.Attributes... verticesPointers);

        GameObjectMeshBuilder setGameObjectType(GameObject.Type gameObjectType);

        GameObjectMeshBuilder runOnFrameBuffer(WindowHandle windowHandle);

        GameObjectMeshBuilder setMode(int mode);

        GameObjectMeshBuilder setUsage(BufferTemplate.Usage usage);

        GameObjectMeshComponent create();
    }

    public static class GameObjectMeshBuilderImpl implements GameObjectMeshBuilder {

        private GameObject.Type gameObjectType;
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
        public GameObjectMeshBuilderImpl setGameObjectType(GameObject.Type gameObjectType) {
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
        public GameObjectMeshComponent create() {
            if (mode == -1 || gameObjectType == null || usage == null)
                throw new IllegalArgumentException("Some arguments not defined!");

            return new GameObjectMeshComponent(mode, gameObjectType,
                    new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                            .setVerticesStrideSize(6)
                            .setUsage(usage)
                            .setAttribPointer(verticesPointers)),
                    new IndexBuffer(), frameBuffer);
        }
    }

    public void add(OxyEntity oxyEntity) {
        oxyEntityList.add(oxyEntity);
        vertexBuffer.addToBuffer(oxyEntity);
        indexBuffer.addToBuffer(oxyEntity);

        load();
    }

    public void add(List<OxyEntity> list) {
        oxyEntityList.addAll(list);
        vertexBuffer.addToBuffer(GameObject.sumAllVertices(list, type));
        indexBuffer.addToBuffer(GameObject.sumAllIndices(list, type));

        load();
    }

    public void add(float[] vertices, int[] indices) {
        vertexBuffer.addToBuffer(vertices);
        indexBuffer.addToBuffer(indices);

        load();
    }

    public void updateSingleEntityData(OxyEntity entity) {
        for (int i = 0; i < oxyEntityList.size(); i++) {
            OxyEntity e = oxyEntityList.get(i);
            if (e.equals(entity)) {
                vertexBuffer.updateSingleEntityData(i * GameObject.Type.Cube.n_Vertices(), entity.getVertices());
            }
        }
    }

    public GameObject.Type getOxyObjectType() {
        return type;
    }

    public List<OxyEntity> getOxyEntityList() {
        return oxyEntityList;
    }
}
