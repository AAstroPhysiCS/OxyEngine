package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.Renderer.Buffer.Platform.TextureFormat;

import static OxyEngine.System.OxySystem.isValidPath;
import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OxyTexture {

    private OxyTexture() {
    }

    private static boolean check(String path) {
        if (path == null) return false;
        if (path.equals("null")) return false;
        if (path.isEmpty() || path.isBlank()) return false;
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return false;
        }
        return true;
    }

    public static void unbindAllTextures() {
        for (int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }

    public static Image2DTexture loadImage(TextureSlot slot, String path, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        if (!check(path)) return null;
        return Image2DTexture.create(slot, path, null, pixelType, parameterBuilder);
    }

    public static Image2DTexture loadImage(TextureSlot slot, String path, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder parameterBuilder) {
        if (!check(path)) return null;
        return Image2DTexture.create(slot, path, null, pixelType, format, parameterBuilder);
    }

    public static Image2DTexture loadImage(TextureSlot slot, int width, int height, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder parameterBuilder) {
        return Image2DTexture.create(slot, width, height, pixelType, format, parameterBuilder);
    }

    public static Image2DTexture loadImage(TextureSlot slot, String path, float[] tcs, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        if (!check(path)) return null;
        return Image2DTexture.create(slot, path, tcs, pixelType, parameterBuilder);
    }

    public static Image2DTexture loadImage(Image2DTexture other) {
        TextureSlot slot = TextureSlot.find(other.getTextureSlot());
        String path = other.getPath();
        if (!check(path)) return null;
        return Image2DTexture.create(slot, path, other.getTextureCoords(), other.pixelType, other.parameterBuilder);
    }

    public static CubeTexture loadCubemap(TextureSlot slot, int width, int height, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        return CubeTexture.create(slot, width, height, pixelType, TextureFormat.RGB32F, parameterBuilder);
    }

    public static CubeTexture loadCubemap(TextureSlot slot, int width, int height, TexturePixelType pixelType, TextureFormat format, TextureParameterBuilder parameterBuilder) {
        return CubeTexture.create(slot, width, height, pixelType, format, parameterBuilder);
    }

    public static CubeTexture loadCubemap(TextureSlot slot, String path, TexturePixelType pixelType, TextureParameterBuilder parameterBuilder) {
        if (!check(path)) return null;
        return CubeTexture.create(slot, path, pixelType, parameterBuilder);
    }

    public static HDRTexture loadHDRTexture(String path) {
        if (!check(path)) return null;
        return HDRTexture.create(TextureSlot.HDR, TextureSlot.PREFILTER, TextureSlot.IRRADIANCE, TextureSlot.BDRF, path);
    }
}
