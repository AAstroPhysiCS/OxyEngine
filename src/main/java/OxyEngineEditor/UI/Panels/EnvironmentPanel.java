package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import imgui.ImGui;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;

public class EnvironmentPanel extends Panel {

    public static float[] gammaStrength = new float[]{2.2f};
    public static float[] mipLevelStrength = new float[]{0.0f};
    public static float[] exposure = new float[]{1.0f};

    private static EnvironmentPanel INSTANCE = null;

    public static EnvironmentPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new EnvironmentPanel();
        return INSTANCE;
    }

    private static boolean initPanel = false;

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {
        ImGui.begin("Environment");

        if (ImGui.button("Load environment map")) {
            String path = openDialog("hdr", null);
            if (path != null) SceneLayer.getInstance().loadHDRTextureToScene(path);
        }

        ImGui.columns(2, "env column");
        if (!initPanel) ImGui.setColumnOffset(0, -90f);
        ImGui.alignTextToFramePadding();
        ImGui.text("Gamma strength:");
        ImGui.alignTextToFramePadding();
        ImGui.text("Environment LOD:");
        ImGui.alignTextToFramePadding();
        ImGui.text("Exposure: ");
        ImGui.nextColumn();
        ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth());
        ImGui.sliderFloat("###hidelabel g", gammaStrength, 0, 10);
        ImGui.sliderFloat("###hidelabel lod", mipLevelStrength, 0, 5);
        ImGui.sliderFloat("###hidelabel exposure", exposure, 0, 10);
        ImGui.popItemWidth();
        ImGui.columns(1);
        initPanel = true;

        ImGui.end();
    }
}
