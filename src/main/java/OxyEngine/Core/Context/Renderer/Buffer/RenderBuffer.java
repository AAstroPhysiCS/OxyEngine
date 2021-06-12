package OxyEngine.Core.Context.Renderer.Buffer;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLRenderBuffer;
import OxyEngine.TargetPlatform;

public abstract class RenderBuffer extends Buffer {

    protected final TextureFormat textureFormat;

    public int width;
    public int height;
    public final int samples;

    public RenderBuffer(TextureFormat textureFormat, int samples, int width, int height) {
        this.textureFormat = textureFormat;
        this.width = width;
        this.samples = samples;
        this.height = height;
        load();
    }

    public abstract void unbind();

    public abstract void bind();

    public abstract void loadStorageWithSamples(int samples, int width, int height);

    public abstract void loadStorage(int width, int height);

    public abstract void resize(int width, int height);

    public static <T extends RenderBuffer> T create(TextureFormat format, int width, int height) {
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
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
