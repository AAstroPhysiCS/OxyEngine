package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Light.SkyLight;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Scene.SceneRenderer;

import java.nio.FloatBuffer;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public class OpenGLHDRTexture extends HDRTexture {

    private int captureFBO;
    private int captureRBO;

    private IrradianceTexture irradianceTexture;
    private PrefilterTexture prefilterTexture;
    private BDRF bdrf;

    private final OxyPipeline hdrPipeline;

    protected OpenGLHDRTexture(TextureSlot hdr, TextureSlot prefilter, TextureSlot radiance, TextureSlot bdrf, String path) {
        super(hdr, prefilter, radiance, bdrf, path);
        assert hdr.getValue() != 0 || prefilter.getValue() != 0 || radiance.getValue() != 0 || bdrf.getValue() != 0 : oxyAssert("Slots can not be 0");
        this.hdrPipeline = SceneRenderer.getInstance().getHDRPipeline();
        initHDR();
    }

    private void initHDR() {

        OxyShader hdrShader = hdrPipeline.getShader();

        captureFBO = glGenFramebuffers();
        captureRBO = glGenRenderbuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);

        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 1920, 1920);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, captureRBO);
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : oxyAssert("Framebuffer is incomplete!");

        loadAsFloatBuffer();

        hdrTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, hdrTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL_RGB, GL_FLOAT, (FloatBuffer) textureBuffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        stbi_image_free((FloatBuffer) textureBuffer);

        hdrTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, hdrTextureId);
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
        glBindTexture(GL_TEXTURE_2D, hdrTextureId);
        glViewport(0, 0, 1920, 1920);
        glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
        for (int i = 0; i < 6; ++i) {
            hdrShader.begin();
            hdrShader.setUniformMatrix4fv("u_viewHDR", captureViews[i], true);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, hdrTextureId, 0);
            OxyRenderer.clearBuffer();
            SkyLight.mesh.render();
            hdrShader.end();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, hdrTextureId);
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        irradianceTexture = new IrradianceTexture(path, hdrTextureId, captureFBO, captureRBO);
        prefilterTexture = new PrefilterTexture(path, hdrTextureId, captureFBO, captureRBO);
        bdrf = new BDRF(path, captureFBO, captureRBO);

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
        glDeleteTextures(hdrTextureId);
        super.dispose();
    }

    @Override
    public void bindAll() {
        if (bdrf != null) glBindTextureUnit(bdrfSlot.getValue(), bdrf.bdrfTextureId);
        if (prefilterTexture != null) glBindTextureUnit(prefilterSlot.getValue(), prefilterTexture.prefilterTextureId);
        if (irradianceTexture != null)
            glBindTextureUnit(radianceSlot.getValue(), irradianceTexture.irradianceTextureId);
        if (this.hdrTextureId != 0) glBindTextureUnit(hdrSlot.getValue(), this.hdrTextureId);
    }

    static class IrradianceTexture extends Texture {

        static final OxyShader shader = OxyShader.createShader("OxyIBL", "shaders/OxyIBL.glsl");

        private final int irradianceTextureId;

        IrradianceTexture(String path, int hdrTextureId, int captureFBO, int captureRBO) {
            super(path);

            irradianceTextureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, irradianceTextureId);
            for (int i = 0; i < 6; ++i) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB32F, 32, 32, 0,
                        GL_RGB, GL_FLOAT, (FloatBuffer) null);
            }
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 32, 32);

            shader.begin();
            shader.setUniform1i("u_skyBoxTextureIBL", 0);
            shader.setUniformMatrix4fv("u_projectionIBL", captureProjection, true);
            shader.end();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_CUBE_MAP, hdrTextureId);
            glViewport(0, 0, 32, 32);
            glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
            for (int i = 0; i < 6; ++i) {
                shader.begin();
                shader.setUniformMatrix4fv("u_viewIBL", captureViews[i], true);
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                        GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, irradianceTextureId, 0);
                OxyRenderer.clearBuffer();
                SkyLight.mesh.render();
                shader.end();
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        @Override
        public void dispose() {
            glDeleteTextures(irradianceTextureId);
        }
    }

    static class PrefilterTexture extends Texture {

        static final OxyShader shader = OxyShader.createShader("OxyPrefiltering", "shaders/OxyPrefiltering.glsl");

        private final int prefilterTextureId;

        PrefilterTexture(String path, int hdrTextureId, int captureFBO, int captureRBO) {
            super(path);

            prefilterTextureId = glGenTextures();
            glBindTexture(GL_TEXTURE_CUBE_MAP, prefilterTextureId);
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
            glBindTexture(GL_TEXTURE_CUBE_MAP, hdrTextureId);
            glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
            int maxMipLevels = 10;
            for (int mip = 0; mip < maxMipLevels; ++mip) {
                int mipWidth = (int) (512 * Math.pow(0.5f, mip));
                int mipHeight = (int) (512 * Math.pow(0.5f, mip));
                glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);
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
                            GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, prefilterTextureId, mip);
                    OxyRenderer.clearBuffer();
                    SkyLight.mesh.render();
                    shader.end();
                }
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        @Override
        public void dispose() {
            glDeleteTextures(prefilterTextureId);
        }
    }

    static class BDRF extends Texture {

        static final OxyShader shader = OxyShader.createShader("OxyBDRF", "shaders/OxyBDRF.glsl");

        private final int bdrfTextureId;

        BDRF(String path, int captureFBO, int captureRBO) {
            super(path);

            bdrfTextureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, bdrfTextureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, 512, 512, 0, GL_RG, GL_FLOAT, 0);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindFramebuffer(GL_FRAMEBUFFER, captureFBO);
            glBindRenderbuffer(GL_RENDERBUFFER, captureRBO);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, 512, 512);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, bdrfTextureId, 0);
            glViewport(0, 0, 512, 512);
            OxyRenderer.clearBuffer();
            shader.begin();
            renderQuad();
            shader.end();
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        @Override
        public void dispose() {
            glDeleteTextures(bdrfTextureId);
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