package OxyEngine.Core.Renderer.Buffer;

public abstract class NormalsBuffer extends Buffer {

    protected float[] normals = new float[0];

    protected final BufferLayoutProducer.BufferLayoutImpl implementation;

    public NormalsBuffer(BufferLayoutProducer.BufferLayoutImpl template) {
        this.implementation = template;
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }

    public boolean emptyData() {
        return normals.length == 0;
    }
}
