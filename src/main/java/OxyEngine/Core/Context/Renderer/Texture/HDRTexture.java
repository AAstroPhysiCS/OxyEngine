package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.System.OxyDisposable;
import OxyEngine.TargetPlatform;
import org.joml.Matrix4f;

public abstract class HDRTexture implements OxyDisposable {

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

    protected final TextureSlot finalTextureHdrSlot, prefilterSlot, radianceSlot, bdrfSlot;

    protected Image2DTexture hdrTexture2D;
    protected CubeTexture finalTexture;

    protected final String path;

    protected HDRTexture(TextureSlot hdr, TextureSlot prefilter, TextureSlot radiance, TextureSlot bdrf, String path) {
        this.path = path;
        this.finalTextureHdrSlot = hdr;
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
        return finalTextureHdrSlot.getValue();
    }

    public String getPath() {
        return path;
    }

    public abstract void bindAll();
}
