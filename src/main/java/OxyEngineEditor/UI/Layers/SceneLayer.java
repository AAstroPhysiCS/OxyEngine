package OxyEngineEditor.UI.Layers;

import OxyEngineEditor.Sandbox.OxyComponents.PerspectiveCamera;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Renderer.Texture.OxyTextureCoords;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.Sandbox.Scene.WorldGrid;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector3f;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class SceneLayer extends UILayer {

    public static boolean focusedWindowDragging, focusedWindow;

    public static float width, height;
    public static float x, y;

    private static SceneLayer INSTANCE = null;

    private final Scene scene;

    public static SceneLayer getInstance(WindowHandle windowHandle, Scene scene) {
        if (INSTANCE == null) INSTANCE = new SceneLayer(windowHandle, scene);
        return INSTANCE;
    }

    private SceneLayer(WindowHandle windowHandle, Scene scene) {
        super(windowHandle, scene);
        this.scene = scene;
        new WorldGrid(scene, 50);
    }

    @Override
    public void preload() {
    }

    static int counter = 1;

    @Override
    public void renderLayer() {
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

        FrameBuffer frameBuffer = scene.getFrameBuffer();
        if(frameBuffer != null) {
            ImGui.image(frameBuffer.getColorAttachment(), frameBuffer.getWidth(), frameBuffer.getHeight(), 0, 1, 1, 0);

            if (availContentRegionSize.x != frameBuffer.getWidth() || availContentRegionSize.y != frameBuffer.getHeight()) {
                frameBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
                if (scene.getRenderer().getCamera() instanceof PerspectiveCamera p)
                    p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
            }
        }

        if (ImGui.beginDragDropTarget()) {
            byte[] data = ImGui.acceptDragDropPayload("mousePosViewportLayer");
            if (data != null) {
                OxyModel model = scene.createModelEntity(new String(data));
                //TEMP
                OxyTexture texture = null;
                if(PropertiesLayer.lastTexturePath != null){
                    texture = OxyTexture.load(PropertiesLayer.lastTexturePath, OxyTextureCoords.FULL.getTcs());
                    PropertiesLayer.lastTextureID = texture.getTextureId();
                }

                OxyColor color = new OxyColor(PropertiesLayer.diffuseColor);
                model.addComponent(new SelectedComponent(false), texture, color, new TransformComponent(new Vector3f(-30, -10 * counter++, 0)));
                model.updateData();
                scene.rebuild();
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
