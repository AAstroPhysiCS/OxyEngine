package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.UILayer;
import imgui.ImGui;
import imgui.flag.ImGuiCond;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class StatsLayer extends UILayer {

    private static StatsLayer INSTANCE = null;

    public static StatsLayer getInstance(WindowHandle windowHandle, Scene scene){
        if(INSTANCE == null) INSTANCE = new StatsLayer(windowHandle, scene);
        return INSTANCE;
    }

    private StatsLayer(WindowHandle windowHandle, Scene scene) {
        super(windowHandle, scene);
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderLayer() {
        ImGui.setNextWindowSize(windowHandle.getWidth() / 4f, windowHandle.getHeight() / 4f, ImGuiCond.Once);
        ImGui.setNextWindowPos(windowHandle.getWidth() / 4f, windowHandle.getHeight() / 4f, ImGuiCond.Once);

        ImGui.begin("Stats");

        ImGui.textColored(normalizeColor(144), normalizeColor(103), normalizeColor(121), 1.0f, scene.getRenderer().info());
        ImGui.spacing();

        ImGui.end();
    }
}
