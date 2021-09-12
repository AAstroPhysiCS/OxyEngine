package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.Renderer.Mesh.Platform.TextureFormat;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.opengl.GL45.glCreateTextures;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public final class OpenGLImage2DTexture extends Image2DTexture {

    OpenGLImage2DTexture(TextureSlot slot, String path, float[] tcs, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder.POpenGL parameter) {
        this(slot, path, tcs, -1, -1, pixelType, format, parameter);
    }

    OpenGLImage2DTexture(TextureSlot slot, String path, float[] tcs, TexturePixelType pixelType, TextureParameterBuilder.POpenGL parameter) {
        this(slot, path, tcs, pixelType, null, parameter);
    }

    OpenGLImage2DTexture(TextureSlot slot, String path, float[] tcs, int width, int height, TextureParameterBuilder.POpenGL parameter) {
        this(slot, path, tcs, width, height, null, null, parameter);
    }

    OpenGLImage2DTexture(TextureSlot slot, String path, float[] tcs, int width, int height, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder.POpenGL parameter) {
        super(slot, path, tcs, pixelType, parameter);
        this.width = width;
        this.height = height;

        int slotValue = slot.getValue();
        assert slotValue != -10 : oxyAssert("No empty texture slot!");
//        assert slotValue != 0 : oxyAssert("Slot can not be 0");
        assert slotValue <= 32 : oxyAssert("32 Texture Slots exceeded!");

        if (pixelType != null && path != null) {
            switch (pixelType) {
                case Float -> loadAsFloatBuffer();
                case UByte -> loadAsByteBuffer();
            }
            assert alFormat != -1 : oxyAssert("Format not supported!");
        }

        textureId = glCreateTextures(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
        if (pixelType != null) {
            if (format != null) {
                switch (pixelType) {
                    case Float -> glTexImage2D(GL_TEXTURE_2D, 0, format.getInternalFormat(), this.width, this.height, 0, format.getStorageFormat(), GL_FLOAT, (FloatBuffer) textureBuffer);
                    case UByte -> glTexImage2D(GL_TEXTURE_2D, 0, format.getInternalFormat(), this.width, this.height, 0, format.getStorageFormat(), GL_UNSIGNED_BYTE, (ByteBuffer) textureBuffer);
                }
            } else {
                switch (pixelType) {
                    case Float -> glTexImage2D(GL_TEXTURE_2D, 0, alFormat, this.width, this.height, 0, alFormat, GL_FLOAT, (FloatBuffer) textureBuffer);
                    case UByte -> glTexImage2D(GL_TEXTURE_2D, 0, alFormat, this.width, this.height, 0, alFormat, GL_UNSIGNED_BYTE, (ByteBuffer) textureBuffer);
                }
            }
        } else throw new IllegalStateException("Pixel Type must be given!");

        if (parameter.minFilterParameter != null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, parameter.minFilterParameter.apiValue);
        if (parameter.magFilterParameter != null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, parameter.magFilterParameter.apiValue);
        if (parameter.wrapSParameter != null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, parameter.wrapSParameter.apiValue);
        if (parameter.wrapRParameter != null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, parameter.wrapRParameter.apiValue);
        if (parameter.wrapTParameter != null)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, parameter.wrapTParameter.apiValue);
        if (parameter.lodBias != -1) glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, parameter.lodBias);
        if (parameter.generateMipMap) glGenerateMipmap(GL_TEXTURE_2D);

        if (textureBuffer != null) {
            switch (pixelType) {
                case Float -> stbi_image_free((FloatBuffer) textureBuffer);
                case UByte -> stbi_image_free((ByteBuffer) textureBuffer);
            }
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        textureBuffer = null;
    }

    @Override
    public void bind() {
        glBindTextureUnit(textureSlot.getValue(), textureId);
    }
}