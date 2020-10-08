package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import imgui.ImGui;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;

public class EnvironmentPanel extends Panel {

    public static final float[] gammaStrength = new float[]{2f};
    public static final float[] mipLevelStrength = new float[]{1.0f};
    public static final float[] exposure = new float[]{1.0f};

    private static EnvironmentPanel INSTANCE = null;

    public static EnvironmentPanel getInstance(SceneLayer scene) {
        if (INSTANCE == null) INSTANCE = new EnvironmentPanel(scene);
        return INSTANCE;
    }

    private final SceneLayer sceneLayer;

    private EnvironmentPanel(SceneLayer sceneLayer) {
        this.sceneLayer = sceneLayer;
    }

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {
        ImGui.begin("Environment");

        if (ImGui.button("Load environment map")) {
            String path = openDialog("hdr", null);
            if (path != null) {
                if (SceneLayer.hdrTexture != null) SceneLayer.hdrTexture.dispose();
                SceneLayer.hdrTexture = OxyTexture.loadHDRTexture(path, sceneLayer.getScene());
                SceneLayer.hdrTexture.captureFaces(0);
            }
        }

        ImGui.alignTextToFramePadding();
        ImGui.text("Gamma strength:");
        ImGui.sameLine();
        ImGui.sliderFloat("###hidelabel g", gammaStrength, 0, 10);

        ImGui.alignTextToFramePadding();
        ImGui.text("Environment LOD:");
        ImGui.sameLine();
        ImGui.sliderFloat("###hidelabel lod", mipLevelStrength, 0, 5);

        ImGui.alignTextToFramePadding();
        ImGui.text("Exposure: ");
        ImGui.sameLine();
        ImGui.sliderFloat("###hidelabel exposure", exposure, 0, 10);

        ImGui.end();
    }
}
