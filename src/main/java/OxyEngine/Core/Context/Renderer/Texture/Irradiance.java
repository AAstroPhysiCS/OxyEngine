package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Mesh.RenderBuffer;
import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Shader;

import static org.lwjgl.opengl.GL13.*;

public final class Irradiance {

    private final CubeTexture radiance;

    public Irradiance(CubeTexture finalTexture, FrameBuffer captureFBO, RenderBuffer captureRBO, TextureSlot radianceSlot){
        radiance = Texture.loadCubemap(radianceSlot, 32, 32, TexturePixelType.Float, TextureFormat.RGB32F,
                TextureParameterBuilder.create()
                        .setMinFilter(TextureParameter.LINEAR)
                        .setMagFilter(TextureParameter.LINEAR)
                        .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapR(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapS(TextureParameter.CLAMP_TO_EDGE));

        captureFBO.bind();
        captureRBO.bind();
        captureRBO.loadStorage(32, 32);

        Shader shader = Renderer.getShader("OxyIBL");
        shader.begin();
        shader.setUniform1i("u_skyBoxTextureIBL", radianceSlot.getValue());
        shader.setUniformMatrix4fv("u_projectionIBL", TextureGlobals.captureProjection);
        shader.end();

        glActiveTexture(GL_TEXTURE0 + radianceSlot.getValue());
        glBindTexture(GL_TEXTURE_CUBE_MAP, finalTexture.getTextureId());
        glViewport(0, 0, 32, 32);
        for (int i = 0; i < 6; ++i) {
            shader.begin();
            shader.setUniformMatrix4fv("u_viewIBL", TextureGlobals.captureViews[i]);
            captureFBO.attachColorAttachment(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, radiance.getTextureId());
            Renderer.clearBuffer();
            Renderer.renderSkyLight(shader);
            shader.end();
        }
        captureFBO.unbind();
    }

    public void dispose(){
        radiance.dispose();
    }

    public CubeTexture getTexture() {
        return radiance;
    }
}
