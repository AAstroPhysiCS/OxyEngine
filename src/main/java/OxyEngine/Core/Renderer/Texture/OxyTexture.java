package OxyEngine.Core.Renderer.Texture;

import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class OxyTexture implements OxyDisposable, EntityComponent {

    private static final List<OxyTexture> allTextures = new ArrayList<>();

    private static int slotCounter = 0;

    private final float[] tcs;

    private final int textureSlot;
    private final int textureId;

    private final String path;

    private OxyTexture(int slot, String path, float[] tcs) {
        this.tcs = tcs;
        this.path = path;
        this.textureSlot = slot;

        if (slot == 0) throw new IllegalArgumentException("Slot can not be 0");

        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];
        ByteBuffer buffer = stbi_load(path, width, height, channels, 0);
        if (buffer == null) {
            logger.severe("Texture could not be loaded!");
            throw new InternalError("Texture could not be loaded");
        }

        int internalFormat = GL_RGBA;
        if (channels[0] == 3)
            internalFormat = GL_RGB;

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTextureParameteri(textureId, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTextureParameteri(textureId, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTextureParameteri(textureId, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width[0], height[0], 0, internalFormat, GL_UNSIGNED_BYTE, buffer);
        stbi_image_free(buffer);

        glBindTexture(GL_TEXTURE_2D, 0);

        allTextures.add(this);
    }

    public static OxyTexture load(int slot, String path) {
        if(slot <= slotCounter) throw new IllegalStateException("Texture Slot already being used");
        return new OxyTexture(slot, path, null);
    }

    public static OxyTexture load(String path) {
        slotCounter++;
        return new OxyTexture(slotCounter, path, null);
    }

    public static OxyTexture load(String path, float[] tcs) {
        slotCounter++;
        return new OxyTexture(slotCounter, path, tcs);
    }

    public static OxyTexture load(int slot, String path, float[] tcs) {
        if(slot <= slotCounter) throw new IllegalStateException("Texture Slot already being used");
        return new OxyTexture(slot, path, tcs);
    }

    public static OxyTexture loadCached(int slot) {
        for (OxyTexture t : allTextures) {
            if (t.getTextureSlot() == slot) {
                return t;
            }
        }
        return null;
    }

    public static void bindAllTextureSlots() {
        for (OxyTexture t : allTextures) glBindTextureUnit(t.getTextureSlot(), t.getTextureId());
    }

    public static void unbindAllTextureSlots() {
        for (int i = 0; i < 32; i++) glBindTextureUnit(i, 0);
    }

    public float[] getTextureCoords() {
        return tcs;
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

    @Override
    public void dispose() {
        glDeleteTextures(textureId);
    }
}
