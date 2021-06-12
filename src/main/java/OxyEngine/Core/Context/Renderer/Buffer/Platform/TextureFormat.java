package OxyEngine.Core.Context.Renderer.Buffer.Platform;

import static org.lwjgl.opengl.GL30.*;

public enum TextureFormat {

    DEPTH24STENCIL8(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL_ATTACHMENT),

    RGBA8(GL_RGBA8, GL_RGBA),

    R32I(GL_R32I, GL_RED_INTEGER),

    RG16F(GL_RG16F, GL_RG),

    RGBA16(GL_RGBA16, GL_RGBA),

    RGB16F(GL_RGB16F, GL_RGB),

    DEPTHCOMPONENT32(GL_DEPTH_COMPONENT32, GL_DEPTH_ATTACHMENT),

    DEPTHCOMPONENT32COMPONENT(GL_DEPTH_COMPONENT32, GL_DEPTH_COMPONENT),

    DEPTHCOMPONENT24(GL_DEPTH_COMPONENT24, GL_DEPTH_ATTACHMENT),

    RGB32F(GL_RGB32F, GL_RGB),

    RGB(GL_RGB, GL_RGB),

    RGBA(GL_RGBA, GL_RGBA);

    final int internalFormatInteger;
    final int storageFormat;

    TextureFormat(int internalFormatInteger, int storageFormat) {
        this.storageFormat = storageFormat;
        this.internalFormatInteger = internalFormatInteger;
    }

    public int getStorageFormat() {
        return storageFormat;
    }

    public int getInternalFormat() {
        return internalFormatInteger;
    }
}
