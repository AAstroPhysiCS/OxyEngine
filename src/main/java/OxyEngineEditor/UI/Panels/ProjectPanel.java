package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxySystem;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;

import java.io.File;

public class ProjectPanel extends Panel {

    private static ProjectPanel INSTANCE = null;

    public static ProjectPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ProjectPanel();
        return INSTANCE;
    }

    private static File[] allCurrentProjectFiles = OxySystem.getCurrentProjectFiles(true);

    private static ImageTexture fileAsset;
    private static ImageTexture dirAsset;

    private static boolean init = false;

    private ProjectPanel() {

    }

    @Override
    public void preload() {
        fileAsset = OxyTexture.loadImage("src/main/resources/assets/fileAsset.png");
        dirAsset = OxyTexture.loadImage("src/main/resources/assets/dirAsset.png");
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
        ImGui.separator();
        ImGui.spacing();

        ImGui.columns(2, "ProjectPanelCol");
        if (!init) {
            ImGui.setColumnOffset(0, (-ImGui.getWindowWidth() / 2f) + 400f);
            init = true;
        }
        // 0 is automatic, i guess
        ImGui.beginChild("FilePanel", 0, 0, false);
        allFileListing(allCurrentProjectFiles);
        ImGui.endChild();
        ImGui.nextColumn();
        ImGui.beginChild("InsideFilePanel", 0, 0, false);
        previewFiles();
        ImGui.endChild();
        ImGui.columns(1);
        ImGui.end();
    }

    private static File lastDir;
    private static String nameOfPreviewFile;

    private void allFileListing(File[] files) {
        if (files == null) return;
        for (File f : files) {
            File[] underFiles = f.listFiles();
            if (underFiles != null) {
                if(nameOfPreviewFile != null) {
                    if (nameOfPreviewFile.equals(f.getName())) {
                        ImGui.setNextItemOpen(true);
                        nameOfPreviewFile = null;
                    }
                }
                if (ImGui.treeNodeEx(f.getName())) {
                    lastDir = f;
                    allFileListing(underFiles);
                    ImGui.treePop();
                }
            } else {
                ImGui.selectable(f.getName(), true);
            }
        }
    }

    private static final float imageButtonWidth = 75f;
    private static final ImVec2 pos = new ImVec2(), regionMax = new ImVec2(), cursorPos = new ImVec2();

    private void previewFiles() {
        if (lastDir == null) return;
        ImGui.getWindowPos(pos);
        ImGui.getWindowContentRegionMax(regionMax);
        float windowVisible = pos.x + regionMax.x;
        ImGui.getCursorPos(cursorPos);
        File f = lastDir;
        ImGuiStyle style = ImGui.getStyle();
        if (f.isDirectory()) {
            File[] underFiles = f.listFiles();
            if (underFiles == null) return;
            for (int i = 0; i < underFiles.length; i++) {
                File fUnderFiles = underFiles[i];
                ImGui.pushID(i);
                ImGui.pushStyleColor(ImGuiCol.Button, 1.0f, 1.0f, 1.0f, 0.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.1f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1.0f, 1.0f, 1.0f, 0.3f);
                if (fUnderFiles.isDirectory()) {
                    if (ImGui.imageButton(dirAsset.getTextureId(), imageButtonWidth, 75f, 0, 1, 1, 0, 2)) {
                        nameOfPreviewFile = fUnderFiles.getName();
                    }
                } else {
                    if (ImGui.imageButton(fileAsset.getTextureId(), imageButtonWidth, 75f, 0, 1, 1, 0, 2)) {
                    }
                }
                ImGui.popStyleColor(3);
                float lastButton = ImGui.getItemRectMaxX();
                float nextButton = lastButton + style.getItemSpacingX() + imageButtonWidth;
                if (i + 1 < underFiles.length && nextButton < windowVisible) ImGui.sameLine();
                else ImGui.dummy(0, 28);
                ImGui.popID();
            }
            int yOff = 0, xOff = 0;
            for (File fUnderFiles : underFiles) {
                float lastButton = ImGui.getItemRectMaxX();
                float nextButton = lastButton + style.getItemSpacingX() + imageButtonWidth + 15;
                if (nextButton > windowVisible) {
                    yOff++;
                    xOff = 1;
                } else xOff++;
                ImGui.setCursorPos((imageButtonWidth * (xOff - 1)) + (imageButtonWidth * (xOff - 1) / 5.7f), (yOff * 115) + 85f);
                ImGui.text(fUnderFiles.getName().length() >= 8 ? fUnderFiles.getName().substring(0, 8) + "..." : fUnderFiles.getName());
            }
        }
    }
}
