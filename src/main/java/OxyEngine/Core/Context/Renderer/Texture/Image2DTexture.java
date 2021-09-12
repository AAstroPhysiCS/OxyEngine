package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.TextureFormat;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.glDeleteTextures;

public abstract class Image2DTexture extends TextureBase {

    protected final TextureSlot textureSlot;
    protected int textureId;

    protected final float[] tcs;

    protected Image2DTexture(TextureSlot slot, String path, float[] tcs, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        super(path, pixelType, parameterBuilder);
        this.textureSlot = slot;
        this.tcs = tcs;
    }

    protected Image2DTexture(Image2DTexture other) {
        this(other.textureSlot, other.path, other.tcs, other.pixelType, other.parameterBuilder);
        this.alFormat = other.alFormat;
    }

    static Image2DTexture create(TextureSlot slot, String path, float[] tcs, TexturePixelType pixelType, TextureFormat textureFormat, TextureParameterBuilder parameterBuilder) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            if (parameterBuilder instanceof TextureParameterBuilder.POpenGL pOpenGL)
                return new OpenGLImage2DTexture(slot, path, tcs, pixelType, textureFormat, pOpenGL);
            else oxyAssert("Wrong parameter is being used!");
        }
        throw new IllegalStateException("API not supported yet!");
    }

    static Image2DTexture create(TextureSlot slot, String path, int width, int height, TextureParameterBuilder parameterBuilder) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            if (parameterBuilder instanceof TextureParameterBuilder.POpenGL pOpenGL)
                return new OpenGLImage2DTexture(slot, path, null, width, height, pOpenGL);
            else oxyAssert("Wrong parameter is being used!");
        }
        throw new IllegalStateException("API not supported yet!");
    }

    static Image2DTexture create(TextureSlot slot,int width, int height, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder parameterBuilder) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            if (parameterBuilder instanceof TextureParameterBuilder.POpenGL pOpenGL)
                return new OpenGLImage2DTexture(slot, null, null, width, height, pixelType, format, pOpenGL);
            else oxyAssert("Wrong parameter is being used!");
        }
        throw new IllegalStateException("API not supported yet!");
    }

    static Image2DTexture create(TextureSlot slot, String path, float[] tcs, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            if (parameterBuilder instanceof TextureParameterBuilder.POpenGL pOpenGL)
                return new OpenGLImage2DTexture(slot, path, tcs, pixelType, pOpenGL);
            else oxyAssert("Wrong parameter is being used!");
        }
        throw new IllegalStateException("API not supported yet!");
    }

    @Override
    public void dispose() {
        glDeleteTextures(textureId);
    }

    public float[] getTextureCoords() {
        return tcs;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getTextureSlot() {
        return textureSlot.getValue();
    }

    public int getFormat() {
        return alFormat;
    }
}
