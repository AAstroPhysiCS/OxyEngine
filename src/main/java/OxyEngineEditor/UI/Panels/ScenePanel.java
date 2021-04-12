package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.EditorCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.SceneRuntime;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector3f;

import java.io.File;
import java.util.List;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.Scene.SceneRuntime.currentBoundedCamera;
import static OxyEngine.System.OxySystem.getExtension;
import static OxyEngine.System.OxySystem.isSupportedModelFileExtension;

public class ScenePanel extends Panel {

    public static boolean focusedWindowDragging, focusedWindow, hoveredWindow;

    public static final ImVec2 windowSize = new ImVec2();
    public static final ImVec2 windowPos = new ImVec2();
    public static final ImVec2 mousePos = new ImVec2();
    public static final ImVec2 offsetScreenPos = new ImVec2();
    public static final ImVec2 offset = new ImVec2(); //Window position relative to the window... means that it subtracts the tab

    private static ScenePanel INSTANCE = null;

    public static ScenePanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ScenePanel();
        return INSTANCE;
    }

    private ScenePanel() {
    }

    @Override
    public void preload() {
    }

    public static OxyNativeObject editorCameraEntity;

    @Override
    public void renderPanel() {

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        ImGui.begin("Viewport", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoTitleBar);

        ImGui.getWindowSize(windowSize);
        ImGui.getWindowPos(windowPos);
        ImGui.getMousePos(mousePos);
        ImGui.getCursorPos(offset);
        ImGui.getCursorScreenPos(offsetScreenPos);

        if (editorCameraEntity == null) {
            editorCameraEntity = ACTIVE_SCENE.createNativeObjectEntity();
            EditorCamera editorCamera = new EditorCamera(true, 45f, ScenePanel.windowSize.x / ScenePanel.windowSize.y, 1f, 10000f, true);
            editorCameraEntity.addComponent(new TransformComponent(new Vector3f(0), new Vector3f(-0.35f, -0.77f, 0.0f)), editorCamera, new TagComponent("Editor Camera"));
            currentBoundedCamera = editorCamera;
            SceneLayer.getInstance().mainCamera = editorCamera;
        }

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(2);
        focusedWindow = ImGui.isWindowFocused();
        hoveredWindow = ImGui.isWindowHovered();

        ImVec2 availContentRegionSize = new ImVec2();
        ImGui.getContentRegionAvail(availContentRegionSize);

        OpenGLFrameBuffer blittedFrameBuffer = ACTIVE_SCENE.getBlittedFrameBuffer();

        if (blittedFrameBuffer != null) {
            ImGui.image(blittedFrameBuffer.getColorAttachmentTexture(0)[0], blittedFrameBuffer.getWidth(), blittedFrameBuffer.getHeight(), 0, 1, 1, 0);
            if (ImGui.beginDragDropTarget()) {
                File f = ImGui.acceptDragDropPayload("projectPanelFile");
                if (f != null) {
                    String fPath = f.getPath();
                    String extension = getExtension(fPath);
                    if (isSupportedModelFileExtension(extension)) {
                        List<OxyModel> eList = ACTIVE_SCENE.createModelEntities(fPath);
                        for (OxyModel e : eList) {
                            e.addComponent(new SelectedComponent(false));
                            e.constructData();
                            e.getGUINodes().add(ModelMeshOpenGL.guiNode);
                            if (!e.getGUINodes().contains(OxyMaterial.guiNode))
                                e.getGUINodes().add(OxyMaterial.guiNode);
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
