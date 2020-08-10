package OxyEngine.Core.Renderer.Shader;

import OxyEngine.System.OxySystem;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL20.*;

public final class ShaderUtil {

    private ShaderUtil() {
    }

    static String loadAsString(String path) {
        return OxySystem.FileSystem.load(path);
    }

    public static String getFragment(String loadedString) {
        return (String) loadedString.subSequence(loadedString.indexOf("//#type fragment"), loadedString.indexOf("//#type vertex"));
    }

    public static String getVertex(String loadedString) {
        return (String) loadedString.subSequence(loadedString.indexOf("//#type vertex"), loadedString.length());
    }

    public static int create(String vertexString, String fragmentString) {
        int program = glCreateProgram();
        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(vertexId, vertexString);
        glShaderSource(fragmentId, fragmentString);

        glCompileShader(vertexId);
        glCompileShader(fragmentId);

        assert glGetShaderi(vertexId, GL_COMPILE_STATUS) == GL_TRUE : oxyAssert("Vertex Shader crashed!" + glGetShaderInfoLog(vertexId));
        assert glGetShaderi(fragmentId, GL_COMPILE_STATUS) == GL_TRUE : oxyAssert("Fragment Shader crashed! " + glGetShaderInfoLog(fragmentId));

        glAttachShader(program, vertexId);
        glAttachShader(program, fragmentId);
        glLinkProgram(program);
        glValidateProgram(program);

        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);

        return program;
    }
}
