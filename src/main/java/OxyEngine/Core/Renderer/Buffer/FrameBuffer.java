package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Context.OpenGLRendererAPI;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;

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

    public abstract void bindDepthAttachment(int specIndex, int index);

    public abstract void bindColorAttachment(int specIndex, int index);

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static <T extends FrameBuffer> T create(int width, int height, FrameBufferSpecification... specBuilders){
        if(rendererAPI instanceof OpenGLRendererAPI) {
            try {
                var constructor = OpenGLFrameBuffer.class.getDeclaredConstructor(int.class, int.class, specBuilders.getClass());
                constructor.setAccessible(true);
                return (T) constructor.newInstance(width, height, specBuilders);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }
}