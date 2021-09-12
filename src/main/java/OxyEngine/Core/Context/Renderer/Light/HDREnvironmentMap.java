package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Core.Context.Renderer.Texture.EnvironmentTexture;
import OxyEngine.Core.Context.Renderer.Texture.Texture;
import OxyEngine.System.Disposable;
import org.lwjgl.stb.STBImage;

import static OxyEngine.System.OxySystem.logger;

public final class HDREnvironmentMap extends SkyLight implements Disposable {

    private EnvironmentTexture environmentTexture;
    public float[] mipLevelStrength = new float[]{1.0f};

    public HDREnvironmentMap() {
    }

    @Override
    public void bind() {
        if (environmentTexture != null) environmentTexture.bind();
    }

    public boolean loadEnvironmentMap(String pathToHDR) {
        if (pathToHDR == null) return false;
        if (!STBImage.stbi_is_hdr(pathToHDR)) {
            logger.severe("Image is not HDR");
            return false;
        }

        dispose();
        environmentTexture = Texture.loadHDRTexture(pathToHDR);
        return true;
    }

    public EnvironmentTexture getEnvironmentTexture() {
        return environmentTexture;
    }

    @Override
    public void dispose() {
        if (environmentTexture != null)
            environmentTexture.dispose();
    }
}
