package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Model.ModelType;
import OxyEngineEditor.Sandbox.Scene.Scene;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;

public class ConfigurationLayer extends UILayer {

    private static final ImBoolean helpWindowBool = new ImBoolean();
    static String lastTexturePath = null;

    private static ConfigurationLayer INSTANCE = null;

    public static ConfigurationLayer getInstance(WindowHandle windowHandle, Scene scene) {
        if (INSTANCE == null) INSTANCE = new ConfigurationLayer(windowHandle, scene);
        return INSTANCE;
    }

    private ConfigurationLayer(WindowHandle windowHandle, Scene scene) {
        super(windowHandle, scene);
    }

    @Override
    public void preload() {
    }

    static final ImString inputTextPath = new ImString();
    static final float[] color = new float[4];

    @Override
    public void renderLayer() {
        ImGui.setNextWindowSize(windowHandle.getWidth() / 5f, windowHandle.getHeight() - 300, ImGuiCond.Once);
        ImGui.setNextWindowPos(0, 40, ImGuiCond.Once);

        ImGui.pushStyleColor(ImGuiCol.WindowBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding | ImGuiStyleVar.WindowBorderSize, 0);

        ImGui.begin("Configurator");

        ImGui.text("Name: " + scene.getSceneName());
        ImGui.separator();
        ImGui.spacing();

        if (ImGui.collapsingHeader("Shapes")) {
            if (ImGui.treeNode("Cube")) {
                ImGui.spacing();
                ImGui.inputText("", inputTextPath);
                ImGui.colorPicker4("Color", color);
                ImGui.sameLine();
                if (ImGui.button("...")) {
                    PointerBuffer buffer = PointerBuffer.allocateDirect(16);
                    int result = NativeFileDialog.NFD_OpenDialog("", null, buffer);
                    if (result == NativeFileDialog.NFD_OKAY) {
                        lastTexturePath = buffer.getStringASCII();
                        inputTextPath.set(lastTexturePath);
                    }
                    NativeFileDialog.nNFD_Free(buffer.get());
                }
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
