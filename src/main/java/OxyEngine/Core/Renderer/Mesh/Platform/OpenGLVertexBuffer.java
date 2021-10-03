package OxyEngine.Core.Renderer.Mesh.Platform;

import OxyEngine.Core.Renderer.Mesh.BufferUsage;
import OxyEngine.Core.Renderer.Mesh.VertexBuffer;
import OxyEngine.Core.Renderer.Renderer;

import static OxyEngine.Utils.copy;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class OpenGLVertexBuffer extends VertexBuffer {

    private OpenGLVertexBuffer(float[] data, BufferUsage usage) {
        super(data, usage);
        bufferId = glCreateBuffers();
    }

    private OpenGLVertexBuffer(int allocationSize, BufferUsage usage) {
        super(allocationSize, usage);
        bufferId = glCreateBuffers();
    }

    private OpenGLVertexBuffer(OpenGLVertexBuffer other) {
        super(other.data.clone(), other.usage);
        bufferId = glCreateBuffers();
    }

    @Override
    public void load() {
        if (usage == BufferUsage.STATIC) loadStatically();
        else if (usage == BufferUsage.DYNAMIC) loadDynamically();
        else throw new IllegalStateException("How did that happen?");
    }

    private void loadStatically() {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
    }

    private void loadDynamically() {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, (long) data.length * Float.BYTES, GL_DYNAMIC_DRAW);
    }

    @Override
    public void addToBuffer(float[] data) {
        if (data == null) return;
        if (this.data == null || this.data.length == 0) {
            this.data = data;
            return;
        }
        this.data = copy(this.data, data);
    }

    @Override
    public void updateData(int pos, float[] newData) {
        int i = pos;
        for (float newVertex : newData) {
            data[i++] = newVertex;
        }
        if (usage == BufferUsage.DYNAMIC) {
            int offsetToUpdate = pos * Float.BYTES;
            glBindBuffer(GL_ARRAY_BUFFER, bufferId);
            glBufferSubData(GL_ARRAY_BUFFER, offsetToUpdate, newData);
        }
    }

    @Override
    public void dispose() {
        data = null;
        glDeleteBuffers(bufferId);
        bufferId = 0;
    }
}