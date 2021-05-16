package OxyEngine.Core.Renderer.Texture;

import OxyEngine.System.OxyDisposable;

import java.nio.Buffer;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public abstract class Texture implements OxyDisposable {

    protected final String path;

    protected int alFormat = -1;

    private static final int[] widthBuffer = new int[1];
    private static final int[] heightBuffer = new int[1];
    private static final int[] channelBuffer = new int[1];

    protected int width, height, channel;

    protected Buffer textureBuffer;

    protected Texture(String path) {
        this.path = path;
    }

    public void loadAsByteBuffer() {
        loadAsByteBuffer(this.path, true);
    }

    public void loadAsByteBuffer(String path) {
        loadAsByteBuffer(path, true);
    }

    public void loadAsByteBuffer(String path, boolean flip) {
        stbi_set_flip_vertically_on_load(flip);
        textureBuffer = stbi_load(path, widthBuffer, heightBuffer, channelBuffer, 0);
        if (textureBuffer == null) {
            logger.warning("Texture: " + path + " could not be loaded!");
        }
        width = widthBuffer[0];
        height = heightBuffer[0];
        channel = channelBuffer[0];
        if(channel == 1)
            alFormat = GL_RED;
        else if(channel == 3)
            alFormat = GL_RGB;
        else if(channel == 4)
            alFormat = GL_RGBA;
    }

    public void loadAsFloatBuffer() {
        loadAsFloatBuffer(this.path, true);
    }

    public void loadAsFloatBuffer(String path) {
        loadAsFloatBuffer(path, true);
    }

    public void loadAsFloatBuffer(String path, boolean flip) {
        stbi_set_flip_vertically_on_load(flip);
        textureBuffer = stbi_loadf(path, widthBuffer, heightBuffer, channelBuffer, 0);
        if (textureBuffer == null) {
            logger.warning("Texture: " + path + " could not be loaded!");
        }
        width = widthBuffer[0];
        height = heightBuffer[0];
        channel = channelBuffer[0];
    }

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
