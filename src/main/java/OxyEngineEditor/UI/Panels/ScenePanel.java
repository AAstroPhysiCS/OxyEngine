package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Components.PerspectiveCamera;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngineEditor.Scene.Model.OxyModel;
import OxyEngineEditor.Scene.WorldGrid;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector3f;

import static OxyEngine.System.Globals.Globals.normalizeColor;

public class ScenePanel extends UIPanel {

    public static boolean focusedWindowDragging, focusedWindow;

    public static final ImVec2 windowSize = new ImVec2();
    public static final ImVec2 windowPos = new ImVec2();
    public static final ImVec2 mousePos = new ImVec2();
    public static final ImVec2 offset = new ImVec2(); //Window position relative to the window... means that it subtracts the tab

    private static ScenePanel INSTANCE = null;

    private final SceneLayer sceneLayer;
    private final OxyShader shader;

    public static ScenePanel getInstance(WindowHandle windowHandle, SceneLayer sceneLayer, OxyShader shader) {
        if (INSTANCE == null) INSTANCE = new ScenePanel(windowHandle, sceneLayer, shader);
        return INSTANCE;
    }

    public static ScenePanel getInstance() { return INSTANCE; }

    private ScenePanel(WindowHandle windowHandle, SceneLayer sceneLayer, OxyShader shader) {
        super(windowHandle);
        this.sceneLayer = sceneLayer;
        this.shader = shader;
        new WorldGrid(sceneLayer.getScene(), 25, shader);
    }

    @Override
    public void preload() {
    }

    static int counter = 1;

    @Override
    public void renderPanel() {

        ImGui.pushStyleColor(ImGuiCol.ChildBg, normalizeColor(20), normalizeColor(20), normalizeColor(20), 1.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 50, 50);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        ImGui.begin("Scene", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoTitleBar);

        ImGui.getWindowSize(windowSize);
        ImGui.getWindowPos(windowPos);
        ImGui.getMousePos(mousePos);
        ImGui.getCursorPos(offset);

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(2);
        focusedWindow = ImGui.isWindowFocused();

        ImVec2 availContentRegionSize = new ImVec2();
        ImGui.getContentRegionAvail(availContentRegionSize);

        FrameBuffer frameBuffer = sceneLayer.getScene().getFrameBuffer();
        if (frameBuffer != null) {
            ImGui.image(frameBuffer.getColorAttachment(), frameBuffer.getWidth(), frameBuffer.getHeight(), 0, 1, 1, 0);

            if (availContentRegionSize.x != frameBuffer.getWidth() || availContentRegionSize.y != frameBuffer.getHeight()) {
                frameBuffer.resize(availContentRegionSize.x, availContentRegionSize.y);
                if (sceneLayer.getScene().getRenderer().getCamera() instanceof PerspectiveCamera p)
                    p.setAspect((float) frameBuffer.getWidth() / frameBuffer.getHeight());
            }
        }

        if (ImGui.beginDragDropTarget()) {
            byte[] data = ImGui.acceptDragDropPayload("mousePosViewportLayer");
            if (data != null) {
                OxyModel model = sceneLayer.getScene().createModelEntity(new String(data), shader);
                //TEMP
                ImageTexture texture = null;
                if (PropertiesPanel.lastTexturePath != null) {
                    texture = OxyTexture.loadImage(PropertiesPanel.lastTexturePath);
                    PropertiesPanel.lastTextureID = texture.getTextureId();
                }

                OxyColor color = new OxyColor(PropertiesPanel.diffuseColor);
                model.addComponent(new SelectedComponent(false), texture, color, new TransformComponent(new Vector3f(-30, -10 * counter++, 0)));
                model.updateData();
                sceneLayer.rebuild();
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
