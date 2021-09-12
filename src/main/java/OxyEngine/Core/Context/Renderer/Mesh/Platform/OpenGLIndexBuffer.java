package OxyEngine.Core.Context.Renderer.Mesh.Platform;

import OxyEngine.Core.Context.Renderer.Mesh.IndexBuffer;

import static OxyEngine.Utils.copy;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class OpenGLIndexBuffer extends IndexBuffer {

    private OpenGLIndexBuffer(int[] data) {
        super(data);
        bufferId = glCreateBuffers();
    }

    private OpenGLIndexBuffer(int allocationSize){
        super(new int[allocationSize]);
        bufferId = glCreateBuffers();
    }

    private OpenGLIndexBuffer(OpenGLIndexBuffer other) {
        super(other.data.clone());
        bufferId = glCreateBuffers();
    }

    @Override
    public void load() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_STATIC_DRAW);
    }

    @Override
    public void addToBuffer(int[] data) {
        if (data == null) return;
        if (this.data == null || this.data.length == 0) {
            this.data = data;
            return;
        }
        this.data = copy(this.data, data);
    }

    @Override
    public void dispose() {
        data = null;
        glDeleteBuffers(bufferId);
        bufferId = 0;
    }
}