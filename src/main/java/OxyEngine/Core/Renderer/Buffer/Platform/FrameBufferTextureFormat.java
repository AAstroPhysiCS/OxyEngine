package OxyEngine.Core.Renderer.Buffer.Platform;

import static org.lwjgl.opengl.GL30.*;

public enum FrameBufferTextureFormat {

    NONE(0, 0),

    DEPTH24STENCIL8(GL_DEPTH24_STENCIL8, 0),

    RGBA8(GL_RGBA8, GL_RGBA),

    R32I(GL_R32I, GL_RED_INTEGER),

    RGBA16(GL_RGBA16, GL_RGBA),

    RGB16F(GL_RGB16F, GL_RGB),

    RGB32F(GL_RGB32F, GL_RGB);

    final int storageFormat;
    final int internalFormatInteger;

    FrameBufferTextureFormat(int internalFormatInteger, int storageFormat) {
        this.storageFormat = storageFormat;
        this.internalFormatInteger = internalFormatInteger;
    }

    public int getStorageFormat() {
        return storageFormat;
    }

    public int getInternalFormatInteger() {
        return internalFormatInteger;
    }
}
