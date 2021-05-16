package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.TargetPlatform;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

public abstract class Image2DTexture extends Texture {

    protected final TextureSlot textureSlot;
    protected int textureId;

    protected final float[] tcs;

    protected Image2DTexture(TextureSlot slot, String path, float[] tcs) {
        super(path);
        this.textureSlot = slot;
        this.tcs = tcs;
    }

    protected Image2DTexture(Image2DTexture other){
        this(other.textureSlot, other.path, other.tcs);
        this.alFormat = other.alFormat;
    }

    static Image2DTexture create(TextureSlot slot, String path, float[] tcs){
        if(OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL){
            return new OpenGLImage2DTexture(slot, path, tcs);
        }
        throw new IllegalStateException("API not supported yet!");
    }

    @Override
    public void dispose(){
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
