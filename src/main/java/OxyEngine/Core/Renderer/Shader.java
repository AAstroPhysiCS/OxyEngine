package OxyEngine.Core.Renderer;

import OxyEngine.System.Disposable;
import OxyEngine.System.FileSystem;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static OxyEngine.System.FileSystem.loadAsByteBuffer;
import static OxyEngine.System.FileSystem.writeAsByteBuffer;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL41.glShaderBinary;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;
import static org.lwjgl.opengl.GL46.GL_SHADER_BINARY_FORMAT_SPIR_V;
import static org.lwjgl.opengl.GL46.glSpecializeShader;
import static org.lwjgl.util.shaderc.Shaderc.*;

public final class Shader implements Disposable {

    public static final int VERTICES = 0;
    public static final int TEXTURE_COORDS = 1;
    public static final int NORMALS = 2;
    public static final int BITANGENT = 3;
    public static final int TANGENT = 4;
    public static final int OBJECT_ID = 5;
    public static final int BONEIDS = 6;
    public static final int WEIGHTS = 7;

    private int program;
    private final String name, glslPath;

    static final String cacheDirectory = "src/main/resources/shaders/cached";

    private final Map<String, Integer> parameterLocations = new HashMap<>();

    private static final FloatBuffer matrixBuffer = createFloatBuffer(16);

    public static Shader createCompute(String name, String glslPath) {
        ShaderLibrary.removeShaderIfExist(name);
        String loadedCode = FileSystem.load(glslPath);
        Shader s = new Shader(name, glslPath, loadedCode);
        ShaderLibrary.addShaders(s);
        return s;
    }

    public static Shader createShader(String name, String glslPath) {
        ShaderLibrary.removeShaderIfExist(name);
        String loadedCode = FileSystem.load(glslPath);
        Shader s = new Shader(name, glslPath, getVertex(loadedCode), getFragment(loadedCode));
        ShaderLibrary.addShaders(s);
        return s;
    }

    private Shader(String name, String glslPath, String vertex, String fragment) {
        this.name = name;
        this.glslPath = glslPath;
        program = createProgram(vertex.trim(), fragment.trim());
    }

    private Shader(String name, String glslPath, String computeShaderSource) {
        this.name = name;
        this.glslPath = glslPath;
        program = createProgram(computeShaderSource);
    }

    //SpirV not being used for now.
    private void enableSPIRV(String loadedCode) {
        createCacheDirectoryIfNeeded();
        long compiler = shaderc_compiler_initialize();
        long options = shaderc_compile_options_initialize();
        compileOrGetOpenGLBinaries(loadedCode, compiler, options);
        shaderc_compile_options_release(options);
        shaderc_compiler_release(compiler);
    }

    private void compileOrGetOpenGLBinaries(String loadedCode, long compiler, long options) {
        shaderc_compile_options_set_optimization_level(options, shaderc_optimization_level_performance);
        shaderc_compile_options_set_target_env(options,
                shaderc_target_env_opengl,
                shaderc_env_version_opengl_4_5);

        String vertexPartCode = getVertex(loadedCode).trim();
        String fragmentPartCode = getFragment(loadedCode).trim();

        File spirVVertexPartCachedFile = new File(cacheDirectory + "\\" + this.name.toLowerCase() + ".vert");
        File spirVFragmentPartCachedFile = new File(cacheDirectory + "\\" + this.name.toLowerCase() + ".frag");

        boolean debug = true;

        ByteBuffer[] data = new ByteBuffer[2];
        if (spirVVertexPartCachedFile.exists() && !debug) {
            data[0] = loadAsByteBuffer(spirVVertexPartCachedFile);
        } else {
            data[0] = compile(vertexPartCode, compiler, options, shaderc_glsl_vertex_shader);
        }

        if (spirVFragmentPartCachedFile.exists() && !debug) {
            data[1] = loadAsByteBuffer(spirVFragmentPartCachedFile);
            program = createProgram(data);
        } else {
            data[1] = compile(fragmentPartCode, compiler, options, shaderc_glsl_fragment_shader);
            program = createProgram(data);

            writeAsByteBuffer(spirVVertexPartCachedFile, data[0]);
            writeAsByteBuffer(spirVFragmentPartCachedFile, data[1]);
        }
    }

