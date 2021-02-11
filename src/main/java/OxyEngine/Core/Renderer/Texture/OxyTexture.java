package OxyEngine.Core.Renderer.Texture;

import OxyEngine.System.OxyDisposable;
import OxyEngine.Scene.Scene;

import static OxyEngine.System.OxySystem.oxyAssert;
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
        protected final int textureSlot;
        protected final String path;

        public AbstractTexture(int slot, String path) {
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
            return textureSlot;
        }
    }

    public static ImageTexture loadImage(int slot, String path) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty() || path.isBlank()) return null;
        return new ImageTexture(slot, path, null);
    }

    public static ImageTexture loadImage(int slot, String path, float[] tcs) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty()) return null;
        assert slot > 0 : oxyAssert("Texture Slot already being used");
        return new ImageTexture(slot, path, tcs);
    }

    public static CubemapTexture loadCubemap(int slot, String path, Scene scene) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        if (path.isEmpty()) return null;
        return new CubemapTexture(slot, path, scene);
    }

    public static HDRTexture loadHDRTexture(String path, Scene scene) {
        return new HDRTexture(6, path, scene);
    }
}
