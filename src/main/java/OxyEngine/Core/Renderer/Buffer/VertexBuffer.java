package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Scene.Objects.Native.OxyNativeObject;

import static OxyEngine.System.OxySystem.oxyAssert;

public abstract class VertexBuffer extends Buffer {

    protected float[] vertices = new float[0];
    protected final BufferLayoutProducer.BufferLayoutImpl impl;

    protected int offsetToUpdate = -1;
    protected float[] dataToUpdate;

    public VertexBuffer(BufferLayoutProducer.BufferLayoutImpl impl) {
        this.impl = impl;

        assert impl.getUsage() != null && impl.getStrideSize() != -1 && impl.getAttribPointers() != null : oxyAssert("Some Implementation arguments are null");
    }

    public abstract void updateSingleEntityData(int pos, float[] newVertices);

    protected abstract void copy(float[] m_Vertices);

    public BufferLayoutProducer.BufferLayoutImpl getImplementation() {
        return impl;
    }

    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public abstract void addToBuffer(OxyNativeObject oxyEntity);

    public abstract void addToBuffer(float[] m_Vertices);
}
