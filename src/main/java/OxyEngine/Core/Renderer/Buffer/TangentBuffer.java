package OxyEngine.Core.Renderer.Buffer;

public abstract class TangentBuffer extends Buffer {

    protected float[] biAndTangent = new float[0];

    protected final BufferLayoutConstructor.BufferLayoutImpl implementation;

    public TangentBuffer(BufferLayoutConstructor.BufferLayoutImpl template) {
        this.implementation = template;
    }

    public boolean emptyData(){
        return biAndTangent.length == 0;
    }

    public void setBiAndTangent(float[] biAndTangent) {
        this.biAndTangent = biAndTangent;
    }
}
