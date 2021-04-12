package OxyEngine.Core.Renderer.Pipeline;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Passes.OxyRenderPass;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public final class OxyPipeline {

    protected OxyShader shader;
    protected final String debugName;
    //protected OxyRenderPass renderPass;

    protected final Map<String, ? super Number> parameterLocations = new HashMap<>();

    private OxyPipeline(PipelineSpecification builder){
        this.shader = builder.shader;
        this.debugName = builder.debugName;
    }

    public static PipelineSpecification createNewSpecification(){
        return new PipelineSpecification();
    }

    interface Builder {

        Builder setShader(OxyShader shader);

        Builder setRenderPass(OxyRenderPass renderPass);

        Builder setDebugName(String name);
    }

    public static OxyPipeline createNewPipeline(PipelineSpecification builder){
        return new OxyPipeline(builder);
    }

    public void begin(){
        glUseProgram(shader.getProgram());
    }

    public void setShader(OxyShader shader){
        this.shader = shader;
    }

    public void end(){
        glUseProgram(0);
    }

    private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    public void setCameraUniforms(OxyCamera camera) {
        setUniformMatrix4fv("pr_Matrix", camera.getProjectionMatrix(), camera.isTranspose());
        setUniformMatrix4fv("m_Matrix", camera.getModelMatrix(), camera.isTranspose());
        setUniformMatrix4fv("v_Matrix", camera.getViewMatrix(), camera.isTranspose());
        setUniformMatrix4fv("v_Matrix_NoTransform", camera.getViewMatrixNoTranslation(), camera.isTranspose());
        setUniformVec3("cameraPos", camera.origin);
    }

    public void setUniform1i(String name, int value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(shader.getProgram(), name));
        }
        glUniform1i((Integer) parameterLocations.get(name), value);
    }

    public void setUniform1iv(String name, int[] value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(shader.getProgram(), name));
        }
        glUniform1iv((Integer) parameterLocations.get(name), value);
    }

    public void setUniform1f(String name, float value) {
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(shader.getProgram(), name));
        }
        glUniform1f((Integer) parameterLocations.get(name), value);
    }

    public void setUniformVec4(String vecName, float x, float y, float z, float w) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(shader.getProgram(), vecName));
        }
        glUniform4f((Integer) parameterLocations.get(vecName), x, y, z, w);
    }

    public void setUniformVec4(Vector4f vec, int location) {
        glUniform4f(location, vec.x, vec.y, vec.z, vec.w);
    }

    public void setUniformVec3(String vecName, float x, float y, float z) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(shader.getProgram(), vecName));
        }
        glUniform3f((Integer) parameterLocations.get(vecName), x, y, z);
    }

    public void setUniformVec3(String vecName, Vector3f vec) {
        if (!parameterLocations.containsKey(vecName)) {
            parameterLocations.put(vecName, glGetUniformLocation(shader.getProgram(), vecName));
        }
        glUniform3f((Integer) parameterLocations.get(vecName), vec.x, vec.y, vec.z);
    }

    public void setUniformVec3(Vector3f vec, int location) {
        glUniform3f(location, vec.x, vec.y, vec.z);
    }

    public void setUniformMatrix4fv(String name, Matrix4f m, boolean transpose) {
        m.get(buffer);
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(shader.getProgram(), name));
        }
        glUniformMatrix4fv((Integer) parameterLocations.get(name), transpose, buffer);
    }

    public void setUniformMatrix3fv(String name, Matrix3f m, boolean transpose) {
        m.get(buffer);
        if (!parameterLocations.containsKey(name)) {
            parameterLocations.put(name, glGetUniformLocation(shader.getProgram(), name));
        }
        glUniformMatrix3fv((Integer) parameterLocations.get(name), transpose, buffer);
    }
}
