package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.System.OxyDisposable;

import static org.lwjgl.opengl.GL45.*;

public abstract class Mesh implements OxyDisposable {

    protected IndexBuffer indexBuffer;
    protected VertexBuffer vertexBuffer;
    protected TextureBuffer textureBuffer;
    protected FrameBuffer frameBuffer;

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

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public void load() {
        if (vao == 0) vao = glCreateVertexArrays();
        glBindVertexArray(vao);

        vertexBuffer.load();
        indexBuffer.load();
        if (textureBuffer != null) if (textureBuffer.empty()) textureBuffer.load();
        if (frameBuffer != null) if (frameBuffer.empty()) frameBuffer.load();
    }

    private void draw() {
        glDrawElements(mode, indexBuffer.length(), GL_UNSIGNED_INT, 0);
    }

    private void bind() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);
        if (vertexBuffer.getImplementation().getUsage() == BufferTemplate.Usage.DYNAMIC) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getBufferId());
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.getVertices());
        }
    }

    private void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        OxyRenderer.Stats.drawCalls++;
        OxyRenderer.Stats.totalVertexCount += vertexBuffer.getVertices().length;
        OxyRenderer.Stats.totalIndicesCount += indexBuffer.getIndices().length;
    }

    public void render() {
        bind();
        draw();
        unbind();
    }

    @Override
    public void dispose() {
        vertexBuffer.dispose();
        indexBuffer.dispose();
        frameBuffer.dispose();
        glDeleteVertexArrays(vao);
    }
}
