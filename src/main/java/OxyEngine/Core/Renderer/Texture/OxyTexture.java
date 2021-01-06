package OxyEngine.Core.Renderer.Texture;

import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Scene.Scene;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.stb.STBImage.stbi_load;

public class OxyTexture {

    static final List<AbstractTexture> allTextures = new ArrayList<>();

    private OxyTexture() {
    }

    public static void unbindAllTextures(){
        for(int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }

    public static abstract class AbstractTexture implements OxyDisposable {

        protected int textureId;
        protected final int textureSlot;
        protected final String path;

        public AbstractTexture(int slot, String path) {
            this.path = path;
            this.textureSlot = slot;
        }

        protected ByteBuffer loadTextureFile(String path, int[] width, int[] height, int[] channels) {
            ByteBuffer buffer = stbi_load(path, width, height, channels, 0);
            if (buffer == null)
                logger.warning("Texture: " + path + " could not be loaded!");
            return buffer;
        }

        @Override
        public void dispose() {
            glDeleteTextures(textureId);
            allTextures.remove(this);
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
        return new ImageTexture(slot, path, null);
    }

    public static ImageTexture loadImage(int slot, String path, float[] tcs) {
        if (path.equals("null")) return null;
        assert slot > 0 : oxyAssert("Texture Slot already being used");
        return new ImageTexture(slot, path, tcs);
    }

    public static CubemapTexture loadCubemap(int slot, String path, Scene scene) {
        if (path == null) return null;
        if (path.equals("null")) return null;
        return new CubemapTexture(slot, path, scene);
    }

    public static HDRTexture loadHDRTexture(String path, Scene scene) {
        return new HDRTexture(6, path, scene);
    }

    public static AbstractTexture loadImageCached(int slot) {
        for (AbstractTexture t : allTextures) {
            if (t.getTextureSlot() == slot) {
                return t;
            }
        }
        return null;
    }

    public static void bindAllTextureSlots() {
        for (AbstractTexture t : allTextures){
            if(t.getTextureSlot() >= 0) glBindTextureUnit(t.getTextureSlot(), t.getTextureId());
        }
    }

    public static void unbindAllTextureSlots() {
        for (int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }
}
