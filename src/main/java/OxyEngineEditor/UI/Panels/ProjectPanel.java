package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxyFontSystem;
import OxyEngine.System.OxyLogger;
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
import static OxyEngine.System.OxySystem.*;
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

    private static final float buttonColorOffsetHovered = 0.09f, buttonColorOffsetActive = 0.15f;
    private static ImageTexture objAsset, fbxAsset, gltfAsset;
    private static ImageTexture fileAsset, pngAsset, jpgAsset, blendAsset;
    public static ImageTexture dirAsset;
    public static ImageTexture dirAssetGrey;

    private ProjectPanel() {

    }

    @Override
    public void preload() {
        fileAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/fileAsset2.png");
        objAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/objFileAsset.png");
        fbxAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/fbxFileAsset.png");
        gltfAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/gltfFileAsset.png");
        dirAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/dirAsset.png");
        dirAssetGrey = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/dirAsset-grey.png");
        pngAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/pngFileAsset.png");
        jpgAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/jpgFileAsset.png");
        blendAsset = OxyTexture.loadImage(TextureSlot.UITEXTURE, "src/main/resources/assets/blendFileAsset.png");
    }

    @Override
    public void renderPanel() {
        //For the top left triangle button (bcs you cant disable that button in imgui-java yet)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], Panel.bgC[3]);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], Panel.bgC[3]);
        ImGui.pushStyleColor(ImGuiCol.Button, Panel.bgC[0], Panel.bgC[1], Panel.bgC[2], Panel.bgC[3]);
        ImGui.begin("Project", ImGuiWindowFlags.NoTitleBar);
        ImGui.popStyleColor(3);

        float getWidth = ImGui.getWindowWidth();

        //For the buttons that are being used in the project browser panel
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, Panel.frameBgC[0] + buttonColorOffsetHovered, Panel.frameBgC[1] + buttonColorOffsetHovered,
                Panel.frameBgC[2] + buttonColorOffsetHovered, Panel.frameBgC[3] + buttonColorOffsetHovered);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, Panel.frameBgC[0] + buttonColorOffsetActive, Panel.frameBgC[1] + buttonColorOffsetActive,
                Panel.frameBgC[2] + buttonColorOffsetActive, Panel.frameBgC[3] + buttonColorOffsetActive);

        ImGui.pushStyleColor(ImGuiCol.Button, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);

        ImGui.setCursorPos(5, 5);
        if (ImGui.beginTabBar("ProjectTabBar")) {
            if (ImGui.beginTabItem("Project")) {
                ImGui.setCursorPos(ImGui.getCursorPosX() + 5, ImGui.getCursorPosY() + 5);
                if (ImGui.beginChild("ProjectChild")) {

                    ImGui.sameLine();

                    ImGui.sameLine(100);
                    ImGui.pushItemWidth(getWidth / 1.4f);
                    if (ImGui.inputTextWithHint("##hideLabelProjectPath", "Path...", currentPathImString, ImGuiInputTextFlags.EnterReturnsTrue))
                        currentFile = new File(currentPathImString.get());
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

                ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.bgC[0] - 0.05f, Panel.bgC[1] - 0.05f, Panel.bgC[2] - 0.05f, Panel.bgC[3] - 0.05f);
                if (ImGui.beginChild("ConsoleChild")) {
                    renderConsole();
                    ImGui.endChild();
                }
                ImGui.popStyleColor();

                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }

        ImGui.popStyleColor(3);
        ImGui.end();
    }

    private void renderConsole(){
        ImGui.pushFont(OxyFontSystem.getAllFonts().get(1));
        String[] splitted = OxyLogger.getHistory().toString().split("\n");
        for(String s : splitted){
            float[] colors = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
            if(s.contains(OxyLogger.ANSI_RED)){
                colors = OxyLogger.ANSI_RED_OXY.getNumbers();
                s = s.replace(OxyLogger.ANSI_RED, "");
            }
            else if(s.contains(OxyLogger.ANSI_BLUE)){
                colors = OxyLogger.ANSI_BLUE_OXY.getNumbers();
                s = s.replace(OxyLogger.ANSI_BLUE, "");
            }
            else if(s.contains(OxyLogger.ANSI_YELLOW)){
                colors = OxyLogger.ANSI_YELLOW_OXY.getNumbers();
                s = s.replace(OxyLogger.ANSI_YELLOW, "");
            }
            s = s.replace(OxyLogger.ANSI_RESET, "");
            ImGui.textColored(colors[0], colors[1], colors[2], colors[3], s);
        }
        ImGui.popFont();
    }

    private void renderFolderStructureChild() {
        ImGui.spacing();
        ImGui.pushStyleColor(ImGuiCol.ChildBg, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);
        ImGui.beginChild("StructureChild", 400, ImGui.getContentRegionAvailY() - 10);

        if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && !ImGui.isAnyItemHovered() && ImGui.isWindowHovered()) {
            currentFile = null;
            currentPathImString.set(rootPath);
        }

        renderFolderStructureRecursively(rootFiles, 48);

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void renderFolderStructureRecursively(File[] parentFiles, int offsetX) {
        if (parentFiles.length == 0) return;
        for (File f : parentFiles) {
            if (f.isDirectory()) {

                String name = f.getName();
                name = renderImageBesideTreeNode(name, dirAssetGrey.getTextureId(), 20, 2, 20, 20);
                ImGui.setCursorPosX(ImGui.getCursorPosX() - offsetX);

                if (ImGui.treeNodeEx(name)) {
                    if (ImGui.isItemHovered(ImGuiMouseButton.Left) && mouseButtonDispatcher.getButtons()[GLFW_MOUSE_BUTTON_1]) {
                        currentFile = f;
                        currentPathImString.set(currentFile.getPath());
                        searchImString.set("");
                    }
                    renderFolderStructureRecursively(Objects.requireNonNull(f.listFiles()), 0);
                    ImGui.dummy(0, 0);
                    ImGui.setCursorPosY(ImGui.getCursorPosY() - 4); //because dummy takes a lill extra space
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

    private static final int fileImageWidth = 70, fileImageHeight = 90;
    private static final int dirImageWidth = 70, dirImageHeight = 80;

    private enum OxyImageButtonType {
        Directory(), File()
    }

    private static final record OxyImageButton(OxyImageButtonType type, String name, float cursorPosX,
                                               float cursorPosY) {
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
            if (removeFileExtension(f.getName()).toLowerCase().contains(searchImString.get().toLowerCase()))
                fList.add(f);
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
        final int wrapWidthFile = 5 + fileImageWidth;
        final int wrapWidthDir = 5 + dirImageWidth;
        final int ySpacingBetweenButtons = 20;

        for (File f : dir) {
            OxyImageButton button;
            if (f.isDirectory()) {
                button = new OxyImageButton(OxyImageButtonType.Directory, f.getName(), ImGui.getCursorPosX(), ImGui.getCursorPosY() + fileImageHeight + imageOffsetFromWindowY);
                ImGui.imageButton(dirAsset.getTextureId(), dirImageWidth, dirImageHeight, 0, 1, 1, 0);
                if (ImGui.isItemClicked()) {
                    currentFile = f;
                    currentPathImString.set(currentFile.getPath());
                    searchImString.set("");
                }
            } else {
                button = new OxyImageButton(OxyImageButtonType.File, f.getName(), ImGui.getCursorPosX(), ImGui.getCursorPosY() + fileImageHeight + imageOffsetFromWindowY);
                String extension = getExtension(f.getPath());
                if (!renderExtensionImages(extension, fileImageWidth, fileImageHeight) &&
                        !renderTextureExtensionImages(extension, fileImageWidth, fileImageHeight)) {
                    ImGui.imageButton(fileAsset.getTextureId(), fileImageWidth, fileImageHeight, 0, 1, 1, 0); //default file asset
                }

                if (ImGui.isItemClicked()) lastDragDropFile = f;
                if (lastDragDropFile != null) {
                    if (ImGui.beginDragDropSource()) {
                        ImGui.setDragDropPayloadObject("projectPanelFile", lastDragDropFile);
                        ImGui.endDragDropSource();
                    }
                }
            }
            imageButtonList.add(button);

            float lastButton = ImGui.getItemRectMaxX();
            float nextButton = lastButton + ImGui.getStyle().getItemInnerSpacingX() + fileImageWidth;
            if (nextButton < windowVisible) {
                ImGui.sameLine();
            } else {
                String lastButtonString = imageButtonList.get(imageButtonList.size() - 1).name;
                ImVec2 lastButtonTextSize = new ImVec2();
                ImGui.calcTextSize(lastButtonTextSize, lastButtonString);
                ImGui.dummy(0, lastButtonTextSize.y + ySpacingBetweenButtons); //spacing (taking also the length of the name accountable)
                ImGui.setCursorPosX(ImGui.getCursorPosX() + imageOffsetFromWindowX);
            }
        }


        for (OxyImageButton button : imageButtonList) {

            int wrapWidth;
            if (button.type == OxyImageButtonType.File) wrapWidth = wrapWidthFile;
            else wrapWidth = wrapWidthDir;

            int imageWidth;
            if (button.type == OxyImageButtonType.File) imageWidth = fileImageWidth;
            else imageWidth = dirImageWidth;

            ImVec2 textSize = new ImVec2();
            ImGui.calcTextSize(textSize, button.name, wrapWidth);

            ImGui.setCursorPosX(button.cursorPosX + imageWidth / 2f - textSize.x / 2f);
            ImGui.setCursorPosY(button.cursorPosY);

            ImGui.pushTextWrapPos(button.cursorPosX + wrapWidth);
            ImGui.textWrapped(button.name);
            ImGui.popTextWrapPos();
        }
    }

    private static boolean renderExtensionImages(String destExtension, float sizeX, float sizeY) {
        if (!isSupportedModelFileExtension(destExtension))
            return false;

        if (destExtension.equalsIgnoreCase("obj"))
            ImGui.imageButton(objAsset.getTextureId(), sizeX, sizeY, 0, 1, 1, 0);
        else if (destExtension.equalsIgnoreCase("fbx"))
            ImGui.imageButton(fbxAsset.getTextureId(), sizeX, sizeY, 0, 1, 1, 0);
        else if (destExtension.equalsIgnoreCase("gltf"))
            ImGui.imageButton(gltfAsset.getTextureId(), sizeX, sizeY, 0, 1, 1, 0);
        else if (destExtension.equalsIgnoreCase("blend"))
            ImGui.imageButton(blendAsset.getTextureId(), sizeX + 20, sizeY, 0, 1, 1, 0);

        return true;
    }

    private static boolean renderTextureExtensionImages(String destExtension, float sizeX, float sizeY) {
        if (!isSupportedTextureFile(destExtension))
            return false;

        if (destExtension.equalsIgnoreCase("png"))
            ImGui.imageButton(pngAsset.getTextureId(), sizeX, sizeY, 0, 1, 1, 0);
        else if (destExtension.equalsIgnoreCase("jpg") || destExtension.equalsIgnoreCase("jpeg"))
            ImGui.imageButton(jpgAsset.getTextureId(), sizeX, sizeY, 0, 1, 1, 0);

        return true;
    }
}
