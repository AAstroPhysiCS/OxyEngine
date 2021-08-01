package OxyEngine.Core.Context.Renderer.Buffer.Platform;

import OxyEngine.Core.Context.Renderer.Buffer.IndexBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Scene.OxyNativeObject;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class OpenGLIndexBuffer extends IndexBuffer {

    OpenGLIndexBuffer(OxyPipeline.Layout layout) {
        super(layout);
    }

    @Override
    public void load() {
        if (indices == null) return;
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        this.length = indices.length;
    }

    @Override
    public void addToBuffer(OxyNativeObject oxyEntity) {
        addToBuffer(oxyEntity.getIndices());
    }

    @Override
    public void addToBuffer(int[] m_indices) {
        if (m_indices == null) return;
        if (indices == null) {
            this.indices = m_indices;
            return;
        }
        copy(m_indices);
    }

    @Override
    protected void copy(int[] m_indices) {
        int[] newObjInd = new int[indices.length + m_indices.length];
        System.arraycopy(indices, 0, newObjInd, 0, indices.length);
        System.arraycopy(m_indices, 0, newObjInd, indices.length, m_indices.length);
        this.indices = newObjInd;
    }

    @Override
    public void dispose() {
        indices = null;
        glDeleteBuffers(bufferId);
        bufferId = 0;
    }
}