package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Font.OxyFontSystem;
import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

import static OxyEngineEditor.UI.Panels.Panel.bgC;

public class OverlayPanelLayer extends Layer {

    private final List<Panel> panelList = new ArrayList<>();

    private final WindowHandle windowHandle;

    public OverlayPanelLayer(WindowHandle windowHandle, Scene scene) {
        super(scene);
        this.windowHandle = windowHandle;
    }

    public void addPanel(Panel panel) {
        panelList.add(panel);
    }

    @Override
    public void build() {
        for (Panel panel : panelList) {
            panel.preload();
        }
    }

    @Override
    public void rebuild() {

    }

    @Override
    public void update(float ts, float deltaTime) {
        windowHandle.update();
    }

    @Override
    public void render(float ts, float deltaTime) {
        OpenGLRendererAPI.clearBuffer();
        OpenGLRendererAPI.clearColor(32, 32, 32, 1.0f);
        ImGui.newFrame();

        ImGui.setNextWindowPos(0, 30, ImGuiCond.Always);
        ImGui.setNextWindowSize(windowHandle.getWidth(), windowHandle.getHeight() - 30, ImGuiCond.Always);
        ImGui.setNextWindowDockID(1);

        ImGui.pushStyleColor(ImGuiCol.DockingEmptyBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushFont(OxyFontSystem.getAllFonts().get(0));
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 8);
        ImGui.begin("Main", ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoBackground |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoDecoration);
        ImGui.popStyleColor();
        ImGui.dockSpace(1);

        for (Panel panel : panelList) {
            panel.renderPanel();
        }

        ImGui.end();
        ImGui.popFont();
        ImGui.popStyleVar();

        ImGui.render();
        scene.getOxyUISystem().renderDrawData();
    }
}
