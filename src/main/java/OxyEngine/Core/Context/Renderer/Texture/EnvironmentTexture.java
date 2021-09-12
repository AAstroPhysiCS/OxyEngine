package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.System.Disposable;
import OxyEngine.TargetPlatform;

public abstract class EnvironmentTexture implements Disposable {

    protected final TextureSlot finalTextureHdrSlot, prefilterSlot, radianceSlot, bdrfSlot;

    protected Image2DTexture hdrTexture2D;
    protected CubeTexture finalTexture;

    protected final String path;

    protected EnvironmentTexture(TextureSlot hdr, TextureSlot prefilter, TextureSlot radiance, TextureSlot bdrf, String path) {
        this.path = path;
        this.finalTextureHdrSlot = hdr;
        this.prefilterSlot = prefilter;
        this.radianceSlot = radiance;
        this.bdrfSlot = bdrf;
    }

    static EnvironmentTexture create(TextureSlot hdr, TextureSlot prefilter, TextureSlot irradiance, TextureSlot bdrf, String path) {
        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            return new OpenGLEnvironmentTexture(hdr, prefilter, irradiance, bdrf, path);
        }
        throw new IllegalStateException("API not supported yet!");
    }

    public abstract void bind();

    public String getPath() {
        return path;
    }
}
