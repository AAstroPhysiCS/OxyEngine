package OxyEngine.Core.Renderer.Texture;

import OxyEngine.Scene.Scene;

import static OxyEngine.System.OxySystem.*;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OxyTexture {

    private OxyTexture() {
    }

    public static void unbindAllTextures() {
        for (int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }

    public static Image2DTexture loadImage(TextureSlot slot, String path) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty() || path.isBlank()) return null;
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return Image2DTexture.create(slot, path, null);
    }

    public static Image2DTexture loadImage(TextureSlot slot, String path, float[] tcs) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty()) return null;
        assert slot.getValue() > 0 : oxyAssert("Texture Slot already being used");
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return Image2DTexture.create(slot, path, tcs);
    }

    public static Image2DTexture loadImage(Image2DTexture other) {
        TextureSlot slot = TextureSlot.find(other.getTextureSlot());
        String path = other.getPath();
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty()) return null;
        assert slot.getValue() > 0 : oxyAssert("Texture Slot already being used");
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return Image2DTexture.create(slot, path, other.getTextureCoords());
    }

    public static CubeTexture loadCubemap(TextureSlot slot, String path, Scene scene) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty()) return null;
        assert slot.getValue() > 0 : oxyAssert("Texture Slot already being used");
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return CubeTexture.create(slot, path, scene);
    }

    public static HDRTexture loadHDRTexture(String path) {
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return HDRTexture.create(TextureSlot.HDR, TextureSlot.PREFILTER, TextureSlot.IRRADIANCE, TextureSlot.BDRF, path);
    }
}
