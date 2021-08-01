package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLVertexBuffer;
import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Scene.OxyNativeObject;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.oxyAssert;

public abstract class VertexBuffer extends Buffer {

    protected float[] vertices = new float[0];
    protected final OxyPipeline.Layout layout;
    protected final MeshUsage usage;

    public int offsetToUpdate = -1;
    protected float[] dataToUpdate;

    public float[] getDataToUpdate() {
        return dataToUpdate;
    }

    protected VertexBuffer(OxyPipeline.Layout layout, MeshUsage usage) {
        this.layout = layout;
        this.usage = usage;
        assert usage != null : oxyAssert("Some Implementation arguments are null");
    }

    public static <T extends VertexBuffer> T create(OxyPipeline pipeline, MeshUsage usage) {
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            var layout = pipeline.getLayout(VertexBuffer.class);
            try {
                var constructor = OpenGLVertexBuffer.class.getDeclaredConstructor(OxyPipeline.Layout.class, MeshUsage.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(layout, usage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public abstract void updateSingleEntityData(int pos, float[] newVertices);

    public MeshUsage getUsage() {
        return usage;
    }

    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public abstract void addToBuffer(OxyNativeObject oxyEntity);

    public abstract void addToBuffer(float[] m_Vertices);
}
