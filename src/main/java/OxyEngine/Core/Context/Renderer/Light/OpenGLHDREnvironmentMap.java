package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Core.Context.Renderer.Texture.OpenGLHDRTexture;
import OxyEngine.Core.Context.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxyDisposable;
import org.lwjgl.stb.STBImage;

import static OxyEngine.System.OxySystem.logger;

public class OpenGLHDREnvironmentMap extends SkyLight implements OxyDisposable {

    private OpenGLHDRTexture hdrTexture;
    public float[] mipLevelStrength = new float[]{1.0f};

    public OpenGLHDREnvironmentMap() {
    }

    @Override
    public void bind() {
        if (hdrTexture != null) hdrTexture.bind();
    }

    public boolean loadEnvironmentMap(String pathToHDR) {
        if (pathToHDR == null) return false;
        if (!STBImage.stbi_is_hdr(pathToHDR)) {
            logger.severe("Image is not HDR");
            return false;
        }

        dispose();
        hdrTexture = (OpenGLHDRTexture) OxyTexture.loadHDRTexture(pathToHDR);
        return true;
    }

    public OpenGLHDRTexture getHDRTexture() {
        return hdrTexture;
    }

    @Override
    public void dispose() {
        if (hdrTexture != null)
            hdrTexture.dispose();
    }
}
