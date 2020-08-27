package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngineEditor.Scene.Model.ModelType;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;

public class ConfigurationPanel extends UIPanel {

    private static final ImBoolean helpWindowBool = new ImBoolean();

    private static ConfigurationPanel INSTANCE = null;

    private final SceneLayer sceneLayer;

    public static ConfigurationPanel getInstance(WindowHandle windowHandle, SceneLayer scene) {
        if (INSTANCE == null) INSTANCE = new ConfigurationPanel(windowHandle, scene);
        return INSTANCE;
    }

    private ConfigurationPanel(WindowHandle windowHandle, SceneLayer sceneLayer) {
        super(windowHandle);
        this.sceneLayer = sceneLayer;
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderPanel() {
        ImGui.pushStyleColor(ImGuiCol.WindowBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding | ImGuiStyleVar.WindowBorderSize, 0);

        ImGui.begin("Configurator");

        ImGui.text("Name: " + sceneLayer.getScene().getSceneName());
        ImGui.separator();
        ImGui.spacing();

        if (ImGui.collapsingHeader("Shapes")) {
            if (ImGui.treeNode("Cube")) {
                ImGui.button("Spawn");
                if (ImGui.beginDragDropSource()) {
                    ImGui.setDragDropPayload("mousePosViewportLayer", ModelType.Cube.getPath().getBytes(), 0);
                    ImGui.text("Spawn a cube");
                    ImGui.endDragDropSource();
                }
                ImGui.treePop();
            }
            if (ImGui.treeNode("Sphere")) {
                ImGui.button("Spawn");
                if (ImGui.beginDragDropSource()) {
                    ImGui.setDragDropPayload("mousePosViewportLayer", ModelType.Sphere.getPath().getBytes(), 0);
                    ImGui.text("Spawn a sphere");
                    ImGui.endDragDropSource();
                }
                ImGui.treePop();
            }
            if (ImGui.treeNode("Cone")) {
                ImGui.button("Spawn");
                if (ImGui.beginDragDropSource()) {
                    ImGui.setDragDropPayload("mousePosViewportLayer", ModelType.Cone.getPath().getBytes(), 0);
                    ImGui.text("Spawn a cone");
                    ImGui.endDragDropSource();
                }
                ImGui.treePop();
            }
            if (ImGui.treeNode("Plane")) {
                ImGui.button("Spawn");
                if (ImGui.beginDragDropSource()) {
                    ImGui.setDragDropPayload("mousePosViewportLayer", ModelType.Plane.getPath().getBytes(), 0);
                    ImGui.text("Spawn a plane");
                    ImGui.endDragDropSource();
                }
                ImGui.treePop();
            }
        }
        ImGui.collapsingHeader("Load model");

        ImGui.checkbox("Configure", helpWindowBool);
        if (helpWindowBool.get()) ImGui.showDemoWindow();

        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }
}
