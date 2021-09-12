package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLUniformBuffer;
import OxyEngine.System.Disposable;
import OxyEngine.TargetPlatform;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public abstract class UniformBuffer implements Disposable {

    protected int bufferId;

    public static <T extends UniformBuffer> T create(int size, int binding) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLUniformBuffer.class.getDeclaredConstructor(int.class, int.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(size, binding);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public abstract void load();

    public abstract void setData(int offset, int[] data);

    public abstract void setData(int offset, float[] data);

    public abstract void setData(int offset, Matrix4fc data);

    public abstract void setData(int offset, Vector3fc data);

    public abstract void setData(int offset, Vector4fc data);
}
