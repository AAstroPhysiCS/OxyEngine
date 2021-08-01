package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Buffer.RenderBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;

import static org.lwjgl.opengl.GL13.*;

public final class OxyPrefilter {

    private final CubeTexture prefilter;

    public OxyPrefilter(CubeTexture finalTexture, FrameBuffer captureFBO, RenderBuffer captureRBO, TextureSlot prefilterSlot) {

        prefilter = OxyTexture.loadCubemap(prefilterSlot, 512, 512, TexturePixelType.Float, TextureFormat.RGB32F,
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
        shader.setUniformMatrix4fv("u_projectionPrefilter", TextureGlobals.captureProjection);
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
                shader.setUniformMatrix4fv("u_viewPrefilter", TextureGlobals.captureViews[i]);
                captureFBO.attachColorAttachment(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, prefilter.getTextureId(), mip);
                OxyRenderer.clearBuffer();
                OxyRenderer.renderSkyLight(shader);
                shader.end();
            }
        }
        captureRBO.unbind();
        captureFBO.unbind();
    }

    public void dispose(){
        prefilter.dispose();
    }

    public CubeTexture getTexture() {
        return prefilter;
    }
}
