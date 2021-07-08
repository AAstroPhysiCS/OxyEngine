package OxyEngine.Core.Context.Renderer.Buffer;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLUniformBuffer;
import OxyEngine.TargetPlatform;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public abstract class UniformBuffer extends Buffer {

    public static <T extends UniformBuffer> T create(int size, int binding) {
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
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

    public abstract void setData(int offset, int[] data);

    public abstract void setData(int offset, float[] data);

    public abstract void setData(int offset, Matrix4fc data);

    public abstract void setData(int offset, Vector3fc data);

    public abstract void setData(int offset, Vector4fc data);
}
