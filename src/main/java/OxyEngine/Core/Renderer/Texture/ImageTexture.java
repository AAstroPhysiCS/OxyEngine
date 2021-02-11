package OxyEngine.Core.Renderer.Texture;

import java.nio.ByteBuffer;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class ImageTexture extends OxyTexture.AbstractTexture {

    private final float[] tcs;
    private int alFormat = -1;

    public ImageTexture(ImageTexture other){
        this(other.textureSlot, other.path, other.tcs);
        this.alFormat = other.alFormat;
    }

    ImageTexture(int slot, String path, float[] tcs) {
        super(slot, path);
        this.tcs = tcs;

        assert slot != -10 : oxyAssert("No empty texture slot!");
        assert slot != 0 : oxyAssert("Slot can not be 0");
        assert slot <= 32 : oxyAssert("32 Texture Slots exceeded!");

        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer buffer = stbi_load(path, width, height, channels, 0);
        if (buffer == null){
            logger.warning("Texture: " + path + " could not be loaded!");
            return;
        }
        if (!buffer.hasRemaining()) return;

        if(channels[0] == 1)
            alFormat = GL_RED;
        else if(channels[0] == 3)
            alFormat = GL_RGB;
        else if(channels[0] == 4)
            alFormat = GL_RGBA;

        assert alFormat != -1 : oxyAssert("Format not supported!");

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, alFormat, width[0], height[0], 0, alFormat, GL_UNSIGNED_BYTE, buffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
//        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.4f);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public float[] getTextureCoords() {
        return tcs;
    }

    public int getFormat() {
        return alFormat;
    }
}
