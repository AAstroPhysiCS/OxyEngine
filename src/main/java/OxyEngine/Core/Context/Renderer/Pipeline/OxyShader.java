package OxyEngine.Core.Context.Renderer.Pipeline;

import OxyEngine.System.OxyDisposable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public final class OxyShader implements OxyDisposable {

    public static final int VERTICES = 0;
    public static final int TEXTURE_COORDS = 1;
    public static final int NORMALS = 2;
    public static final int BITANGENT = 3;
    public static final int TANGENT = 4;
    public static final int OBJECT_ID = 5;
    public static final int BONEIDS = 6;
    public static final int WEIGHTS = 7;

    private final int program;
    private final String name, glslPath;

    protected final Map<String, Number> parameterLocations = new HashMap<>();
    private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    public static OxyShader createShader(String name, String glslPath){
        ShaderLibrary.removeShaderIfExist(name);
        OxyShader s = new OxyShader(name, glslPath);
        ShaderLibrary.addShaders(s);
        return s;
    }

    private OxyShader(String name, String glslPath) {
        this.name = name;
        this.glslPath = glslPath;
        String loadedString = ShaderUtil.loadAsString(glslPath);
        program = ShaderUtil.create(ShaderUtil.getVertex(loadedString).trim(), ShaderUtil.getFragment(loadedString).trim());
    }

    public int getProgram() {
        return program;
    }

    public String getName() {
        return name;
    }

    public void begin(){
        glUseProgram(program);
    }

    public void end(){
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
        glUniform1i((Integer) parameterLocations.get(name), value);
    }

    public void setUniform1iv(String name, int[] value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1iv((Integer) parameterLocations.get(name), value);
    }

    public void setUniform1f(String name, float value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1f((Integer) parameterLocations.get(name), value);
    }

    public void setUniformVec4(String vecName, float x, float y, float z, float w) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform4f((Integer) parameterLocations.get(vecName), x, y, z, w);
    }

    public void setUniformVec4(Vector4f vec, int location) {
        glUniform4f(location, vec.x, vec.y, vec.z, vec.w);
    }

    public void setUniformVec3(String vecName, float x, float y, float z) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform3f((Integer) parameterLocations.get(vecName), x, y, z);
    }

    public void setUniformVec3(String vecName, Vector3f vec) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform3f((Integer) parameterLocations.get(vecName), vec.x, vec.y, vec.z);
    }

    public void setUniformVec3(Vector3f vec, int location) {
        glUniform3f(location, vec.x, vec.y, vec.z);
    }

    public void setUniformMatrix4fv(String name, Matrix4f m, boolean transpose) {
        m.get(buffer);
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniformMatrix4fv((Integer) parameterLocations.get(name), transpose, buffer);
    }

    public void setUniformMatrix3fv(String name, Matrix3f m, boolean transpose) {
        m.get(buffer);
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniformMatrix3fv((Integer) parameterLocations.get(name), transpose, buffer);
    }

    public void recompile() {
        //method: clears the buffer and creates a new shader object
        this.dispose();
        OxyShader newShader = OxyShader.createShader(this.name, this.glslPath);
        int[] samplers = new int[32];
        for (int i = 0; i < samplers.length; i++) samplers[i] = i;

        newShader.begin();
        newShader.setUniform1iv("tex", samplers);
        newShader.end();
    }
}