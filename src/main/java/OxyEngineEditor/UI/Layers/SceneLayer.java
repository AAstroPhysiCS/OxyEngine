package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.OxyObjects.Cube;
import OxyEngine.Core.OxyObjects.WorldGrid;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.UI.UILayer;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector3f;

import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.sandBoxMesh;
import static OxyEngine.System.Globals.Globals.normalizeColor;

public class SceneLayer extends UILayer {

    private final WorldGrid worldGrid;

    public static boolean focusedWindowDragging, focusedWindow;

    public static float width, height;
    public static float x, y;

    private static SceneLayer INSTANCE = null;

    public static SceneLayer getInstance(WindowHandle windowHandle, OxyRenderer3D renderer){
        if(INSTANCE == null) INSTANCE = new SceneLayer(windowHandle, renderer);
        return INSTANCE;
    }

    private SceneLayer(WindowHandle windowHandle, OxyRenderer renderer) {
        super(windowHandle, renderer);
        worldGrid = new WorldGrid(renderer, 50);
    }

    @Override
    public void preload() {
    }

    static int counter = 1;
    public static Cube cube;

    @Override
    public void renderLayer() {
        currentRenderer.getShader().enable();
        worldGrid.render();
        currentRenderer.getShader().disable();

        ImGui.setNextWindowSize(windowHandle.getWidth() / 1.3f, windowHandle.getHeight() / 1.3f, ImGuiCond.Once);
        ImGui.setNextWindowPos(40, 40, ImGuiCond.Once);

        ImGui.pushStyleColor(ImGuiCol.ChildBg, normalizeColor(20), normalizeColor(20), normalizeColor(20), 1.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 50, 50);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.begin("Viewport", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoTitleBar);

        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        width = windowSize.x;
        height = windowSize.y;

        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        x = windowPos.x;
        y = windowPos.y;

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(0);
        focusedWindow = ImGui.isWindowFocused();

        ImVec2 availContentRegionSize = new ImVec2();
        ImGui.getContentRegionAvail(availContentRegionSize);

        FrameBuffer frameBuffer = sandBoxMesh.obj.getFrameBuffer();
        ImGui.image(frameBuffer.getColorAttachment(), frameBuffer.getWidth(), frameBuffer.getHeight(), 0, 1, 1, 0);

        if (availContentRegionSize.x != frameBuffer.getWidth() || availContentRegionSize.y != frameBuffer.getHeight()) {
            frameBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
            if (currentRenderer.getCamera() instanceof PerspectiveCamera p)
                p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
        }

        if (ImGui.beginDragDropTarget()) {
            if (ImGui.acceptDragDropPayload("mousePosViewportLayer") != null) {
                cube = new Cube(1, new Vector3f(-30, -10 * counter++, 0), new Vector3f(0, 0, 0));
                cube.initData(sandBoxMesh.obj);
                sandBoxMesh.obj.add(cube);
            }
            ImGui.endDragDropTarget();
        }

        ImGui.popStyleVar();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
        ImGui.popStyleVar();
        ImGui.popStyleVar();
        ImGui.end();
    }
}
