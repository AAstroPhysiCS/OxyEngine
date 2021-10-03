package OxyEngine.Core.Renderer;

import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Mesh.BufferUsage;
import OxyEngine.Core.Renderer.Mesh.Platform.OpenGLEmptyBuffer;
import OxyEngine.Core.Renderer.Mesh.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Texture.TextureSlot;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static OxyEngine.Core.Renderer.Renderer.depthPrePassRenderPass;
import static OxyEngine.Core.Scene.SceneRuntime.cameraContext;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

/*
 * For Forward+ Rendering
 */
final class RendererLightBufferSpecification {

    private final OpenGLEmptyBuffer lightBuffer;
    private final OpenGLEmptyBuffer visibleLightIndicesBuffer;

    static final int MAX_SUPPORTED_LIGHTS = 1024;

    static int numberOfTiles, workGroupsX, workGroupsY;

    RendererLightBufferSpecification() {
        workGroupsX = (int) ((ScenePanel.windowSize.x + (ScenePanel.windowSize.x % 16)) / 16);
        workGroupsY = (int) ((ScenePanel.windowSize.y + (ScenePanel.windowSize.y % 16)) / 16);
        numberOfTiles = workGroupsX * workGroupsY;
        lightBuffer = new OpenGLEmptyBuffer(GL_SHADER_STORAGE_BUFFER, MAX_SUPPORTED_LIGHTS * 12 * Float.BYTES, BufferUsage.DYNAMIC);
        visibleLightIndicesBuffer = new OpenGLEmptyBuffer(GL_SHADER_STORAGE_BUFFER, numberOfTiles * Integer.BYTES * MAX_SUPPORTED_LIGHTS, BufferUsage.STATIC);

        lightBuffer.load();
        visibleLightIndicesBuffer.load();
    }

    void update() {

        int length = Renderer.getPointLightCommand().size() * Float.BYTES * 12;
        if (length == 0) return;

        RenderCommand.bindBuffer(GL_SHADER_STORAGE_BUFFER, lightBuffer.getBufferId());
        FloatBuffer pointLightData = RenderCommand.mapBuffer(GL_SHADER_STORAGE_BUFFER, GL_WRITE_ONLY).asFloatBuffer();
        pointLightData.position(0);
        BufferUtils.zeroBuffer(pointLightData);

        float padding = 1.0f;
        for (PointLight light : Renderer.getPointLightCommand()) {
            Vector3f position = light.getPosition();
            Vector3f color = light.getColor();
            float colorIntensity = light.getColorIntensity();

            pointLightData.put(position.x).put(position.y).put(position.z).put(padding);
            pointLightData.put(color.x * colorIntensity).put(color.y * colorIntensity).put(color.z * colorIntensity).put(padding);
            pointLightData.put(light.getRadius()).put(light.getCutoff());
            pointLightData.put(padding).put(padding);
        }

        RenderCommand.unmapBuffer(GL_SHADER_STORAGE_BUFFER);
        RenderCommand.bindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    void dispatch() {
        //Perform light culling
        Shader lightCullingComputeShader = Renderer.getShader("OxyLightCulling");
        lightCullingComputeShader.begin();
        lightCullingComputeShader.setUniformMatrix4fv("projection", cameraContext.getProjectionMatrix());
        lightCullingComputeShader.setUniformMatrix4fv("view", cameraContext.getModelMatrix());
        lightCullingComputeShader.setUniform1i("depthPrePassMap", 0);
        lightCullingComputeShader.setUniformVec2i("screenSize", (int) ScenePanel.windowSize.x, (int) ScenePanel.windowSize.y);
        lightCullingComputeShader.setUniform1i("lightCount", MAX_SUPPORTED_LIGHTS);

        if (depthPrePassRenderPass.getFrameBuffer() instanceof OpenGLFrameBuffer frameBuffer) {
            int depthMapTextureID = frameBuffer.getColorAttachmentTexture(0)[0];
            RenderCommand.bindTextureUnit(TextureSlot.UNUSED.getValue(), depthMapTextureID);
            RenderCommand.dispatchCompute(workGroupsX, workGroupsY, 1);
            RenderCommand.memoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
        }
        lightCullingComputeShader.end();
    }

    public OpenGLEmptyBuffer getLightBuffer() {
        return lightBuffer;
    }

    public OpenGLEmptyBuffer getVisibleLightIndicesBuffer() {
        return visibleLightIndicesBuffer;
    }
}
