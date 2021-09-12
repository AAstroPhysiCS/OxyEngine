package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.FrameBufferSpecification;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.OpenGLRenderBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Mesh.RenderBuffer;
import OxyEngine.Core.Context.Renderer.Shader;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL30.*;

public final class OpenGLEnvironmentTexture extends EnvironmentTexture {

    private Irradiance irradiance;
    private Prefilter prefilter;

    private static BDRF bdrf;

    protected OpenGLEnvironmentTexture(TextureSlot hdr, TextureSlot prefilter, TextureSlot radiance, TextureSlot bdrf, String path) {
        super(hdr, prefilter, radiance, bdrf, path);
        assert hdr.getValue() != 0 || prefilter.getValue() != 0 || radiance.getValue() != 0 || bdrf.getValue() != 0 : oxyAssert("Slots can not be 0");
        initHDR();
    }

    @Override
    public void bind() {
        if (bdrf != null)
            bdrf.getTexture().bind();
        if (prefilter != null)
            prefilter.getTexture().bind();
        if (irradiance != null)
            irradiance.getTexture().bind();
        if (finalTexture != null)
            finalTexture.bind();
    }

    private void initHDR() {

        OpenGLRenderBuffer captureRBO = RenderBuffer.create(TextureFormat.DEPTHCOMPONENT24, 2048, 2048);
        OpenGLFrameBuffer captureFBO = FrameBuffer.create(2048, 2048,
                FrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .useRenderBuffer(captureRBO));

        if (hdrTexture2D != null) hdrTexture2D.dispose();
        if (finalTexture != null) finalTexture.dispose();

        //UNUSED because we dont care about the slot now = 0
        hdrTexture2D = Texture.loadImage(TextureSlot.UNUSED, path, TexturePixelType.Float, TextureFormat.RGB32F,
                TextureParameterBuilder.create()
                        .setMinFilter(TextureParameter.LINEAR)
                        .setMagFilter(TextureParameter.LINEAR)
                        .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapS(TextureParameter.CLAMP_TO_EDGE));

        finalTexture = Texture.loadCubemap(finalTextureHdrSlot, 2048, 2048, TexturePixelType.Float, TextureFormat.RGB32F,
                TextureParameterBuilder.create()
                        .setMinFilter(TextureParameter.LINEAR_MIPMAP_LINEAR)
                        .setMagFilter(TextureParameter.LINEAR)
                        .setWrapR(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapS(TextureParameter.CLAMP_TO_EDGE)
                        .enableMipMap(true));

        Shader equirectangularToCubemap = Renderer.getShader("OxyEquirectangularToCubemap");
        equirectangularToCubemap.begin();
        equirectangularToCubemap.setUniform1i("u_hdrTexture", hdrTexture2D.getTextureSlot());
        equirectangularToCubemap.setUniformMatrix4fv("u_projectionHDR", TextureGlobals.captureProjection);
        equirectangularToCubemap.end();

        hdrTexture2D.bind();
        glViewport(0, 0, 2048, 2048);
        captureFBO.bind();

        for (int i = 0; i < 6; i++) {
            equirectangularToCubemap.begin();
            equirectangularToCubemap.setUniformMatrix4fv("u_viewHDR", TextureGlobals.captureViews[i]);
            captureFBO.attachColorAttachment(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, finalTexture.getTextureId());
            Renderer.clearBuffer();
            Renderer.renderSkyLight(equirectangularToCubemap);
            equirectangularToCubemap.end();
        }

        captureFBO.unbind();
        finalTexture.bind();
        glBindTexture(GL_TEXTURE_CUBE_MAP, finalTexture.getTextureId());
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        if (irradiance != null) irradiance.dispose();
        if (prefilter != null) prefilter.dispose();

        irradiance = new Irradiance(finalTexture, captureFBO, captureRBO, radianceSlot);
        prefilter = new Prefilter(finalTexture, captureFBO, captureRBO, prefilterSlot);
        if (bdrf == null) bdrf = new BDRF(captureFBO, captureRBO, bdrfSlot);

        Texture.unbindAllTextures();

        captureFBO.dispose();
        captureRBO.dispose();
    }

    @Override
    public void dispose() {
        if (irradiance != null) irradiance.dispose();
        if (prefilter != null) prefilter.dispose();
        if (hdrTexture2D != null) hdrTexture2D.dispose();
        if (finalTexture != null) finalTexture.dispose();
//        bdrf.dispose(); should not get destroyed
        irradiance = null;
        prefilter = null;
        hdrTexture2D = null;
        finalTexture = null;
    }

}