package OxyEngine.Core.Renderer.Pipeline;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;

public enum ShaderType {
    Float1(1, GL_FLOAT), Float2(2, GL_FLOAT), Float3(3, GL_FLOAT), Float4(4, GL_FLOAT),
    Int1(1, GL_INT), Int2(2, GL_INT), Int3(3, GL_INT), Int4(4, GL_INT),
    Matrix3f(3 * 3, GL_FLOAT), Matrix4f(4 * 4, GL_FLOAT);

    final int size, openGLType;

    ShaderType(int size, int openGLType){
        this.size = size;
        this.openGLType = openGLType;
    }

    public int getSize() {
        return size;
    }

    public int getOpenGLType() {
        return openGLType;
    }
}
