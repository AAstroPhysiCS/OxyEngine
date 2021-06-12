package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.TextureFormat;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.glDeleteTextures;

public abstract class CubeTexture extends Texture {

    protected final TextureSlot textureSlot;
    protected int textureId;

    protected CubeTexture(TextureSlot slot, String path, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        super(path, pixelType, parameterBuilder);
        this.textureSlot = slot;
    }

    static CubeTexture create(TextureSlot slot, String path, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            if (parameterBuilder instanceof TextureParameterBuilder.POpenGL pOpenGL)
                return new OpenGLCubeTexture(slot, path, pixelType, pOpenGL);
            else oxyAssert("Wrong parameter is being used!");
        }
        throw new IllegalStateException("API not supported yet!");
    }

    static CubeTexture create(TextureSlot slot, int width, int height, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder parameterBuilder) {
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            if (parameterBuilder instanceof TextureParameterBuilder.POpenGL pOpenGL)
                return new OpenGLCubeTexture(slot, width, height, pixelType, format, pOpenGL);
            else oxyAssert("Wrong parameter is being used!");
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public int getTextureId() {
        return textureId;
    }

    public int getTextureSlot() {
        return textureSlot.getValue();
    }

    @Override
    public void dispose() {
        glDeleteTextures(textureId);
    }
}
