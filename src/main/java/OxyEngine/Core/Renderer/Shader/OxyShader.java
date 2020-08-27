package OxyEngine.Core.Renderer.Shader;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Components.EntityComponent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class OxyShader implements OxyDisposable, EntityComponent {

    public static final int VERTICES = 0;
    public static final int TEXTURE_COORDS = 1;
    public static final int TEXTURE_SLOT = 2;
    public static final int COLOR = 3;
    public static final int NORMALS = 4;

    private final Map<String, ? super Number> uniformLocations = new HashMap<>();

    private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    private final int program;

    public OxyShader(String glslPath) {
        String loadedString = ShaderUtil.loadAsString(glslPath);
        program = ShaderUtil.create(ShaderUtil.getVertex(loadedString).trim(), ShaderUtil.getFragment(loadedString).trim());
    }

    public void enable() {
        glUseProgram(program);
    }

    public void disable() {
        glUseProgram(0);
    }

    public void setUniform1i(String name, int value) {
        if (!uniformLocations.containsKey(name)) {
            uniformLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1i((Integer) uniformLocations.get(name), value);
    }

    public void setUniform1iv(String name, int[] value) {
        if (!uniformLocations.containsKey(name)) {
            uniformLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1iv((Integer) uniformLocations.get(name), value);
    }

    public void setUniform1f(String name, float value) {
        if (!uniformLocations.containsKey(name)) {
            uniformLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniform1f((Integer) uniformLocations.get(name), value);
    }

    public void setUniformVec4(String vecName, Vector4f vec) {
        if (!uniformLocations.containsKey(vecName)) {
            uniformLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform4f((Integer) uniformLocations.get(vecName), vec.x, vec.y, vec.z, vec.w);
    }

    public void setUniformVec4(Vector4f vec, int location) {
        glUniform4f(location, vec.x, vec.y, vec.z, vec.w);
    }

    public void setUniformVec3(String vecName, Vector3f vec) {
        if (!uniformLocations.containsKey(vecName)) {
            uniformLocations.put(vecName, glGetUniformLocation(program, vecName));
        }
        glUniform3f((Integer) uniformLocations.get(vecName), vec.x, vec.y, vec.z);
    }

    public void setUniformVec3(Vector3f vec, int location) {
        glUniform3f(location, vec.x, vec.y, vec.z);
    }

    public void setUniformMatrix4fv(Matrix4f m, String name, boolean transpose){
        m.get(buffer);
        if (!uniformLocations.containsKey(name)) {
            uniformLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniformMatrix4fv((Integer) uniformLocations.get(name), transpose, buffer);
    }

    public void setUniformMatrix3fv(Matrix3f m, String name, boolean transpose){
        m.get(buffer);
        if (!uniformLocations.containsKey(name)) {
            uniformLocations.put(name, glGetUniformLocation(program, name));
        }
        glUniformMatrix3fv((Integer) uniformLocations.get(name), transpose, buffer);
    }

    public void setCamera(OxyCamera camera) {
        setUniformMatrix4fv(camera.getProjectionMatrix(), "pr_Matrix", camera.isTranspose());
        setUniformMatrix4fv(camera.getModelMatrix(), "m_Matrix", camera.isTranspose());
        setUniformMatrix4fv(camera.getViewMatrix(), "v_Matrix", camera.isTranspose());
        setUniformMatrix4fv(camera.getViewMatrixNoTranslation(), "v_Matrix_NoTransform", camera.isTranspose());
    }

    public Map<String, ? super Number> getUniformLocations() {
        return uniformLocations;
    }

    public int getProgram() {
        return program;
    }

    @Override
    public void dispose() {
        buffer.clear();
        glDeleteProgram(program);
    }
}
