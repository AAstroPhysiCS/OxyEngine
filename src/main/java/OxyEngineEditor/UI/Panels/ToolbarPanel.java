package OxyEngineEditor.UI.Panels;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiStyleVar;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;

public class ToolbarPanel extends Panel {

    private static ToolbarPanel INSTANCE = null;

    public static ToolbarPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ToolbarPanel();
        return INSTANCE;
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderPanel() {
        ImGui.pushStyleColor(ImGuiCol.MenuBarBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 10);

        if (ImGui.beginMainMenuBar()) {
            ImVec2 pos = new ImVec2();
            ImGui.getWindowPos(pos);
            ImGui.setCursorPosY(ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable) ? pos.y - 20f : pos.y + 3);
            if (ImGui.beginMenu("File")) {
                if (ImGui.beginMenu("New")) {
                    ImGui.menuItem("New Scene");
                    ImGui.endMenu();
                }
                if (ImGui.menuItem("Open a scene", "Ctrl+O")) {
                    String openScene = openDialog("", null);
                }
                if (ImGui.menuItem("Save the scene", "Ctrl+S")) {
                }
                if (ImGui.menuItem("Save As...")) {
                    String saveAs = openDialog("", null);
                }
                ImGui.endMenu();
            }
            ImGui.spacing();
            if (ImGui.beginMenu("Edit")) {
                if (ImGui.menuItem("Back", "Ctrl+Z")) {
                }
                if (ImGui.menuItem("Forward", "Ctrl+Y")) {
                }
                ImGui.endMenu();
            }
            ImGui.spacing();
            ImGui.endMainMenuBar();
        }

        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }
}
