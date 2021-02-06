package OxyEngineEditor.UI.Panels;

import OxyEngine.Scene.SceneSerializer;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

import static OxyEngine.System.OxySystem.FileSystem.saveDialog;
import static OxyEngine.Scene.Scene.*;
import static OxyEngine.Scene.SceneSerializer.extensionName;
import static OxyEngine.Scene.SceneSerializer.fileExtension;

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
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("New Scene")) newScene();
                if (ImGui.menuItem("Open a scene", "Ctrl+O")) openScene();
                if (ImGui.menuItem("Save the scene", "Ctrl+S")) saveScene();
                if (ImGui.menuItem("Save As...")) {
                    String saveAs = saveDialog(extensionName, null);
                    if (saveAs != null) SceneSerializer.serializeScene(saveAs + fileExtension);
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
