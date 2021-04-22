package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.TextureSlot;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.stb.STBImage.*;

public class HDRTexture extends OxyTexture.AbstractTexture {

    private static final Matrix4f[] captureViews = new Matrix4f[]{
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
    };

    private static final Matrix4f captureProjection = new Matrix4f().setPerspective((float) Math.toRadians(90), 1.0f, 0.4768f, 10.0f);
    private int captureFBO;
    private int captureRBO;
    private int hdrTexture;

    private IrradianceTexture irradianceTexture;
    private PrefilterTexture prefilterTexture;
    private BDRF bdrf;

    private final OxyPipeline hdrPipeline;

    HDRTexture(TextureSlot slot, String path) {
        super(slot, path);
        assert slot.getValue() != 0 : oxyAssert("Slot can not be 0");
        this.hdrPipeline = SceneRenderer.getInstance().getHDRPipeline();
        load();
    }

    private void load() {

        OxyShader hdrShader = hdrPipeline.getShader();

        captureFBO = glGenFramebuffers();
        captureRBO = glGenRenderbuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);

        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 1920, 1920);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, captureRBO);
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");

        int[] width = new int[1];
        int[] height = new int[1];
        int[] nrComponents = new int[1];
        stbi_set_flip_vertically_on_load(true);
        FloatBuffer data = stbi_loadf(path, width, height, nrComponents, 0);
        assert data != null : oxyAssert("HDR Texture failed!");
        hdrTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hdrTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width[0], height[0], 0, GL_RGB, GL_FLOAT, data);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        stbi_image_free(data);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
        for (int i = 0; i < 6; ++i) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB32F,
                    1920, 1920, 0, GL_RGB, GL_FLOAT, (FloatBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        hdrShader.begin();
        hdrShader.setUniform1i("u_hdrTexture", 0);
        hdrShader.setUniformMatrix4fv("u_projectionHDR", captureProjection, true);
        hdrShader.end();

        if (SkyLight.mesh.empty())
            SkyLight.mesh.load(hdrPipeline);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, hdrTexture);
        glViewport(0, 0, 1920, 1920);
        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        for (int i = 0; i < 6; ++i) {
            hdrShader.begin();
            hdrShader.setUniformMatrix4fv("u_viewHDR", captureViews[i], true);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, 0);
            rendererAPI.clearBuffer();
            SkyLight.mesh.render();
            hdrShader.end();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        irradianceTexture = new IrradianceTexture(TextureSlot.IRRADIANCE, path, this);
        prefilterTexture = new PrefilterTexture(TextureSlot.PREFILTER, path, this);
        bdrf = new BDRF(TextureSlot.BDRF, path, this);

        OxyTexture.unbindAllTextures();
    }

    @Override
    public void dispose() {
        bdrf.dispose();
        prefilterTexture.dispose();
        irradianceTexture.dispose();
        glDeleteFramebuffers(captureFBO);
        glDeleteRenderbuffers(captureRBO);
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        quadVAO = 0;
        quadVBO = 0;
        glDeleteTextures(hdrTexture);
        super.dispose();
    }

    public int getIBLSlot() {
        return irradianceTexture.getTextureSlot();
    }

    public int getPrefilterSlot() {
        return prefilterTexture.getTextureSlot();
    }

    public int getBDRFSlot() {
        return bdrf.getTextureSlot();
    }

    public void bindAll() {
        if (bdrf != null) glBindTextureUnit(bdrf.getTextureSlot(), bdrf.textureId);
        if (prefilterTexture != null) glBindTextureUnit(prefilterTexture.getTextureSlot(), prefilterTexture.textureId);
        if (irradianceTexture != null)
            glBindTextureUnit(irradianceTexture.getTextureSlot(), irradianceTexture.textureId);
        if (this.textureId != 0) glBindTextureUnit(this.getTextureSlot(), this.textureId);
    }

    static class IrradianceTexture extends OxyTexture.AbstractTexture {

        final static OxyShader shader = OxyShader.createShader("OxyIBL", "shaders/OxyIBL.glsl");

        IrradianceTexture(TextureSlot slot, String path, HDRTexture mainTexture) {
            super(slot, path);

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
            for (int i = 0; i < 6; ++i) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB32F, 32, 32, 0,
                        GL_RGB, GL_FLOAT, (FloatBuffer) null);
            }
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 32, 32);

            shader.begin();
            shader.setUniform1i("u_skyBoxTextureIBL", 0);
            shader.setUniformMatrix4fv("u_projectionIBL", captureProjection, true);
            shader.end();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, mainTexture.textureId);
            glViewport(0, 0, 32, 32);
            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            for (int i = 0; i < 6; ++i) {
                shader.begin();
                shader.setUniformMatrix4fv("u_viewIBL", captureViews[i], true);
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                        GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, 0);
                rendererAPI.clearBuffer();
                SkyLight.mesh.render();
                shader.end();
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    static class PrefilterTexture extends OxyTexture.AbstractTexture {

        final static OxyShader shader = OxyShader.createShader("OxyPrefiltering", "shaders/OxyPrefiltering.glsl");

        PrefilterTexture(TextureSlot slot, String path, HDRTexture mainTexture) {
            super(slot, path);

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);
            for (int i = 0; i < 6; ++i) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB32F, 512, 512, 0,
                        GL_RGB, GL_FLOAT, (FloatBuffer) null);
            }
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

            shader.begin();
            shader.setUniform1i("u_skyBoxTexturePrefilter", 0);
            shader.setUniformMatrix4fv("u_projectionPrefilter", captureProjection, true);
            shader.setUniform1f("u_faceSize", 1920);
            shader.end();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, mainTexture.textureId);
            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            int maxMipLevels = 10;
            for (int mip = 0; mip < maxMipLevels; ++mip) {
                int mipWidth = (int) (512 * Math.pow(0.5f, mip));
                int mipHeight = (int) (512 * Math.pow(0.5f, mip));
                glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
                glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, mipWidth, mipHeight);
                glViewport(0, 0, mipWidth, mipHeight);
                float roughness = (float) mip / (float) (maxMipLevels - 1);
                shader.begin();
                shader.setUniform1f("u_roughness", roughness);
                shader.end();
                for (int i = 0; i < 6; ++i) {
                    shader.begin();
                    shader.setUniformMatrix4fv("u_viewPrefilter", captureViews[i], true);
                    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                            GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, textureId, mip);
                    rendererAPI.clearBuffer();
                    SkyLight.mesh.render();
                    shader.end();
                }
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    static class BDRF extends OxyTexture.AbstractTexture {

        final static OxyShader shader = OxyShader.createShader("OxyBDRF", "shaders/OxyBDRF.glsl");

        BDRF(TextureSlot slot, String path, HDRTexture mainTexture) {
            super(slot, path);

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, 512, 512, 0, GL_RG, GL_FLOAT, 0);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindFramebuffer(GL_FRAMEBUFFER, mainTexture.captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, mainTexture.captureRBO);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 512, 512);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
            glViewport(0, 0, 512, 512);
            rendererAPI.clearBuffer();
            shader.begin();
            renderQuad();
            shader.end();
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    private static int quadVAO, quadVBO;

    //TODO: Rendering 2D
    private static void renderQuad() {
        if (quadVAO == 0) {
            float[] quadVertices = {
                    -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            };

            quadVAO = glGenVertexArrays();
            quadVBO = glGenBuffers();
            glBindVertexArray(quadVAO);
            glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
            glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        }
        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
    }
}