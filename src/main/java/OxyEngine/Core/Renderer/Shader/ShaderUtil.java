package OxyEngine.Core.Renderer.Shader;

import OxyEngine.System.OxySystem;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL20.*;

public final class ShaderUtil {

    private ShaderUtil() {
    }

    static String loadAsString(String path) {
        return OxySystem.FileSystem.load(path);
    }

    public static String getFragment(String loadedString){
        return (String) loadedString.subSequence(loadedString.indexOf("//#type fragment"), loadedString.indexOf("//#type vertex"));
    }

    public static String getVertex(String loadedString){
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

        if(glGetShaderi(vertexId, GL_COMPILE_STATUS) != GL_TRUE){
            logger.severe("Vertex Shader crashed! " + glGetShaderInfoLog(vertexId));
            throw new InternalError("Vertex Shader crashed! " + glGetShaderInfoLog(vertexId));
        }

        if(glGetShaderi(fragmentId, GL_COMPILE_STATUS) != GL_TRUE){
            logger.severe("Fragment Shader crashed! " + glGetShaderInfoLog(fragmentId));
            throw new InternalError("Fragment Shader crashed! " + glGetShaderInfoLog(fragmentId));
        }

        glAttachShader(program, vertexId);
        glAttachShader(program, fragmentId);
        glLinkProgram(program);
        glValidateProgram(program);

        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);

        return program;
    }
}
