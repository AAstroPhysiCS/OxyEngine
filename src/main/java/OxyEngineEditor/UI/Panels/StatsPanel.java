package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.OxyRenderer;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static OxyEngine.Tools.Globals.normalizeColor;

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
        ImGui.begin("Stats", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoDocking);

        ImGui.textColored(normalizeColor(200), normalizeColor(200), normalizeColor(200), 1.0f, OxyRenderer.Stats.getStats());
        ImGui.spacing();

        ImGui.end();
    }
}
