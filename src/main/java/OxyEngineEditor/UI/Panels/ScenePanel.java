package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngineEditor.Components.PerspectiveCamera;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.WorldGrid;
import OxyEngineEditor.Scene.SceneRuntime;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import static OxyEngine.System.OxyEventSystem.keyEventDispatcher;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;
import static org.lwjgl.glfw.GLFW.*;

public class ScenePanel extends Panel {

    public static boolean focusedWindowDragging, focusedWindow, hoveredWindow;

    public static final ImVec2 windowSize = new ImVec2();
    public static final ImVec2 windowPos = new ImVec2();
    public static final ImVec2 mousePos = new ImVec2();
    public static final ImVec2 offset = new ImVec2(); //Window position relative to the window... means that it subtracts the tab

    private static boolean cPressed = false;

    private static ScenePanel INSTANCE = null;

    private final SceneLayer sceneLayer;

    public static ScenePanel getInstance(SceneLayer sceneLayer) {
        if (INSTANCE == null) INSTANCE = new ScenePanel(sceneLayer);
        return INSTANCE;
    }

    private ScenePanel(SceneLayer sceneLayer) {
        this.sceneLayer = sceneLayer;
        new WorldGrid(SceneRuntime.ACTIVE_SCENE, 10);
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderPanel() {

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        ImGui.begin("Scene", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoTitleBar);

        ImGui.getWindowSize(windowSize);
        ImGui.getWindowPos(windowPos);
        ImGui.getMousePos(mousePos);
        ImGui.getCursorPos(offset);

        if (keyEventDispatcher.getKeys()[GLFW_KEY_DELETE] && entityContext != null) {
            SceneRuntime.ACTIVE_SCENE.removeEntity(entityContext);
            sceneLayer.updateAllModelEntities();
            entityContext = null;
        }

        if (keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_CONTROL] && keyEventDispatcher.getKeys()[GLFW_KEY_C] &&
                entityContext instanceof OxyModel m && !cPressed && focusedWindow) {
            m.copyMe();
            sceneLayer.updateAllModelEntities();
            cPressed = true;
        }
        if (!keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_CONTROL] && !keyEventDispatcher.getKeys()[GLFW_KEY_C])
            cPressed = false;

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(2);
        focusedWindow = ImGui.isWindowFocused();
        hoveredWindow = ImGui.isWindowHovered();

        ImVec2 availContentRegionSize = new ImVec2();
        ImGui.getContentRegionAvail(availContentRegionSize);

        FrameBuffer frameBuffer = SceneRuntime.ACTIVE_SCENE.getFrameBuffer();
        if (frameBuffer != null) {
            ImGui.image(frameBuffer.getColorAttachmentTexture(), frameBuffer.getWidth(), frameBuffer.getHeight(), 0, 1, 1, 0);

            if (availContentRegionSize.x != frameBuffer.getWidth() || availContentRegionSize.y != frameBuffer.getHeight()) {
                frameBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
                if (SceneRuntime.ACTIVE_SCENE.getRenderer().getCamera() instanceof PerspectiveCamera p)
                    p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
            }
        }

        ImGui.popStyleVar();
        ImGui.end();
    }
}
