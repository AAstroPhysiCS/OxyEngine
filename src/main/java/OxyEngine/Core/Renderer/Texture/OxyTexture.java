package OxyEngine.Core.Renderer.Texture;

import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Scene.Scene;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.stb.STBImage.stbi_load;

public class OxyTexture {

    static final List<AbstractTexture> allTextures = new ArrayList<>();
    public static final int[] slotCounter = new int[32];
    static {
        Arrays.fill(slotCounter, 0);
    }

    private OxyTexture() {
    }

    static abstract class AbstractTexture implements OxyDisposable {

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
                logger.warning("Texture: " + path + " could not be loaded! Gonna give some default color");
            return buffer;
        }

        @Override
        public void dispose() {
            glDeleteTextures(textureId);
            allTextures.remove(this);
            if(textureSlot != -1) slotCounter[textureSlot - 1] = 0;
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
        assert slotCounter[slot] != 0 : oxyAssert("Texture Slot already being used");
        return new ImageTexture(slot, path, null);
    }

    public static ImageTexture loadImage(String path) {
        if(path == null){
            logger.warning("Path is null");
            return null;
        }
        if(path.equals("null")){
            logger.warning("Path is null");
            return null;
        }
        return new ImageTexture(getLatestSlot(), path, null);
    }

    public static ImageTexture loadImage(String path, float[] tcs) {
        if(path == null){
            logger.warning("Path is null");
            return null;
        }
        if(path.equals("null")){
            logger.warning("Path is null");
            return null;
        }
        return new ImageTexture(getLatestSlot(), path, tcs);
    }

    public static ImageTexture loadImage(int slot, String path, float[] tcs) {
        assert slotCounter[slot] != 0 : oxyAssert("Texture Slot already being used");
        return new ImageTexture(slot, path, tcs);
    }

    public static CubemapTexture loadCubemap(String path, Scene scene) {
        if(path == null){
            logger.warning("Path is null");
            return null;
        }
        if(path.equals("null")){
            logger.warning("Path is null");
            return null;
        }
        return new CubemapTexture(getLatestSlot(), path, scene);
    }

    public static HDRTexture loadHDRTexture(String path, Scene scene) {
        HDRTexture hdrTexture = new HDRTexture(getLatestSlot(), path, scene);
        HDRTexture.IrradianceTexture irradianceTexture = new HDRTexture.IrradianceTexture(getLatestSlot(), path, hdrTexture);
        HDRTexture.PrefilterTexture prefilterTexture = new HDRTexture.PrefilterTexture(getLatestSlot(), path, hdrTexture);
        HDRTexture.BDRF bdrfTexture = new HDRTexture.BDRF(getLatestSlot(), path, hdrTexture);
        hdrTexture.setIrradianceTexture(irradianceTexture);
        hdrTexture.setPrefilterTexture(prefilterTexture);
        hdrTexture.setBdrf(bdrfTexture);
        return hdrTexture;
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
        for (AbstractTexture t : allTextures) glBindTextureUnit(t.getTextureSlot(), t.getTextureId());
    }

    public static void unbindAllTextureSlots() {
        for (int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }

    private static int getLatestSlot(){
        for(int i = 0; i < slotCounter.length - 1; i++){
            if(slotCounter[i] == 0){
                slotCounter[i] = 1;
                return i + 1;
            }
        }
        return -10;
    }
}
