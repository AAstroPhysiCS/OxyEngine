package OxyEngineEditor.UI.Panels;

import imgui.ImGui;

import static OxyEngine.Core.Context.Scene.SceneRuntime.ACTIVE_SCENE;

public final class SettingsPanel extends Panel {

    private static SettingsPanel INSTANCE = null;

    public static SettingsPanel getInstance(){
        if(INSTANCE == null) INSTANCE = new SettingsPanel();
        return INSTANCE;
    }

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {
        ImGui.begin("Settings");
        ImGui.columns(2, "env column");
        ImGui.setColumnOffset(0, -90f);
        ImGui.alignTextToFramePadding();
        ImGui.text("Gamma strength:");
        ImGui.alignTextToFramePadding();
        ImGui.text("Exposure: ");
        ImGui.nextColumn();
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
        ImGui.sliderFloat("###hidelabel g", ACTIVE_SCENE.gammaStrength, 0, 10);
        ImGui.sliderFloat("###hidelabel exposure", ACTIVE_SCENE.exposure, 0, 10);
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.end();
    }
}
