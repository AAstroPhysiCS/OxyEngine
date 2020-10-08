package OxyEngineEditor.UI.Panels;

import OxyEngine.System.OxySystem;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.flag.ImGuiColorEditFlags;

import java.io.File;

public class ProjectPanel extends Panel {

    private static ProjectPanel INSTANCE = null;

    public static ProjectPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ProjectPanel();
        return INSTANCE;
    }

    private static File[] allCurrentProjectFiles = OxySystem.getCurrentProjectFiles(true);

    private ProjectPanel() {

    }

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {
        ImGui.begin("Project");

        if (ImGui.button("Create")) {

        }
        ImGui.sameLine(ImGui.getWindowWidth() - 100);
        if (ImGui.button("Reload")) {
            allCurrentProjectFiles = OxySystem.getCurrentProjectFiles(true);
        }

        ImGui.columns(2, "ProjectPanelCol");
        // 0 is automatic, i guess
        ImGui.beginChild("FilePanel", 0, 0, true);
        allFileListing(allCurrentProjectFiles);
        ImGui.endChild();
        ImGui.nextColumn();
        ImGui.beginChild("InsideFilePanel", 0, 0, true);
        previewFiles();
        ImGui.endChild();
        ImGui.columns(1);

        ImGui.end();
    }

    private static String lastPath;

    private void allFileListing(File[] files) {
        for (File f : files) {
            File[] underFiles = f.listFiles();
            if (underFiles != null) {
                if (ImGui.treeNode(f.getName())) {
                    lastPath = f.getPath();
                    allFileListing(underFiles);
                    ImGui.treePop();
                }
            } else {
                ImGui.selectable(f.getName(), true);
            }
        }
    }

    private static final float imageButtonWidth = 75f;
    private static final ImVec2 pos = new ImVec2(), regionMax = new ImVec2();

    private void previewFiles() {
        if (lastPath == null) return;
        ImGui.getWindowPos(pos);
        ImGui.getWindowContentRegionMax(regionMax);
        float windowVisible = pos.x + regionMax.x;

        File f = new File(lastPath);
        ImGuiStyle style = ImGui.getStyle();
        if (f.isDirectory()) {
            File[] underFiles = f.listFiles();
            if (underFiles == null) return;
            for (int i = 0; i < underFiles.length; i++) {
                File fUnderFiles = underFiles[i];
                ImGui.pushID(i);
                if (fUnderFiles.isDirectory()) {
                    ImGui.colorButton("preview" + fUnderFiles.hashCode(), new float[]{1.0f, 0.0f, 0.0f, 0.0f}, ImGuiColorEditFlags.None, imageButtonWidth, 50f);
//                    ImGui.sameLine();
//                    ImGui.text(fUnderFiles.getName());
                } else {
                    ImGui.colorButton("preview" + fUnderFiles.hashCode(), new float[]{1.0f, 1.0f, 1.0f, 1.0f}, ImGuiColorEditFlags.None, imageButtonWidth, 50f);
//                    ImGui.sameLine();
//                    ImGui.text(fUnderFiles.getName().split("\\.")[0]);
                }
                float lastButton = ImGui.getItemRectMaxX();
                float nextButton = lastButton + style.getItemSpacingX() + imageButtonWidth;
                if (i + 1 < underFiles.length && nextButton < windowVisible) ImGui.sameLine();
                ImGui.popID();
            }
        }
    }
}