    private ByteBuffer compile(String stagePartCode, long compiler, long options, int stageType) {
        long result = shaderc_compile_into_spv(compiler, stagePartCode, stageType, this.name + ".glsl", "main", options);
        assert shaderc_result_get_compilation_status(result) == shaderc_compilation_status_success : oxyAssert(stageType == 0 ? "Vertex Shader" : "Fragment Shader" +
                " compilation failed!\n" + shaderc_result_get_error_message(result));

        int size = (int) shaderc_result_get_length(result);
        ByteBuffer moduleBytes = createByteBuffer(size);
        moduleBytes.put(shaderc_result_get_bytes(result)).flip();
        return moduleBytes;
    }

    private static String getFragment(String loadedString) {
        return (String) loadedString.subSequence(loadedString.indexOf("//#type fragment"), loadedString.length());
    }

    private static String getVertex(String loadedString) {
        return (String) loadedString.subSequence(loadedString.indexOf("//#type vertex"), loadedString.indexOf("//#type fragment"));
    }

    private static void createCacheDirectoryIfNeeded() {
        File f = new File(cacheDirectory);
        assert f.exists() || f.mkdir() : oxyAssert("Failed to create a cache directory!");
    }

    private static int createProgram(ByteBuffer[] data) {
        int program = glCreateProgram();
        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderBinary(new int[]{vertexId}, GL_SHADER_BINARY_FORMAT_SPIR_V, data[0]);
        glSpecializeShader(vertexId, "main", new int[0], new int[0]);
        glAttachShader(program, vertexId);

        glShaderBinary(new int[]{fragmentId}, GL_SHADER_BINARY_FORMAT_SPIR_V, data[1]);
        glSpecializeShader(fragmentId, "main", new int[0], new int[0]);
        glAttachShader(program, fragmentId);

        glLinkProgram(program);

        int[] isLinked = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, isLinked);
        if (isLinked[0] == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            oxyAssert("Shader linking crashed: \n" + log);

            glDeleteProgram(program);
        }

        glDetachShader(program, vertexId);
        glDetachShader(program, fragmentId);
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);

        return program;
    }

    private static int createProgram(String vertexString, String fragmentString) {
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

    private static int createProgram(String computeShaderSource) {
        int program = glCreateProgram();
        int computeId = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(computeId, computeShaderSource);
        glCompileShader(computeId);

        assert glGetShaderi(computeId, GL_COMPILE_STATUS) == GL_TRUE : oxyAssert("Compute Shader crashed!" + glGetShaderInfoLog(computeId));

        glAttachShader(program, computeId);
        glLinkProgram(program);
        glValidateProgram(program);

        glDeleteShader(computeId);

        return program;
    }

    public int getProgram() {
        return program;
    }

    public String getName() {
        return name;
    }

    public void begin() {
        glUseProgram(program);
    }

    public void end() {
        glUseProgram(0);
    }

    @Override
    public void dispose() {
        ShaderLibrary.removeShaderIfExist(name);
        glDeleteProgram(program);
    }

    public void setUniform1i(String name, int value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1i(parameterLocations.get(name), value);
    }

    public void setUniform1iv(String name, int[] value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1iv(parameterLocations.get(name), value);
    }

    public void setUniform1f(String name, float value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1f(parameterLocations.get(name), value);
    }

    public void setUniformVec4(String vecName, float x, float y, float z, float w) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform4f(parameterLocations.get(vecName), x, y, z, w);
    }

    public void setUniformVec4(Vector4f vec, int location) {
        glUniform4f(location, vec.x, vec.y, vec.z, vec.w);
    }

    public void setUniformVec2i(String vecName, int x, int y) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform2i(parameterLocations.get(vecName), x, y);
    }

    public void setUniformVec3(String vecName, float x, float y, float z) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform3f(parameterLocations.get(vecName), x, y, z);
    }

    public void setUniformVec3(String vecName, Vector3f vec) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform3f(parameterLocations.get(vecName), vec.x, vec.y, vec.z);
    }

    public void setUniformVec3(Vector3f vec, int location) {
        glUniform3f(location, vec.x, vec.y, vec.z);
    }

    public void setUniformMatrix4fv(String name, Matrix4f m) {
        m.get(matrixBuffer);
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniformMatrix4fv(parameterLocations.get(name), false, matrixBuffer);
    }

    public void setUniformMatrix3fv(String name, Matrix3f m) {
        m.get(matrixBuffer);
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniformMatrix3fv(parameterLocations.get(name), false, matrixBuffer);
    }

    //clears the buffer and creates a new shader object
    public void recompile() {
        this.dispose();
        Shader newShader = Shader.createShader(this.name, this.glslPath);
        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;

        newShader.begin();
        newShader.setUniform1iv("tex", samplers);
        newShader.end();
    }
}