package OxyEngine.Core.Renderer.Texture;

import OxyEngine.System.Disposable;

import java.nio.Buffer;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public abstract class TextureBase implements Disposable {

    protected final String path;

    protected int alFormat = -1;

    private static final int[] widthBuffer = new int[1];
    private static final int[] heightBuffer = new int[1];
    private static final int[] channelBuffer = new int[1];

    protected int width, height, channel;

    protected final TexturePixelType pixelType;
    protected Buffer textureBuffer;
    protected final TextureParameterBuilder parameterBuilder;

    protected TextureBase(String path, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        this.path = path;
        this.pixelType = pixelType;
        this.parameterBuilder = parameterBuilder;
    }

    protected void loadAsByteBuffer() {
        loadAsByteBuffer(this.path, true);
    }

    protected void loadAsByteBuffer(String path) {
        loadAsByteBuffer(path, true);
    }

    protected void loadAsByteBuffer(String path, boolean flip) {
        stbi_set_flip_vertically_on_load(flip);
        textureBuffer = stbi_load(path, widthBuffer, heightBuffer, channelBuffer, 0);
        if (textureBuffer == null) {
            logger.warning("Texture: " + path + " could not be loaded!");
        }
        width = widthBuffer[0];
        height = heightBuffer[0];
        channel = channelBuffer[0];
        if (channel == 1)
            alFormat = GL_RED;
        else if (channel == 3)
            alFormat = GL_RGB;
        else if (channel == 4)
            alFormat = GL_RGBA;
    }

    protected void loadAsFloatBuffer() {
        loadAsFloatBuffer(this.path, true);
    }

    protected void loadAsFloatBuffer(String path) {
        loadAsFloatBuffer(path, true);
    }

    protected void loadAsFloatBuffer(String path, boolean flip) {
        stbi_set_flip_vertically_on_load(flip);
        textureBuffer = stbi_loadf(path, widthBuffer, heightBuffer, channelBuffer, 0);
        if (textureBuffer == null) {
            logger.warning("Texture: " + path + " could not be loaded!");
        }
        width = widthBuffer[0];
        height = heightBuffer[0];
        channel = channelBuffer[0];
        if (channel == 1)
            alFormat = GL_RED;
        else if (channel == 3)
            alFormat = GL_RGB;
        else if (channel == 4)
            alFormat = GL_RGBA;
    }

    public abstract void bind();

    public abstract void unbind();

    public String getPath() {
        return path;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChannel() {
        return channel;
    }
}
