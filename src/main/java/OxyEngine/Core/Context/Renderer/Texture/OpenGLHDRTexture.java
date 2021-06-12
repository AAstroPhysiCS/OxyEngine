package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLRenderBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Buffer.RenderBuffer;
import OxyEngine.Core.Context.Renderer.Light.SkyLight;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Scene.SceneRenderer;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OpenGLHDRTexture extends HDRTexture {

    private Irradiance irradiance;
    private Prefilter prefilter;
    private BDRF bdrf;

    private OpenGLRenderBuffer captureRBO;
    private OpenGLFrameBuffer captureFBO;

    private final OxyPipeline hdrPipeline;

    protected OpenGLHDRTexture(TextureSlot hdr, TextureSlot prefilter, TextureSlot radiance, TextureSlot bdrf, String path) {
        super(hdr, prefilter, radiance, bdrf, path);
        assert hdr.getValue() != 0 || prefilter.getValue() != 0 || radiance.getValue() != 0 || bdrf.getValue() != 0 : oxyAssert("Slots can not be 0");
        this.hdrPipeline = SceneRenderer.getInstance().getHDRPipeline();
        initHDR();
    }

    private void initHDR() {

        captureRBO = RenderBuffer.create(TextureFormat.DEPTHCOMPONENT24, 1920, 1920);
        captureFBO = FrameBuffer.create(1920, 1920,
                FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .useRenderBuffer(captureRBO));

        //UNUSED because we dont care about the slot now = 0
        hdrTexture2D = OxyTexture.loadImage(TextureSlot.UNUSED, path, TexturePixelType.Float, TextureFormat.RGB32F,
                TextureParameterBuilder.create()
                        .setMinFilter(TextureParameter.LINEAR)
                        .setMagFilter(TextureParameter.LINEAR)
                        .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapS(TextureParameter.CLAMP_TO_EDGE));

        finalTexture = OxyTexture.loadCubemap(finalTextureHdrSlot, 1920, 1920, TexturePixelType.Float, TextureFormat.RGB32F,
                TextureParameterBuilder.create()
                        .setMinFilter(TextureParameter.LINEAR_MIPMAP_LINEAR)
                        .setMagFilter(TextureParameter.LINEAR)
                        .setWrapR(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapS(TextureParameter.CLAMP_TO_EDGE)
                        .enableMipMap(true));

        OxyShader hdrShader = hdrPipeline.getShader();
        hdrShader.begin();
        hdrShader.setUniform1i("u_hdrTexture", 0);
        hdrShader.setUniformMatrix4fv("u_projectionHDR", captureProjection, true);
        hdrShader.end();

        if (SkyLight.mesh.empty())
            SkyLight.mesh.load(hdrPipeline);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, hdrTexture2D.getTextureId());
        glViewport(0, 0, 1920, 1920);
        captureFBO.bind();

        for (int i = 0; i < 6; i++) {
            hdrShader.begin();
            hdrShader.setUniformMatrix4fv("u_viewHDR", captureViews[i], true);
            captureFBO.attachColorAttachment(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, finalTexture.getTextureId());
            OxyRenderer.clearBuffer();
            SkyLight.mesh.render();
            hdrShader.end();
        }

        captureFBO.unbind();
        glBindTexture(GL_TEXTURE_CUBE_MAP, finalTexture.getTextureId());
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        irradiance = new Irradiance((OpenGLCubeTexture) finalTexture, captureFBO, captureRBO, radianceSlot);
        prefilter = new Prefilter((OpenGLCubeTexture) finalTexture, captureFBO, captureRBO, prefilterSlot);
        bdrf = new BDRF(captureFBO, captureRBO, bdrfSlot);

        OxyTexture.unbindAllTextures();
    }

    @Override
    public void dispose() {
        captureFBO.dispose();
        captureRBO.dispose();
        hdrTexture2D.dispose();
        finalTexture.dispose();
        bdrf.dispose();
        prefilter.dispose();
        irradiance.dispose();
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        quadVAO = 0;
        quadVBO = 0;
    }

    @Override
    public void bindAll() {
        if (bdrf != null) glBindTextureUnit(bdrf.getTexture().getTextureSlot(), bdrf.getTexture().getTextureId());
        if (prefilter != null)
            glBindTextureUnit(prefilter.getTexture().getTextureSlot(), prefilter.getTexture().getTextureId());
        if (irradiance != null)
            glBindTextureUnit(irradiance.getTexture().getTextureSlot(), irradiance.getTexture().getTextureId());
        if (finalTexture != null) glBindTextureUnit(finalTextureHdrSlot.getValue(), finalTexture.getTextureId());
    }

    private static final class Irradiance {

        private final OpenGLCubeTexture radiance;

        Irradiance(OpenGLCubeTexture finalTexture, OpenGLFrameBuffer captureFBO, OpenGLRenderBuffer captureRBO, TextureSlot radianceSlot) {

            radiance = (OpenGLCubeTexture) OxyTexture.loadCubemap(radianceSlot, 32, 32, TexturePixelType.Float, TextureFormat.RGB32F,
                    TextureParameterBuilder.create()
                            .setMinFilter(TextureParameter.LINEAR)
                            .setMagFilter(TextureParameter.LINEAR)
                            .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                            .setWrapR(TextureParameter.CLAMP_TO_EDGE)
                            .setWrapS(TextureParameter.CLAMP_TO_EDGE));

            captureFBO.bind();
            captureRBO.bind();
            captureRBO.loadStorage(32, 32);

            OxyShader shader = ShaderLibrary.get("OxyIBL");
            shader.begin();
            shader.setUniform1i("u_skyBoxTextureIBL", radianceSlot.getValue());
            shader.setUniformMatrix4fv("u_projectionIBL", captureProjection, true);
            shader.end();

            glActiveTexture(GL_TEXTURE0 + radianceSlot.getValue());
            glBindTexture(GL_TEXTURE_CUBE_MAP, finalTexture.getTextureId());
            glViewport(0, 0, 32, 32);
            for (int i = 0; i < 6; ++i) {
                shader.begin();
                shader.setUniformMatrix4fv("u_viewIBL", captureViews[i], true);
                captureFBO.attachColorAttachment(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, radiance.getTextureId());
                OxyRenderer.clearBuffer();
                SkyLight.mesh.render();
                shader.end();
            }
            captureFBO.unbind();
        }

        public OpenGLCubeTexture getTexture() {
            return radiance;
        }

        public void dispose() {
            radiance.dispose();
        }
    }

    private static final class Prefilter {

        final OpenGLCubeTexture prefilter;

        Prefilter(OpenGLCubeTexture finalTexture, OpenGLFrameBuffer captureFBO, OpenGLRenderBuffer captureRBO, TextureSlot prefilterSlot) {

            prefilter = (OpenGLCubeTexture) OxyTexture.loadCubemap(prefilterSlot, 512, 512, TexturePixelType.Float, TextureFormat.RGB32F,
                    TextureParameterBuilder.create()
                            .setMinFilter(TextureParameter.LINEAR_MIPMAP_LINEAR)
                            .setMagFilter(TextureParameter.LINEAR)
                            .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                            .setWrapR(TextureParameter.CLAMP_TO_EDGE)
                            .setWrapS(TextureParameter.CLAMP_TO_EDGE)
                            .enableMipMap(true));

            OxyShader shader = ShaderLibrary.get("OxyPrefiltering");
            shader.begin();
            shader.setUniform1i("u_skyBoxTexturePrefilter", prefilterSlot.getValue());
            shader.setUniformMatrix4fv("u_projectionPrefilter", captureProjection, true);
            shader.setUniform1f("u_faceSize", 1920);
            shader.end();

            glActiveTexture(GL_TEXTURE0 + prefilterSlot.getValue());
            glBindTexture(GL_TEXTURE_CUBE_MAP, finalTexture.getTextureId());
            captureFBO.bind();
            int maxMipLevels = 10;
            for (int mip = 0; mip < maxMipLevels; ++mip) {
                int mipWidth = (int) (512 * Math.pow(0.5f, mip));
                int mipHeight = (int) (512 * Math.pow(0.5f, mip));
                captureRBO.bind();
                captureRBO.loadStorage(mipWidth, mipHeight);
                glViewport(0, 0, mipWidth, mipHeight);
                float roughness = (float) mip / (float) (maxMipLevels - 1);
                shader.begin();
                shader.setUniform1f("u_roughness", roughness);
                shader.end();
                for (int i = 0; i < 6; ++i) {
                    shader.begin();
                    shader.setUniformMatrix4fv("u_viewPrefilter", captureViews[i], true);
                    captureFBO.attachColorAttachment(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, prefilter.getTextureId(), mip);
                    OxyRenderer.clearBuffer();
                    SkyLight.mesh.render();
                    shader.end();
                }
            }
            captureRBO.unbind();
            captureFBO.unbind();
        }

        public OpenGLCubeTexture getTexture() {
            return prefilter;
        }

        public void dispose() {
            prefilter.dispose();
        }
    }

    private static final class BDRF {

        private final OpenGLImage2DTexture bdrf;

        BDRF(OpenGLFrameBuffer captureFBO, OpenGLRenderBuffer captureRBO, TextureSlot bdrfSlot) {

            bdrf = (OpenGLImage2DTexture) OxyTexture.loadImage(bdrfSlot, 512, 512, TexturePixelType.Float, TextureFormat.RG16F,
                    TextureParameterBuilder.create()
                            .setMinFilter(TextureParameter.LINEAR)
                            .setMagFilter(TextureParameter.LINEAR)
                            .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                            .setWrapS(TextureParameter.CLAMP_TO_EDGE));

            OxyShader shader = ShaderLibrary.get("OxyBDRF");
            captureFBO.bind();
            captureRBO.bind();
            captureRBO.loadStorage(512, 512);
            captureFBO.attachColorAttachment(GL_TEXTURE_2D, 0, bdrf.getTextureId());
            glViewport(0, 0, 512, 512);
            OxyRenderer.clearBuffer();
            shader.begin();
            renderQuad();
            shader.end();
            captureRBO.unbind();
            captureFBO.unbind();
        }

        public OpenGLImage2DTexture getTexture() {
            return bdrf;
        }

        public void dispose() {
            bdrf.dispose();
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