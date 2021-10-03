package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Mesh.FrameBuffer;
import OxyEngine.Core.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Renderer.Mesh.RenderBuffer;
import OxyEngine.Core.Renderer.Shader;

import static org.lwjgl.opengl.GL30.*;

public final class BDRF {

    private final Image2DTexture bdrf;

    public BDRF(FrameBuffer captureFBO, RenderBuffer captureRBO, TextureSlot bdrfSlot) {

        bdrf = Texture.loadImage(bdrfSlot, 512, 512, TexturePixelType.Float, TextureFormat.RG16F,
                TextureParameterBuilder.create()
                        .setMinFilter(TextureParameter.LINEAR)
                        .setMagFilter(TextureParameter.LINEAR)
                        .setWrapT(TextureParameter.CLAMP_TO_EDGE)
                        .setWrapS(TextureParameter.CLAMP_TO_EDGE));

        Shader shader = Renderer.getShader("OxyBDRF");
        captureFBO.bind();
        captureRBO.bind();
        captureRBO.loadStorage(512, 512);
        captureFBO.attachColorAttachment(GL_TEXTURE_2D, 0, bdrf.getTextureId());
        glViewport(0, 0, 512, 512);
        Renderer.clearBuffer();
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
