package OxyEngine.Core.Renderer.Buffer;

public abstract class TextureBuffer extends Buffer {

    protected final BufferLayoutConstructor.BufferLayoutImpl implementation;

    protected float[] textureCoords;

    public TextureBuffer(BufferLayoutConstructor.BufferLayoutImpl template) {
        this.implementation = template;
        textureCoords = new float[0];
    }

    public void setTextureCoords(float[] textureCoords) {
        this.textureCoords = textureCoords;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public boolean emptyData() {
        return textureCoords.length == 0;
    }
}
