package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.SelectedComponent;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.Objects.WorldGrid;
import OxyEngine.Scene.SceneRuntime;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.io.File;
import java.util.List;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.System.OxySystem.getExtension;
import static OxyEngine.System.OxySystem.isSupportedModelFileExtension;
import static OxyEngineEditor.EditorApplication.oxyShader;

public class ScenePanel extends Panel {

    public static boolean focusedWindowDragging, focusedWindow, hoveredWindow;

    public static final ImVec2 windowSize = new ImVec2();
    public static final ImVec2 windowPos = new ImVec2();
    public static final ImVec2 mousePos = new ImVec2();
    public static final ImVec2 offset = new ImVec2(); //Window position relative to the window... means that it subtracts the tab

    private static ScenePanel INSTANCE = null;

    public static ScenePanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ScenePanel();
        return INSTANCE;
    }

    private ScenePanel() {
        new WorldGrid(ACTIVE_SCENE, 10);
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderPanel() {

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        ImGui.begin("Viewport", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoTitleBar);

        ImGui.getWindowSize(windowSize);
        ImGui.getWindowPos(windowPos);
        ImGui.getMousePos(mousePos);
        ImGui.getCursorPos(offset);

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(2);
        focusedWindow = ImGui.isWindowFocused();
        hoveredWindow = ImGui.isWindowHovered();

        ImVec2 availContentRegionSize = new ImVec2();
        ImGui.getContentRegionAvail(availContentRegionSize);

        OpenGLFrameBuffer blittedFrameBuffer = ACTIVE_SCENE.getBlittedFrameBuffer();

        if (blittedFrameBuffer != null) {
            ImGui.image(blittedFrameBuffer.getColorAttachmentTexture(0), blittedFrameBuffer.getWidth(), blittedFrameBuffer.getHeight(), 0, 1, 1, 0);
            if(ImGui.beginDragDropTarget()){
                File f = (File) ImGui.acceptDragDropPayloadObject("projectPanelFile");
                if(f != null) {
                    String fPath = f.getPath();
                    if (isSupportedModelFileExtension(getExtension(fPath))) {
                        List<OxyModel> eList = ACTIVE_SCENE.createModelEntities(fPath, oxyShader);
                        for(OxyModel e : eList){
                            e.addComponent(new SelectedComponent(false));
                            e.constructData();
                        }
                        SceneLayer.getInstance().updateModelEntities();
                    }
                    ProjectPanel.lastDragDropFile = null;
                }
                ImGui.endDragDropTarget();
            }

            if (availContentRegionSize.x != blittedFrameBuffer.getWidth() || availContentRegionSize.y != blittedFrameBuffer.getHeight()) {
                OpenGLFrameBuffer pickingBuffer = ACTIVE_SCENE.getPickingBuffer();
                OpenGLFrameBuffer srcFrameBuffer = ACTIVE_SCENE.getFrameBuffer();
                blittedFrameBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
                srcFrameBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
                pickingBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
                if (SceneRuntime.currentBoundedCamera instanceof PerspectiveCamera p)
                    p.setAspect((float) blittedFrameBuffer.getWidth() / blittedFrameBuffer.getHeight());
            }
        }

        ImGui.popStyleVar();
        ImGui.end();
    }
}
