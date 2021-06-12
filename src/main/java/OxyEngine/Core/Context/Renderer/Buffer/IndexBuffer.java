package OxyEngine.Core.Context.Renderer.Buffer;

import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLIndexBuffer;
import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Scene.Objects.Model.OxyNativeObject;
import OxyEngine.TargetPlatform;

public abstract class IndexBuffer extends Buffer {

    protected int length;
    protected int[] indices;

    protected final OxyPipeline.Layout layout;

    public IndexBuffer(OxyPipeline.Layout layout) {
        this.layout = layout;
    }

    protected abstract void copy(int[] m_indices);

    public int length() {
        return length;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public abstract void addToBuffer(OxyNativeObject oxyEntity);

    public abstract void addToBuffer(int[] m_indices);

    public boolean emptyData() {
        return indices == null;
    }

    public static <T extends IndexBuffer> T create(OxyPipeline pipeline) {
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            var layout = pipeline.getLayout(IndexBuffer.class);
            try {
                var constructor = OpenGLIndexBuffer.class.getDeclaredConstructor(OxyPipeline.Layout.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(layout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }
}
