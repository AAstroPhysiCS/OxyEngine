package OxyEngineEditor.UI.Panels;

import imgui.ImGui;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;

public final class SettingsPanel extends Panel {

    private static SettingsPanel INSTANCE = null;

    public static SettingsPanel getInstance(){
        if(INSTANCE == null) INSTANCE = new SettingsPanel();
        return INSTANCE;
    }

    @Override
    public void preload() {

    }

    private static final float[] gammaStrengthBuffer = new float[1];
    private static final float[] exposureBuffer = new float[1];

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
        gammaStrengthBuffer[0] = ACTIVE_SCENE.gammaStrength;
        exposureBuffer[0] = ACTIVE_SCENE.exposure;
        ImGui.sliderFloat("###hidelabel g", gammaStrengthBuffer, 0, 10);
        ImGui.sliderFloat("###hidelabel exposure", exposureBuffer, 0, 10);
        ACTIVE_SCENE.exposure = exposureBuffer[0];
        ACTIVE_SCENE.gammaStrength = gammaStrengthBuffer[0];
        ImGui.popItemWidth();
        ImGui.columns(1);
        ImGui.end();
    }
}
