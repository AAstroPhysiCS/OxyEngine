package OxyEngine.Core.Context.Renderer.Buffer;

import OxyEngine.Core.Context.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import OxyEngine.TargetPlatform;

public abstract class FrameBuffer extends Buffer {

    protected int width;
    protected int height;

    protected boolean flushed = false;

    protected boolean needResize;

    protected static boolean windowMinized;

    protected final OxyColor clearColor;

    public FrameBuffer(final int width, final int height, OxyColor clearColor) {
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

    public OxyColor getClearColor() {
        return clearColor;
    }

    public boolean isFlushed() {
        return flushed;
    }

    public static <T extends FrameBuffer> T create(int width, int height, OxyColor clearColor, FrameBufferSpecification... specBuilders) {
        if (specBuilders.length == 0) throw new IllegalStateException("Specification not given!");
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLFrameBuffer.class.getDeclaredConstructor(int.class, int.class, OxyColor.class, specBuilders.getClass());
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
        return create(width, height, OxyColor.DEFAULT, specBuilders);
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