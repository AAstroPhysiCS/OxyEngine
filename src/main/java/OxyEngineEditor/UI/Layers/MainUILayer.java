package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.UI.Font.OxyFontSystem;
import OxyEngineEditor.UI.UILayer;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

public class MainUILayer extends UILayer {

    private static final List<UILayer> ALL_UI_COMPONENTS_LINKED = new ArrayList<>();

    public MainUILayer(WindowHandle windowHandle, OxyRenderer currentRenderer) {
        super(windowHandle, currentRenderer);
    }

    public void addUILayers(UILayer component){
        ALL_UI_COMPONENTS_LINKED.add(component);
    }

    @Override
    public void preload() {
        for(UILayer component : ALL_UI_COMPONENTS_LINKED){
            component.preload();
        }
    }

    @Override
    public void renderLayer() {
        ImGui.setNextWindowPos(0, 30, ImGuiCond.Always);
        ImGui.setNextWindowSize(windowHandle.getWidth(), windowHandle.getHeight() - 30, ImGuiCond.Always);
        ImGui.setNextWindowDockID(1);

        ImGui.pushStyleColor(ImGuiCol.DockingEmptyBg, bgC[0], bgC[1], bgC[2], bgC[3]);
        ImGui.pushFont(OxyFontSystem.getAllFonts().get(0));
        ImGui.begin("Main", ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.NoBackground |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus |
                ImGuiWindowFlags.NoDecoration);

        ImGui.popStyleColor();
        ImGui.dockSpace(1);

        for(UILayer component : ALL_UI_COMPONENTS_LINKED){
            component.renderLayer();
        }

        ImGui.end();
        ImGui.popFont();
    }
}
