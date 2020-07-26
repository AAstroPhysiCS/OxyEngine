package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.UI.UILayer;
import imgui.ImGui;
import imgui.flag.ImGuiCond;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class StatsLayer extends UILayer {

    private static StatsLayer INSTANCE = null;

    public static StatsLayer getInstance(WindowHandle windowHandle, OxyRenderer renderer){
        if(INSTANCE == null) INSTANCE = new StatsLayer(windowHandle, renderer);
        return INSTANCE;
    }

    private StatsLayer(WindowHandle windowHandle, OxyRenderer currentRenderer) {
        super(windowHandle, currentRenderer);
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderLayer() {
        ImGui.setNextWindowSize(windowHandle.getWidth() / 4f, windowHandle.getHeight() / 4f, ImGuiCond.Once);
        ImGui.setNextWindowPos(windowHandle.getWidth() / 4f, windowHandle.getHeight() / 4f, ImGuiCond.Once);

        ImGui.begin("Stats");

        ImGui.textColored(normalizeColor(144), normalizeColor(103), normalizeColor(121), 1.0f, currentRenderer.info());
        ImGui.spacing();

        ImGui.end();
    }
}
