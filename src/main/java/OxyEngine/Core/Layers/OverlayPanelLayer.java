package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.System.OxyFontSystem;
import OxyEngineEditor.UI.Panels.*;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;

public class OverlayPanelLayer extends Layer {

    private final List<Panel> panelList = new ArrayList<>();

    private final WindowHandle windowHandle;

    public OverlayPanelLayer(WindowHandle windowHandle) {
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
    public void update(float ts) {
        windowHandle.update();
    }

    @Override
    public void render(float ts) {
        OpenGLRendererAPI.clearBuffer();
        OpenGLRendererAPI.clearColor(32, 32, 32, 1.0f);

        ACTIVE_SCENE.getOxyUISystem().newFrameGLFW();
        ImGui.newFrame();

        final ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY(), ImGuiCond.Always);
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY(), ImGuiCond.Always);

        ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 8);
        ImGui.pushFont(OxyFontSystem.getAllFonts().get(0));

        ImGui.begin("Main", ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoBackground |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoDecoration);
        int id = ImGui.getID("MyDockSpace");
        ImGui.dockSpace(id, 0, 0, ImGuiDockNodeFlags.PassthruCentralNode);
        ImGui.end();
        for (Panel panel : panelList)
            panel.renderPanel();
        ImGui.popFont();
        ImGui.popStyleVar(3);

        ImGui.render();
        ACTIVE_SCENE.getOxyUISystem().renderDrawData();
    }
}
