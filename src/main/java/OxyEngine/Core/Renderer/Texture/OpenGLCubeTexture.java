package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.Core.Renderer.Renderer;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.opengl.GL45.glCreateTextures;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public final class OpenGLCubeTexture extends CubeTexture {

    OpenGLCubeTexture(TextureSlot slot, int width, int height, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder.POpenGL parameter) {
        super(slot, null, pixelType, parameter);
        this.width = width;
        this.height = height;

        assert slot.getValue() != 0 : oxyAssert("Slot can not be 0");

        textureId = glCreateTextures(GL_TEXTURE_CUBE_MAP);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);

        switch (pixelType) {
            case Float -> {
                for (int i = 0; i < 6; ++i) {
                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, format.getInternalFormat(),
                            width, height, 0, format.getStorageFormat(), GL_FLOAT, (FloatBuffer) null);
                }
            }
            case UByte -> {
                for (int i = 0; i < 6; ++i) {
                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, format.getInternalFormat(),
                            width, height, 0, format.getStorageFormat(), GL_UNSIGNED_BYTE, (ByteBuffer) null);
                }
            }
        }
        if (parameter.minFilterParameter != null)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, parameter.minFilterParameter.apiValue);
        if (parameter.magFilterParameter != null)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, parameter.magFilterParameter.apiValue);
        if (parameter.wrapSParameter != null)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, parameter.wrapSParameter.apiValue);
        if (parameter.wrapRParameter != null)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, parameter.wrapRParameter.apiValue);
        if (parameter.wrapTParameter != null)
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, parameter.wrapTParameter.apiValue);
        if (parameter.lodBias != -1) glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, parameter.lodBias);
        if (parameter.generateMipMap) glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
    }

    OpenGLCubeTexture(TextureSlot slot, String path, TexturePixelType pixelType, TextureParameterBuilder.POpenGL parameter) {
        super(slot, path, pixelType, parameter);

        assert slot.getValue() != 0 : oxyAssert("Slot can not be 0");

        textureId = glCreateTextures(GL_TEXTURE_CUBE_MAP);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId);

        List<String> totalFiles = new ArrayList<>();
        File[] files = Objects.requireNonNull(new File(path).listFiles());
        for (String structureName : List.of("right", "left", "bottom", "top", "front", "back")) {
            for (File f : files) {
                String name = f.getName().split("\\.")[0];
                if (structureName.equals(name)) {
                    totalFiles.add(f.getPath());
                }
            }
        }
        switch (pixelType) {
            case UByte -> {
                for (int i = 0; i < 6; i++) {
                    loadAsByteBuffer(totalFiles.get(i));
                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, alFormat, this.width, this.height, 0, alFormat, GL_UNSIGNED_BYTE, (FloatBuffer) textureBuffer);
                    if (parameter.minFilterParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, parameter.minFilterParameter.apiValue);
                    if (parameter.magFilterParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, parameter.magFilterParameter.apiValue);
                    if (parameter.wrapSParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, parameter.wrapSParameter.apiValue);
                    if (parameter.wrapRParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, parameter.wrapRParameter.apiValue);
                    if (parameter.wrapTParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, parameter.wrapTParameter.apiValue);
                    if (parameter.lodBias != -1)
                        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, parameter.lodBias);
                    if (parameter.generateMipMap) glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
                    stbi_image_free((FloatBuffer) textureBuffer);
                }
            }

            case Float -> {
                for (int i = 0; i < 6; i++) {
                    loadAsFloatBuffer(totalFiles.get(i));
                    glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, alFormat, this.width, this.height, 0, alFormat, GL_FLOAT, (ByteBuffer) textureBuffer);
                    if (parameter.minFilterParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, parameter.minFilterParameter.apiValue);
                    if (parameter.magFilterParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, parameter.magFilterParameter.apiValue);
                    if (parameter.wrapSParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, parameter.wrapSParameter.apiValue);
                    if (parameter.wrapRParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, parameter.wrapRParameter.apiValue);
                    if (parameter.wrapTParameter != null)
                        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, parameter.wrapTParameter.apiValue);
                    if (parameter.lodBias != -1)
                        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, parameter.lodBias);
                    if (parameter.generateMipMap) glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
                    stbi_image_free((ByteBuffer) textureBuffer);
                }
            }
        }
    }

    @Override
    public void bind() {
        glBindTextureUnit(textureSlot.getValue(), textureId);
    }

    @Override
    public void unbind() {
        glBindTextureUnit(textureSlot.getValue(), 0);
    }
}