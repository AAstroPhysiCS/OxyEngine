package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Scene;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;

public class PropertiesLayer extends UILayer {

    public PropertiesLayer(WindowHandle windowHandle, Scene scene) {
        super(windowHandle, scene);
    }

    private static PropertiesLayer INSTANCE = null;

    public static PropertiesLayer getInstance(WindowHandle windowHandle, Scene scene) {
        if(INSTANCE == null) INSTANCE = new PropertiesLayer(windowHandle, scene);
        return INSTANCE;
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

        ImGui.begin("Properties");

        ImGui.end();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }
}
