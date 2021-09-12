package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLVertexBuffer;
import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.oxyAssert;

public abstract class VertexBuffer extends Buffer<float[]> {

    protected final MeshUsage usage;

    protected VertexBuffer(float[] data, MeshUsage usage) {
        super(data);
        this.usage = usage;
        assert usage != null : oxyAssert("Some Implementation arguments are null");
    }

    protected VertexBuffer(int allocationSize, MeshUsage usage) {
        super(new float[allocationSize]);
        this.usage = usage;
    }

    public static <T extends VertexBuffer> T create(float[] data, MeshUsage usage) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLVertexBuffer.class.getDeclaredConstructor(float[].class, MeshUsage.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(data, usage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public static <T extends VertexBuffer> T create(T other){
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLVertexBuffer.class.getDeclaredConstructor(OpenGLVertexBuffer.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(other);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public static <T extends VertexBuffer> T create(int allocationSize, MeshUsage usage){
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLVertexBuffer.class.getDeclaredConstructor(int.class, MeshUsage.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(allocationSize, usage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public MeshUsage getUsage() {
        return usage;
    }

    public abstract void addToBuffer(float[] m_Vertices);

    public abstract void updateData(int pos, float[] newData);
}
