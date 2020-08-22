package OxyEngine.Core.Renderer.Texture;

import java.nio.ByteBuffer;

import static OxyEngine.Core.Renderer.Texture.OxyTexture.allTextures;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public class ImageTexture extends OxyTexture.Texture {

    private final float[] tcs;

    ImageTexture(int slot, String path, float[] tcs) {
        this.textureSlot = slot;
        this.tcs = tcs;
        this.path = path;

        assert slot != 0 : oxyAssert("Slot can not be 0");

        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];
        ByteBuffer buffer = loadTextureFile(path, width, height, channels);
        int alFormat = GL_RGBA;
        if (channels[0] == 3)
            alFormat = GL_RGB;

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.4f);

        glTexImage2D(GL_TEXTURE_2D, 0, alFormat, width[0], height[0], 0, alFormat, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(buffer);
        glBindTexture(GL_TEXTURE_2D, 0);

        allTextures.add(this);
    }

    public float[] getTextureCoords() {
        return tcs;
    }
}
