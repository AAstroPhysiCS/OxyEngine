package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.System.OxyDisposable;
import OxyEngine.TargetPlatform;

public abstract class HDRTexture implements OxyDisposable {

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
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            return new OpenGLHDRTexture(hdr, prefilter, irradiance, bdrf, path);
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public CubeTexture getFinalTexture() {
        return finalTexture;
    }

    public String getPath() {
        return path;
    }
}
