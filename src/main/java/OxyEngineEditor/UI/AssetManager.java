package OxyEngineEditor.UI;

import OxyEngine.Core.Renderer.Texture.Image2DTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Renderer.Texture.TextureSlot;

import java.io.File;
import java.util.*;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.removeFileExtension;

public class AssetManager {

    private final Map<String, Image2DTexture> assets = new HashMap<>();

    private static AssetManager INSTANCE = null;

    public static AssetManager getInstance() {
        if (INSTANCE == null) INSTANCE = new AssetManager();
        return INSTANCE;
    }

    static {
        AssetManager.getInstance().loadUIAssets();
    }

    private AssetManager() {

    }

    public void loadUIAssets() {
        File[] fList = new File("src/main/resources/assets").listFiles();
        for (File f : Objects.requireNonNull(fList)) {
            loadUIAsset(("UI " + removeFileExtension(f.getName())).toUpperCase(), f.getPath());
        }
    }

    public void loadUIAsset(String name, String path) {
        if (name.isEmpty() || name.isBlank()) {
            logger.warning("Asset loader failed because name is empty");
            return;
        }
        if (assets.containsKey(name)) {
            logger.severe("Texture exists in the buffer: " + name);
            return;
        }

        assets.put(name, OxyTexture.loadImage(TextureSlot.UITEXTURE, path));
    }

    public Image2DTexture getAsset(String imageName) {
        return assets.get(imageName);
    }
}
