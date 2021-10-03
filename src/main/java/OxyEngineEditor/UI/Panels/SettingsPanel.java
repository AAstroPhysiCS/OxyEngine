package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Renderer;
import imgui.ImGui;

import static OxyEngine.Core.Scene.SceneRuntime.sceneContext;

public final class SettingsPanel extends Panel {

    private static SettingsPanel INSTANCE = null;

    public static SettingsPanel getInstance(){
        if(INSTANCE == null) INSTANCE = new SettingsPanel();
        return INSTANCE;
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
        ImGui.alignTextToFramePadding();
        ImGui.text("Show Bounding Boxes: ");
        ImGui.nextColumn();
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
        ImGui.sliderFloat("###hidelabel g", sceneContext.gammaStrength, 0, 10);
        ImGui.sliderFloat("###hidelabel exposure", sceneContext.exposure, 0, 10);
        if(ImGui.radioButton("###hidelabel aabb", Renderer.showBoundingBoxes))
            Renderer.showBoundingBoxes = !Renderer.showBoundingBoxes;
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.end();
    }
}
