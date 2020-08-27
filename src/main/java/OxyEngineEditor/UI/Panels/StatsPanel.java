package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Window.WindowHandle;
import imgui.ImGui;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class StatsPanel extends Panel {

    private static StatsPanel INSTANCE = null;

    public static StatsPanel getInstance(WindowHandle windowHandle) {
        if (INSTANCE == null) INSTANCE = new StatsPanel(windowHandle);
        return INSTANCE;
    }

    private StatsPanel(WindowHandle windowHandle) {
        super(windowHandle);
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderPanel() {
        ImGui.begin("Stats");

        ImGui.textColored(normalizeColor(144), normalizeColor(103), normalizeColor(121), 1.0f, OxyRenderer.Stats.getStats());
        ImGui.spacing();

        ImGui.end();
    }
}
