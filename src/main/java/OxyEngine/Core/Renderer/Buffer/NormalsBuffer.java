package OxyEngine.Core.Renderer.Buffer;

public abstract class NormalsBuffer extends Buffer {

    protected float[] normals = new float[0];

    protected final BufferLayoutConstructor.BufferLayoutImpl implementation;

    public NormalsBuffer(BufferLayoutConstructor.BufferLayoutImpl template) {
        this.implementation = template;
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }

    public boolean emptyData() {
        return normals.length == 0;
    }
}
