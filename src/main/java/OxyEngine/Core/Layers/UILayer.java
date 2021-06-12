package OxyEngine.Core.Layers;

import OxyEngine.OxyEngine;
import OxyEngine.Scene.SceneRuntime;
import OxyEngine.System.OxyFontSystem;
import OxyEngine.System.OxyUISystem;
import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.*;

import java.util.ArrayList;
import java.util.List;

public class UILayer extends Layer {

    private final List<Panel> panelList = new ArrayList<>();

    public static OxyUISystem uiSystem;

    private static UILayer INSTANCE = null;

    public static UILayer getInstance() {
        if (INSTANCE == null) INSTANCE = new UILayer();
        return INSTANCE;
    }

    private UILayer() {
        uiSystem = new OxyUISystem(OxyEngine.getWindowHandle());
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
    public void update(float ts) {
        OxyEngine.getWindowHandle().update();
        UILayer.uiSystem.dispatchNativeEvents();
    }

    @Override
    public void render() {
        uiSystem.newFrameGLFW();
        ImGui.newFrame();
        ImGuizmo.beginFrame();

        final ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY(), ImGuiCond.Always);
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY(), ImGuiCond.Always);

        ImGui.pushFont(OxyFontSystem.getAllFonts().get(0));

        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.begin("Main", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoNavFocus |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoDecoration);
        int id = ImGui.getID("MyDockSpace");
        ImGui.dockSpace(id, 0, 0, ImGuiDockNodeFlags.PassthruCentralNode);
        ImGui.end();
        ImGui.popStyleVar(3);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 3);
        ImGui.pushStyleColor(ImGuiCol.TableHeaderBg, Panel.childCardBgC[0], Panel.childCardBgC[1], Panel.childCardBgC[2], Panel.childCardBgC[3]);
        ImGui.pushStyleColor(ImGuiCol.TableBorderLight, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);
        ImGui.pushStyleColor(ImGuiCol.FrameBg, Panel.frameBgC[0], Panel.frameBgC[1], Panel.frameBgC[2], Panel.frameBgC[3]);
        for (Panel panel : panelList)
            panel.renderPanel();
        ImGui.popStyleVar(2);
        ImGui.popStyleColor(3);

        ImGui.popFont();

        uiSystem.updateImGuiContext(SceneRuntime.TS);
        ImGui.render();
        uiSystem.renderDrawData();
    }
}
