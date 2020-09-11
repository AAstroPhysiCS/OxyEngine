package OxyEngine.Core.Renderer.Buffer;

import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public final class IndexBuffer extends Buffer {

    private int length;
    private int[] indices = new int[0];

    public IndexBuffer() {
    }

    @Override
    public void load() {
        if(indices.length == 0) return;
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        this.length = indices.length;
    }

    public void addToBuffer(OxyNativeObject oxyEntity) {
        addToBuffer(oxyEntity.getIndices());
    }

    public void addToBuffer(int[] m_indices){
        if(m_indices == null) return;
        if(indices == null){
            this.indices = m_indices;
            return;
        }
        copy(m_indices);
    }

    private void copy(int[] m_indices){
        int[] newObjInd = new int[indices.length + m_indices.length];
        System.arraycopy(indices, 0, newObjInd, 0, indices.length);
        System.arraycopy(m_indices, 0, newObjInd, indices.length, m_indices.length);
        this.indices = newObjInd;
    }

    public int length() {
        return length;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    @Override
    public void dispose() {
        indices = null;
        glDeleteBuffers(bufferId);
    }
}