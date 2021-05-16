package OxyEngine.Core.Renderer.Texture;

import java.nio.ByteBuffer;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class OpenGLImage2DTexture extends Image2DTexture {

    OpenGLImage2DTexture(TextureSlot slot, String path, float[] tcs) {
        super(slot, path, tcs);

        int slotValue = slot.getValue();
        assert slotValue != -10 : oxyAssert("No empty texture slot!");
        assert slotValue != 0 : oxyAssert("Slot can not be 0");
        assert slotValue <= 32 : oxyAssert("32 Texture Slots exceeded!");

        loadAsByteBuffer();

        assert alFormat != -1 : oxyAssert("Format not supported!");

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, alFormat, width, height, 0, alFormat, GL_UNSIGNED_BYTE, (ByteBuffer) textureBuffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
//        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.4f);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free((ByteBuffer) textureBuffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        textureBuffer = null;
    }
}