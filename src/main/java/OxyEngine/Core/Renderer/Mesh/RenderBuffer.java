package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Renderer.Mesh.Platform.OpenGLRenderBuffer;
import OxyEngine.System.Disposable;
import OxyEngine.TargetPlatform;

public abstract class RenderBuffer implements Disposable {

    protected final TextureFormat textureFormat;

    public int width;
    public int height;
    public final int samples;

    protected int bufferId;

    public RenderBuffer(TextureFormat textureFormat, int samples, int width, int height) {
        this.textureFormat = textureFormat;
        this.width = width;
        this.samples = samples;
        this.height = height;
    }

    public abstract void unbind();

    public abstract void bind();

    public abstract void loadStorageWithSamples(int samples, int width, int height);

    public abstract void loadStorage(int width, int height);

    public abstract void resize(int width, int height);

    public boolean isNull() {
        return bufferId == 0;
    }

    public int getBufferId() {
        return bufferId;
    }

    public static <T extends RenderBuffer> T create(TextureFormat format, int width, int height) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLRenderBuffer.class.getDeclaredConstructor(TextureFormat.class, int.class, int.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(format, width, height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public TextureFormat getFormat() {
        return textureFormat;
    }
}
