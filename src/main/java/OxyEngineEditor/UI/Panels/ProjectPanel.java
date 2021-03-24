package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.TextureSlot;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.*;
import imgui.type.ImString;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static OxyEngine.System.OxyEventSystem.mouseButtonDispatcher;
import static OxyEngine.System.OxySystem.BASE_PATH;
import static OxyEngine.System.OxySystem.removeFileExtension;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;

public class ProjectPanel extends Panel {

    private static ProjectPanel INSTANCE = null;

    public static ProjectPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ProjectPanel();
        return INSTANCE;
    }

    private static final String rootPath = (BASE_PATH + "\\src\\main\\resources");
    private static final ImString currentPathImString = new ImString(1000);
    private static final File[] rootFiles = new File(rootPath).listFiles();
    private static File currentFile = null;
    public static File lastDragDropFile = null;

    static {
        currentPathImString.set(rootPath);
    }

    private static final ImString searchImString = new ImString(1000);

    private static ImageTexture fileAsset;
    public static ImageTexture dirAsset;
    public static ImageTexture dirAssetGrey;

    private ProjectPanel() {

    }

    @Override
    public void preload() {
        fileAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/fileAsset.png");
        dirAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/dirAsset.png");
        dirAssetGrey = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/dirAsset-grey.png");
    }

    @Override
    public void renderPanel() {
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], Panel.bgC[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], Panel.bgC[3]);
        ImGui.pushStyleColor(ImGuiCol.Button, Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], Panel.bgC[3]);
        ImGui.begin("Project", ImGuiWindowFlags.NoTitleBar);
        ImGui.popStyleColor(3);

        float getWidth = ImGui.getWindowWidth();

        ImGui.setCursorPos(5, 5);
        if (ImGui.beginTabBar("ProjectTabBar")) {
            if (ImGui.beginTabItem("Project")) {
                ImGui.setCursorPos(ImGui.getCursorPosX() + 5, ImGui.getCursorPosY() + 5);
                if (ImGui.beginChild("ProjectChild")) {

                    ImGui.button("<", 30, 20);
                    ImGui.sameLine();
                    ImGui.button(">", 30, 20);
                    ImGui.sameLine();

                    ImGui.sameLine(100);
                    ImGui.pushItemWidth(getWidth / 1.4f);
                    ImGui.inputTextWithHint("##hideLabelProjectPath", "Path...", currentPathImString);
                    ImGui.popItemWidth();

                    ImGui.sameLine(getWidth / 1.4f + 125);
                    ImGui.pushItemWidth(getWidth / 7);
                    ImGui.inputTextWithHint("##hideLabelProjectSearch", "Search...", searchImString);
                    ImGui.popItemWidth();

                    renderFolderStructureChild();
                    ImGui.sameLine();
                    renderFolderContentChild();

                    ImGui.endChild();
                }
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Console")) {

                if (ImGui.beginChild("ConsoleChild")) {

                    ImGui.endChild();
                }

                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
        ImGui.end();
    }

    private void renderFolderStructureChild() {
        ImGui.spacing();
        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);
        ImGui.beginChild("StructureChild", 400, ImGui.getContentRegionAvailY() - 10);

        if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && !ImGui.isAnyItemHovered() && ImGui.isWindowHovered()) {
            currentFile = null;
            currentPathImString.set(rootPath);
        }

        renderFolderRecursively(rootFiles, 48);

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void renderFolderRecursively(File[] parentFiles, int offsetX) {
        if (parentFiles.length == 0) return;
        for (File f : parentFiles) {
            if (f.isDirectory()) {

                String name = f.getName();
                name = renderImageBesideTreeNode(name, dirAssetGrey.getTextureId(), 20, 2, 20, 20);
                ImGui.setCursorPosX(ImGui.getCursorPosX() - offsetX);

                if (ImGui.treeNodeEx(name, ImGuiTreeNodeFlags.OpenOnArrow)) {
                    if (ImGui.isItemHovered(ImGuiMouseButton.Left) && mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_1]) {
                        currentFile = f;
                        currentPathImString.set(currentFile.getPath());
                        searchImString.set("");
                    }
                    renderFolderRecursively(Objects.requireNonNull(f.listFiles()), 0);
                    ImGui.dummy(0, 0);
                    ImGui.setCursorPosY(ImGui.getCursorPosY() - 4); //because dummy takes a lill space
                    ImGui.treePop();
                }
                if (ImGui.isItemHovered(ImGuiMouseButton.Left) && mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_1]) {
                    currentFile = f;
                    currentPathImString.set(currentFile.getPath());
                    searchImString.set("");
                }
            }
        }
    }

    private static final int imageWidth = 60, imageHeight = 75;

    private static final record OxyImageButton(String name, float cursorPosX, float cursorPosY) {
    }

    private void renderFolderContentChild() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);
        final float childWidth = ImGui.getContentRegionAvailX() - 20;
        final float childHeight = ImGui.getContentRegionAvailY() - 10;
        ImGui.beginChild("ContentChild", childWidth, childHeight);
        float windowVisible = ImGui.getWindowPosX() + childWidth;

        if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && !ImGui.isAnyItemHovered() && ImGui.isWindowHovered()) {
            currentFile = null;
            currentPathImString.set(rootPath);
        }

        boolean searchBeingUsed = !searchImString.isEmpty();
        if (currentFile != null) {

            File[] listedCurrentFile = Objects.requireNonNull(currentFile.listFiles());

            if (!searchBeingUsed) {

                File[] sortedToDir = Arrays.stream(listedCurrentFile).sorted((o1, o2) -> {
                    if (o1.isDirectory()) return -1;
                    if (o2.isDirectory()) return 1;
                    return 0;
                }).collect(Collectors.toList()).toArray(File[]::new);

                //Regular folder render because search is not being used
                renderFolderImageButtons(sortedToDir, windowVisible);
            }
        }

        if (searchBeingUsed) {
            searchForAFileRecursively(rootFiles);
            File[] searchFiles = fList.toArray(File[]::new);
            renderFolderImageButtons(searchFiles, windowVisible);
            fList.clear();
        }

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private static final List<File> fList = new ArrayList<>();

    private void searchForAFileRecursively(File[] filesToSearch) {
        for (File f : filesToSearch) {
            if (removeFileExtension(f.getName()).contains(searchImString.get())) {
                fList.add(f);
            }
            File[] listFiles = f.listFiles();
            if (listFiles != null) searchForAFileRecursively(listFiles);
        }
    }

    private void renderFolderImageButtons(File[] dir, float windowVisible) {
        List<OxyImageButton> imageButtonList = new ArrayList<>();

        final float imageOffsetFromWindowX = 10;
        final float imageOffsetFromWindowY = 5;

        ImGui.setCursorPosX(ImGui.getCursorPosX() + imageOffsetFromWindowX);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + imageOffsetFromWindowY);
        for (File f : dir) {
            OxyImageButton button = new OxyImageButton(f.getName(), ImGui.getCursorPosX(), ImGui.getCursorPosY() + imageHeight + imageOffsetFromWindowY);
            imageButtonList.add(button);
            if (f.isDirectory()) {
                ImGui.imageButton(dirAsset.getTextureId(), imageWidth, imageHeight, 0, 1, 1, 0);
                if(ImGui.isItemClicked()) {
                    currentFile = f;
                    currentPathImString.set(currentFile.getPath());
                    searchImString.set("");
                }
            } else {
                ImGui.imageButton(fileAsset.getTextureId(), imageWidth, imageHeight, 0, 1, 1, 0);
                if(ImGui.isItemClicked()) lastDragDropFile = f;
                if(lastDragDropFile != null) {
                    if (ImGui.beginDragDropSource()) {
                        ImGui.setDragDropPayloadObject("projectPanelFile", lastDragDropFile);
                        ImGui.endDragDropSource();
                    }
                }
            }

            float lastButton = ImGui.getItemRectMaxX();
            float nextButton = lastButton + ImGui.getStyle().getItemInnerSpacingX() + imageWidth;
            if (nextButton < windowVisible) {
                ImGui.sameLine();
            } else {
                ImGui.dummy(0, 20); //spacing
                ImGui.setCursorPosX(ImGui.getCursorPosX() + imageOffsetFromWindowX);
            }
        }

        for (OxyImageButton button : imageButtonList) {

            String wrappedName = button.name;
            boolean isOnLimit = false;
            if (wrappedName.length() > 8) {
                isOnLimit = true;
                wrappedName = button.name.substring(0, 8) + "...";
            }

            ImVec2 textSize = new ImVec2();
            ImGui.calcTextSize(textSize, wrappedName);

            ImGui.setCursorPosX(button.cursorPosX);
            ImGui.setCursorPosY(button.cursorPosY);
            ImGui.text(wrappedName);
            if (ImGui.isItemHovered() && isOnLimit) {
                if (ImGui.beginPopup("TextPopupRenderFolderChildren")) {
                    ImGui.text(button.name);
                    ImGui.endPopup();
                }
            }
        }
    }
}
