package OxyEngineEditor.UI;

import OxyEngine.Core.Renderer.Texture.*;

import java.io.File;
import java.util.*;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.removeFileExtension;

public final class UIAssetManager {

    private final Map<String, Image2DTexture> uiAssets = new HashMap<>();

    public static final TextureParameterBuilder DEFAULT_TEXTURE_PARAMETER = TextureParameterBuilder.create()
            .setMinFilter(TextureParameter.LINEAR_MIPMAP_LINEAR)
            .setMagFilter(TextureParameter.LINEAR)
            .setWrapS(TextureParameter.REPEAT)
            .setWrapT(TextureParameter.REPEAT)
            .enableMipMap(true);

    private static UIAssetManager INSTANCE = null;

    public static UIAssetManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UIAssetManager();
            INSTANCE.loadUIAssets();
        }
        return INSTANCE;
    }

    private UIAssetManager() {

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
        if (uiAssets.containsKey(name)) {
            logger.severe("Texture exists in the buffer: " + name);
            return;
        }

        uiAssets.put(name, Texture.loadImage(TextureSlot.UITEXTURE, path, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER));
    }

    public Image2DTexture getUIAsset(String imageName) {
        return uiAssets.get(imageName);
    }
}
