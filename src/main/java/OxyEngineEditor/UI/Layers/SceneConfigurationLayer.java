package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Scene;
import OxyEngineEditor.UI.UILayer;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;

public class SceneConfigurationLayer extends UILayer {

    private static final ImBoolean helpWindowBool = new ImBoolean();

    private static SceneConfigurationLayer INSTANCE = null;

    public static SceneConfigurationLayer getInstance(WindowHandle windowHandle, Scene scene){
        if(INSTANCE == null) INSTANCE = new SceneConfigurationLayer(windowHandle, scene);
        return INSTANCE;
    }

    private SceneConfigurationLayer(WindowHandle windowHandle, Scene scene) {
        super(windowHandle, scene);
    }

    @Override
    public void preload() {
    }

    @Override
    public void renderLayer() {
        ImGui.setNextWindowSize(windowHandle.getWidth() / 5f, windowHandle.getHeight() - 300, ImGuiCond.Once);
        ImGui.setNextWindowPos(0, 40, ImGuiCond.Once);

        ImGui.pushStyleColor(ImGuiCol.WindowBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding | ImGuiStyleVar.WindowBorderSize, 0);

        ImGui.begin("Scene Configurator");

        ImGui.text("Name: Example Scene");
        ImGui.separator();
        ImGui.spacing();

        if (ImGui.collapsingHeader("Shapes")) {
            if (ImGui.treeNode("Cube")) {
                ImGui.button("Spawn");
                if (ImGui.beginDragDropSource()) {
                    ImGui.setDragDropPayload("mousePosViewportLayer", new byte[]{0}, 1); //dummy data (for now)
                    ImGui.text("Spawn a cube");
                    ImGui.endDragDropSource();
                }
                ImGui.treePop();
            }
            if (ImGui.treeNode("Sphere")) {
                ImGui.treePop();
            }
            if (ImGui.treeNode("Cylinder")) {
                ImGui.treePop();
            }
            if (ImGui.treeNode("Plane")) {
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
