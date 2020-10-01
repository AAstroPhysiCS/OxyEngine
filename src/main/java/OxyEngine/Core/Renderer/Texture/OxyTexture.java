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

    static final List<Texture> allTextures = new ArrayList<>();
    private static int slotCounter = 0;

    private OxyTexture() {
    }

    static abstract class Texture implements OxyDisposable {

        protected int textureId;
        protected final int textureSlot;
        protected final String path;

        public Texture(int slot, String path) {
            this.path = path;
            this.textureSlot = slot;
        }

        protected ByteBuffer loadTextureFile(String path, int[] width, int[] height, int[] channels) {
            ByteBuffer buffer = stbi_load(path, width, height, channels, 0);
            if (buffer == null)
                logger.warning("Texture: " + path + " could not be loaded! Gonna give some default color");
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
        assert slot <= slotCounter : oxyAssert("Texture Slot already being used");
        return new ImageTexture(slot, path, null);
    }

    public static ImageTexture loadImage(String path) {
        return new ImageTexture(++slotCounter, path, null);
    }

    public static ImageTexture loadImage(String path, float[] tcs) {
        return new ImageTexture(++slotCounter, path, tcs);
    }

    public static ImageTexture loadImage(int slot, String path, float[] tcs) {
        assert slot <= slotCounter : oxyAssert("Texture Slot already being used");
        return new ImageTexture(slot, path, tcs);
    }

    public static CubemapTexture loadCubemap(String path, Scene scene) {
        return new CubemapTexture(++slotCounter, path, scene);
    }

    public static HDRTexture loadHDRTexture(String path, Scene scene) {
        HDRTexture hdrTexture = new HDRTexture(++slotCounter, path, scene);
        HDRTexture.IrradianceTexture irradianceTexture = new HDRTexture.IrradianceTexture(++slotCounter, path, hdrTexture);
        HDRTexture.PrefilterTexture prefilterTexture = new HDRTexture.PrefilterTexture(++slotCounter, path, hdrTexture);
        HDRTexture.BDRF bdrfTexture = new HDRTexture.BDRF(++slotCounter, path, hdrTexture);
        hdrTexture.setIrradianceTexture(irradianceTexture);
        hdrTexture.setPrefilterTexture(prefilterTexture);
        hdrTexture.setBdrf(bdrfTexture);
        return hdrTexture;
    }

    public static Texture loadImageCached(int slot) {
        for (Texture t : allTextures) {
            if (t.getTextureSlot() == slot) {
                return t;
            }
        }
        return null;
    }

    public static void bindAllTextureSlots() {
        for (Texture t : allTextures) glBindTextureUnit(t.getTextureSlot(), t.getTextureId());
    }

    public static void unbindAllTextureSlots() {
        for (int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }
}
