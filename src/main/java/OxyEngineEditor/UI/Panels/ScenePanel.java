package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngineEditor.Scene.Objects.WorldGrid;
import OxyEngineEditor.Scene.SceneRuntime;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

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

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(2);
        focusedWindow = ImGui.isWindowFocused();
        hoveredWindow = ImGui.isWindowHovered();

        ImVec2 availContentRegionSize = new ImVec2();
        ImGui.getContentRegionAvail(availContentRegionSize);

        OpenGLFrameBuffer frameBuffer = SceneRuntime.ACTIVE_SCENE.getFrameBuffer();
        if (frameBuffer != null) {
            ImGui.image(frameBuffer.getColorAttachmentTexture(), frameBuffer.getWidth(), frameBuffer.getHeight(), 0, 1, 1, 0);

            if (availContentRegionSize.x != frameBuffer.getWidth() || availContentRegionSize.y != frameBuffer.getHeight()) {
                frameBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
                if (SceneRuntime.currentBoundedCamera instanceof PerspectiveCamera p)
                    p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
            }
        }

        ImGui.popStyleVar();
        ImGui.end();
    }
}
