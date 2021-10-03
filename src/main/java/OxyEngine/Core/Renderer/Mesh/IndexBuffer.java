package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Mesh.Platform.OpenGLIndexBuffer;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.TargetPlatform;

public abstract class IndexBuffer extends Buffer<int[]> {

    protected IndexBuffer(int[] data) {
        super(data);
    }

    protected IndexBuffer(int allocationSize) {
        super(new int[allocationSize]);
    }

    public abstract void addToBuffer(int[] data);

    public static <T extends IndexBuffer> T create(int[] data) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLIndexBuffer.class.getDeclaredConstructor(int[].class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public static <T extends IndexBuffer> T create(int allocationSize) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLIndexBuffer.class.getDeclaredConstructor(int.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(allocationSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public static <T extends IndexBuffer> T create(T other) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            try {
                var constructor = OpenGLIndexBuffer.class.getDeclaredConstructor(OpenGLIndexBuffer.class);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(other);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("API not supported yet!");
    }
}
