package OxyEngine.Core.Renderer.Texture;

import OxyEngine.System.OxyDisposable;
import OxyEngine.Scene.Scene;
import OxyEngine.TextureSlot;

import static OxyEngine.System.OxySystem.*;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public class OxyTexture {

    private OxyTexture() {
    }

    public static void unbindAllTextures() {
        for (int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }

    public static abstract class AbstractTexture implements OxyDisposable {

        protected int textureId;
        protected final TextureSlot textureSlot;
        protected final String path;

        public AbstractTexture(TextureSlot slot, String path) {
            this.path = path;
            this.textureSlot = slot;
        }

        @Override
        public void dispose() {
            glDeleteTextures(textureId);
        }

        public boolean empty() {
            return textureId == 0;
        }

        public String getPath() {
            return path;
        }

        public int getTextureId() {
            return textureId;
        }

        public int getTextureSlot() {
            return textureSlot.getValue();
        }
    }

    public static ImageTexture loadImage(TextureSlot slot, String path) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty() || path.isBlank()) return null;
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return new ImageTexture(slot, path, null);
    }

    public static ImageTexture loadImage(TextureSlot slot, String path, float[] tcs) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty()) return null;
        assert slot.getValue() > 0 : oxyAssert("Texture Slot already being used");
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return new ImageTexture(slot, path, tcs);
    }

    public static CubemapTexture loadCubemap(TextureSlot slot, String path, Scene scene) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty()) return null;
        assert slot.getValue() > 0 : oxyAssert("Texture Slot already being used");
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return new CubemapTexture(slot, path, scene);
    }

    public static HDRTexture loadHDRTexture(String path, Scene scene) {
        if (!isValidPath(path)) {
            logger.warning("Path not valid!");
            return null;
        }
        return new HDRTexture(TextureSlot.HDR, path, scene);
    }
}
