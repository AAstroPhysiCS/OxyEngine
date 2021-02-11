package OxyEngine.Core.Renderer.Buffer;

public abstract class FrameBuffer extends Buffer {

    protected int width;
    protected int height;

    protected static boolean windowMinized;

    public FrameBuffer(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void resize(float width, float height);

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
