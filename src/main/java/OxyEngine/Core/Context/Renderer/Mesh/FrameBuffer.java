package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.Core.Context.Renderer.Mesh.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Texture.Color;
import OxyEngine.System.Disposable;
import OxyEngine.TargetPlatform;

public abstract class FrameBuffer implements Disposable {

    protected int bufferId;

    protected int width;
    protected int height;

    protected boolean flushed = false;

    protected boolean needResize;

    protected static boolean windowMinized;

    protected final Color clearColor;

    public FrameBuffer(final int width, final int height, Color clearColor) {
        this.width = width;
        this.height = height;
        this.clearColor = clearColor;
    }

    public boolean needResize() {
        return needResize;
    }

    public void setNeedResize(boolean needResize, int newWidth, int newHeight) {
        this.needResize = needResize;
        this.width = newWidth;
        this.height = newHeight;
    }

    public abstract void bind();

    public abstract void unbind();

    public abstract void resize(int width, int height);

    public abstract void bindDepthAttachment(int specIndex, int index);

    public abstract void bindColorAttachment(int specIndex, int index);

    public abstract void flush();

    public void resetFlush(){
        flushed = false;
    }

    public abstract void attachColorAttachment(int textarget, int indexOfColorAttachment, int textureId);

    public abstract void attachColorAttachment(int textarget, int indexOfColorAttachment, int textureId, int mip);

    protected abstract void attachRenderBufferToFrameBuffer(int attachmentId, RenderBuffer renderBuffer);

    public abstract void checkStatus();

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getClearColor() {
        return clearColor;
    }

    public boolean isFlushed() {
        return flushed;
    }

    public static <T extends FrameBuffer> T create(int width, int height, Color clearColor, FrameBufferSpecification... specBuilders) {
        if (specBuilders.length == 0) throw new IllegalStateException("Specification not given!");
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLFrameBuffer.class.getDeclaredConstructor(int.class, int.class, Color.class, specBuilders.getClass());
                constructor.setAccessible(true);
                return (T) constructor.newInstance(width, height, clearColor, specBuilders);
            } catch (Exception e) {
                e.getCause().printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public static <T extends FrameBuffer> T create(int width, int height, FrameBufferSpecification... specBuilders) {
        if (specBuilders.length == 0) throw new IllegalStateException("Specification not given!");
        return create(width, height, Color.DEFAULT, specBuilders);
    }

    public static <T> T createNewSpec(Class<T> tClass) {
        try {
            return tClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Spec Builder should not be empty!");
    }
}