package OxyEngine.Core.Context.Renderer.Buffer.Platform;

import OxyEngine.Core.Context.Renderer.Buffer.UniformBuffer;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.nio.FloatBuffer;

import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.opengl.GL45.*;

public final class OpenGLUniformBuffer extends UniformBuffer {

    private final int size;
    private final int binding;

    //Java stuff (all in bytes)
    private static final FloatBuffer MATRIX4X4_BUFFER = createFloatBuffer(4 * 4);
    private static final FloatBuffer VEC4_BUFFER = createFloatBuffer(4);
    private static final FloatBuffer VEC3_BUFFER = createFloatBuffer(3);

    public OpenGLUniformBuffer(int size, int binding) {
        this.size = size;
        this.binding = binding;
        load();
    }

    @Override
    protected void load() {
        bufferId = glCreateBuffers();
        glNamedBufferData(bufferId, size, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, binding, bufferId);
    }

    @Override
    public void setData(int offset, int[] data) {
        glNamedBufferSubData(bufferId, offset, data);
    }

    @Override
    public void setData(int offset, float[] data) {
        glNamedBufferSubData(bufferId, offset, data);
    }

    @Override
    public void setData(int offset, Matrix4fc data) {
        data.get(MATRIX4X4_BUFFER);
        glNamedBufferSubData(bufferId, offset, MATRIX4X4_BUFFER);
    }

    @Override
    public void setData(int offset, Vector4fc data) {
        data.get(VEC4_BUFFER);
        glNamedBufferSubData(bufferId, offset, VEC4_BUFFER);
    }

    @Override
    public void setData(int offset, Vector3fc data) {
        data.get(VEC3_BUFFER);
        glNamedBufferSubData(bufferId, offset, VEC3_BUFFER);
    }

    @Override
    public void dispose() {
        glDeleteBuffers(bufferId);
    }
}
