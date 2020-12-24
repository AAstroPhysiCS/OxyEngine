package OxyEngine.Core.Renderer.Buffer;

import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;

public abstract class IndexBuffer extends Buffer {

    protected int length;
    protected int[] indices;

    public IndexBuffer() {
    }

    protected abstract void copy(int[] m_indices);

    public int length() {
        return length;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public abstract void addToBuffer(OxyNativeObject oxyEntity);

    public abstract void addToBuffer(int[] m_indices);
}
