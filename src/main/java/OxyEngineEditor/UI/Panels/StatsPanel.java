package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.OxyRenderer;
import imgui.ImGui;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class StatsPanel extends Panel {

    private static StatsPanel INSTANCE = null;

    public static StatsPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new StatsPanel();
        return INSTANCE;
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
