package OxyEngine.Core.Window;

import OxyEngineEditor.UI.Font.OxyFontSystem;
import OxyEngineEditor.UI.Layers.Layer;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

import static OxyEngineEditor.UI.Layers.UILayer.bgC;
import static org.lwjgl.glfw.GLFW.*;

public class WindowHandle {

    private final WindowMode mode;
    private int width;
    private int height;
    private long pointer;
    private final String title;

    private final List<Layer> layerList = new ArrayList<>();

    public WindowHandle(String title, int width, int height, WindowMode mode) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.mode = mode;
    }

    public enum WindowMode {
        FULLSCREEN(), WINDOWEDFULLSCREEN(), WINDOWED()
    }

    public void update() {
        glfwSetWindowSizeLimits(pointer, 1366, 768, GLFW_DONT_CARE, GLFW_DONT_CARE);

        int[] w = new int[1];
        int[] h = new int[1];

        glfwGetWindowSize(pointer, w, h);
        this.width = w[0];
        this.height = h[0];
    }

    public void addLayer(Layer layer) {
        layerList.add(layer);
    }

    public void removeLayer(Layer layer) {
        layerList.remove(layer);
    }

    public void removeLayer(int index) {
        layerList.remove(index);
    }

    public void preloadAllLayers() {
        for (Layer layer : layerList) {
            layer.preload();
        }
    }

    public void renderAllLayers() {
        ImGui.setNextWindowPos(0, 30, ImGuiCond.Always);
        ImGui.setNextWindowSize(this.getWidth(), this.getHeight() - 30, ImGuiCond.Always);
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
        for (Layer layer : layerList) {
            layer.renderLayer();
        }
        ImGui.end();
        ImGui.popFont();
    }

    public WindowMode getMode() {
        return mode;
    }

    public void setPointer(long id) {
        this.pointer = id;
    }

    public long getPointer() {
        return pointer;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getTitle() {
        return title;
    }
}
