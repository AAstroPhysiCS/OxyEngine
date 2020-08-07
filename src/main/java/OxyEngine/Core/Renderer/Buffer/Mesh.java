package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import OxyEngineEditor.Sandbox.Scene.Scene;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL45.*;

public abstract class Mesh implements OxyDisposable, EntityComponent {

    protected IndexBuffer indexBuffer;
    protected VertexBuffer vertexBuffer;
    protected TextureBuffer textureBuffer;

    protected final List<OxyEntity> entities = new ArrayList<>();

    protected int mode, vao;

    public boolean empty() {
        return indexBuffer.empty() && vertexBuffer.empty();
    }

    public IndexBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void load() {
        if (vao == 0) vao = glCreateVertexArrays();
        glBindVertexArray(vao);

        vertexBuffer.load();
        indexBuffer.load();
        if (textureBuffer != null) if (textureBuffer.empty()) textureBuffer.load();

        if (vertexBuffer.getImplementation().getUsage() == BufferTemplate.Usage.DYNAMIC) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getBufferId());
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.getVertices());
        }
    }

    private void draw() {
        glDrawElements(mode, indexBuffer.length(), GL_UNSIGNED_INT, 0);
    }

    private void bind() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);
        if (vertexBuffer.getImplementation().getUsage() == BufferTemplate.Usage.DYNAMIC && vertexBuffer.offsetToUpdate != -1) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.bufferId);
            glBufferSubData(GL_ARRAY_BUFFER, vertexBuffer.offsetToUpdate, vertexBuffer.dataToUpdate);

            vertexBuffer.offsetToUpdate = -1;
        }
    }

    private void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        OxyRenderer.Stats.drawCalls++;
        OxyRenderer.Stats.totalVertexCount += vertexBuffer.getVertices().length;
        OxyRenderer.Stats.totalIndicesCount += indexBuffer.getIndices().length;
    }

    public void addToList(OxyEntity e) {
        entities.add(e);
    }

    public void initList() {
        vertexBuffer.addToBuffer(OxyEntity.sumAllVertices(entities));
        indexBuffer.addToBuffer(OxyEntity.sumAllIndices(entities));

        load();
    }

    public void clear() {
        vertexBuffer.clear();
        indexBuffer.clear();
    }

    public void render() {
        bind();
        draw();
        unbind();
    }

    public void updateSingleEntityData(Scene scene, OxyEntity e) {
        int i = 0;
        for (OxyEntity entity : scene.getEntities()) {
            if (entity.equals(e)) {
                vertexBuffer.updateSingleEntityData(i * e.getType().n_Vertices(), e.getVertices());
                break;
            }
            i++;
        }
    }

    public void updateSingleEntityData(int offsetToUpdate, float[] dataToUpdate) {
        vertexBuffer.updateSingleEntityData(offsetToUpdate, dataToUpdate);
    }

    @Override
    public void dispose() {
        vertexBuffer.dispose();
        indexBuffer.dispose();
        glDeleteVertexArrays(vao);
    }
}
