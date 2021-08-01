package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.TextureFormat;
import OxyEngine.Core.Context.Renderer.Buffer.RenderBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;

import static org.lwjgl.opengl.GL30.*;

public final class OxyBDRF {

    private final Image2DTexture bdrf;

    public OxyBDRF(FrameBuffer captureFBO, RenderBuffer captureRBO, TextureSlot bdrfSlot) {

        bdrf = OxyTexture.loadImage(bdrfSlot, 512, 512, TexturePixelType.Float, TextureFormat.RG16F,
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

    /* TODO: THIS CODE NEEDS TO GET CHANGED (ITS OPENGL ONLY) */
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

    public Image2DTexture getTexture() {
        return bdrf;
    }

    public void dispose() {
        bdrf.dispose();
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        quadVAO = 0;
        quadVBO = 0;
    }
}
