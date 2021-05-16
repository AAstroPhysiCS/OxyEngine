package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.System.OxyDisposable;

public abstract class Buffer implements OxyDisposable {

    protected int bufferId;

    protected abstract void load();

    public float[] copy(float[] vertices, float[] m_Vertices) {
        float[] newObjVert = new float[vertices.length + m_Vertices.length];
        System.arraycopy(vertices, 0, newObjVert, 0, vertices.length);
        System.arraycopy(m_Vertices, 0, newObjVert, vertices.length, m_Vertices.length);
        return newObjVert;
    }

    public boolean glBufferNull() {
        return bufferId == 0;
    }

    public int getBufferId() {
        return bufferId;
    }
}
