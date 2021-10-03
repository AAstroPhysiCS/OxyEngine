package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Mesh.Platform.OpenGLVertexBuffer;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.oxyAssert;

public abstract class VertexBuffer extends Buffer<float[]> {

    protected final BufferUsage usage;

    protected VertexBuffer(float[] data, BufferUsage usage) {
        super(data);
        this.usage = usage;
        assert usage != null : oxyAssert("Some Implementation arguments are null");
    }

    protected VertexBuffer(int allocationSize, BufferUsage usage) {
        super(new float[allocationSize]);
        this.usage = usage;
    }

    public static <T extends VertexBuffer> T create(float[] data, BufferUsage usage) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLVertexBuffer.class.getDeclaredConstructor(float[].class, BufferUsage.class);
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

    public static <T extends VertexBuffer> T create(int allocationSize, BufferUsage usage){
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLVertexBuffer.class.getDeclaredConstructor(int.class, BufferUsage.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(allocationSize, usage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public BufferUsage getUsage() {
        return usage;
    }

    public abstract void addToBuffer(float[] m_Vertices);

    public abstract void updateData(int pos, float[] newData);
}
