package OxyEngine.Core.Renderer.Mesh.Platform;

import OxyEngine.Core.Renderer.Mesh.Buffer;
import OxyEngine.Core.Renderer.Mesh.BufferUsage;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class OpenGLEmptyBuffer extends Buffer<Object> {

    private final int size, type;
    private final BufferUsage usage;

    public OpenGLEmptyBuffer(int type, int size, BufferUsage usage) {
        super(null);
        this.size = size;
        this.type = type;
        this.usage = usage;
        bufferId = glCreateBuffers();
    }

    @Override
    public void load() {
        glBindBuffer(type, bufferId);
        glBufferData(type, size, usage == BufferUsage.STATIC ? GL_STATIC_DRAW : GL_DYNAMIC_DRAW);
    }

    @Override
    public void dispose() {
        glDeleteBuffers(bufferId);
    }
}
