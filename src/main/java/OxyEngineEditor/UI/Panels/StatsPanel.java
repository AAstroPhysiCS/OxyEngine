package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Context.OxyRenderer;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

public class StatsPanel extends Panel {

    private static StatsPanel INSTANCE = null;

    public static StatsPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new StatsPanel();
        return INSTANCE;
    }

    private static final ImBoolean open = new ImBoolean(false);

    @Override
    public void preload() {
    }

    @Override
    public void renderPanel() {
        ImGui.begin("Stats", ImGuiWindowFlags.NoBackground);

        ImGui.textColored(200/255f, 200/255f, 200/255f, 1.0f, OxyRenderer.Stats.getStats());
        ImGui.spacing();

        ImGui.checkbox("Demo", open);

        if(open.get()) ImGui.showDemoWindow();

        ImGui.end();
    }
}
