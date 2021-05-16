package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.TargetPlatform;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

public abstract class HDRTexture extends Texture {

    protected static final Matrix4f[] captureViews = new Matrix4f[]{
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
    };

    protected static final Matrix4f captureProjection = new Matrix4f().setPerspective((float) Math.toRadians(90), 1.0f, 0.4768f, 10.0f);

    protected final TextureSlot hdrSlot, prefilterSlot, radianceSlot, bdrfSlot;
    protected int hdrTextureId;

    protected HDRTexture(TextureSlot hdr, TextureSlot prefilter, TextureSlot radiance, TextureSlot bdrf, String path) {
        super(path);
        this.hdrSlot = hdr;
        this.prefilterSlot = prefilter;
        this.radianceSlot = radiance;
        this.bdrfSlot = bdrf;
    }

    static HDRTexture create(TextureSlot hdr, TextureSlot prefilter, TextureSlot irradiance, TextureSlot bdrf, String path) {
        if(OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL){
            return new OpenGLHDRTexture(hdr, prefilter, irradiance, bdrf, path);
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public int getIBLSlot() {
        return radianceSlot.getValue();
    }

    public int getPrefilterSlot() {
        return prefilterSlot.getValue();
    }

    public int getBDRFSlot() {
        return bdrfSlot.getValue();
    }

    public int getHDRSlot() {
        return hdrSlot.getValue();
    }

    public abstract void bindAll();

    @Override
    public void dispose(){
        glDeleteTextures(hdrTextureId);
    }
}
