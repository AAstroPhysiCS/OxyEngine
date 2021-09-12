package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.Event;
import OxyEngine.Core.Window.Window;
import OxyEngine.System.FileSystem;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.ImGuiViewport;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static OxyEngine.System.OxySystem.gl_Version;
import static org.lwjgl.glfw.GLFW.*;

public final class ImGuiLayer extends Layer {

    private ImGuiIO io;

    private final ImGuiImplGl3 imGuiRenderer;
    private final ImGuiImplGlfw imGuiGlfw;
    private final Window window;

    private final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];

    private final List<Panel> panelList = new ArrayList<>();

    private static ImGuiLayer INSTANCE = null;

    public static ImGuiLayer getInstance(Window window){
        if(INSTANCE == null) INSTANCE = new ImGuiLayer(window);
        return INSTANCE;
    }

    private ImGuiLayer(Window window){
        this.window = window;
        imGuiRenderer = new ImGuiImplGl3();
        imGuiGlfw = new ImGuiImplGlfw();
        init();
    }

    private void init() {
        ImGui.createContext();

        io = ImGui.getIO();
        ImGui.getStyle().setColors(loadStyle(FileSystem.load(FileSystem.getResourceByPath("/theme/oxyTheme.txt")).split("\n")));
        io.setIniFilename(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\ini\\imgui.ini").toPath().toAbsolutePath().toString());
        io.setWantSaveIniSettings(true);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setConfigViewportsNoTaskBarIcon(true);
        setKeymap();

        final long winPtr = window.getPointer();
        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(winPtr, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                final String clipboardString = glfwGetClipboardString(winPtr);
                return Objects.requireNonNullElse(clipboardString, "");
            }
        });

        File[] file = new File(FileSystem.getResourceByPath("/fonts/")).listFiles();
        assert file != null;
        for (File f : file) {
            OxySystem.Font.load(io, f.getPath(), 16, f.getName().split("\\.")[0]);
            OxySystem.Font.load(io, f.getPath(), 19, f.getName().split("\\.")[0]);
        }

        imGuiRenderer.init(gl_Version);
        imGuiGlfw.init(window.getPointer(), true);

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
    }

    public void addPanel(Panel panel) {
        panelList.add(panel);
        panel.preload();
    }

    private static float[][] loadStyle(String[] splittedContent) {
        float[][] allThemeColors = new float[ImGuiCol.COUNT][4];
        for (int i = 0; i < splittedContent.length; i++) {
            String s = splittedContent[i];
            if (s.equals("ImVec4* colors = ImGui::GetStyle().Colors;")) continue;
            float[] value = loadColorValue(s);
            allThemeColors[i] = value;
        }
        return allThemeColors;
    }

    private static float[] loadColorValue(String content) {
        String[] sequence = ((String) content.subSequence(content.indexOf("(") + 1, content.indexOf(")"))).split(",");
        float[] value = new float[sequence.length];
        for (int i = 0; i < value.length; i++) {
            value[i] = Float.parseFloat(sequence[i]);
        }
        return value;
    }

    public void updateImGuiContext(float deltaTime) {
        int[] fbWidth = new int[1];
        int[] fbHeight = new int[1];
        glfwGetFramebufferSize(window.getPointer(), fbWidth, fbHeight);

        io.setDisplaySize(window.getWidth(), window.getHeight());
        io.setDisplayFramebufferScale((float) fbWidth[0] / window.getWidth(), (float) fbHeight[0] / window.getHeight());
        io.setMousePos(Input.getMouseX(), Input.getMouseY());
        io.setDeltaTime(deltaTime);

        final int imguiCursor = ImGui.getMouseCursor();
        glfwSetCursor(window.getPointer(), mouseCursors[imguiCursor]);
        glfwSetInputMode(window.getPointer(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void newFrameGLFW() {
        imGuiGlfw.newFrame();
    }

    public void renderDrawData() {
        imGuiRenderer.renderDrawData(ImGui.getDrawData());
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    public void dispose() {
        for (long mouseCursor : mouseCursors) {
            glfwDestroyCursor(mouseCursor);
        }
        imGuiRenderer.dispose();
        ImGui.destroyContext();
    }

    private void setKeymap() {
        final int[] keyMap = new int[ImGuiKey.COUNT];
        //unfortunately can't use a for loop here
        keyMap[ImGuiKey.Tab] = GLFW_KEY_TAB;
        keyMap[ImGuiKey.LeftArrow] = GLFW_KEY_LEFT;
        keyMap[ImGuiKey.RightArrow] = GLFW_KEY_RIGHT;
        keyMap[ImGuiKey.UpArrow] = GLFW_KEY_UP;
        keyMap[ImGuiKey.DownArrow] = GLFW_KEY_DOWN;
        keyMap[ImGuiKey.PageUp] = GLFW_KEY_PAGE_UP;
        keyMap[ImGuiKey.PageDown] = GLFW_KEY_PAGE_DOWN;
        keyMap[ImGuiKey.Home] = GLFW_KEY_HOME;
        keyMap[ImGuiKey.End] = GLFW_KEY_END;
        keyMap[ImGuiKey.Insert] = GLFW_KEY_INSERT;
        keyMap[ImGuiKey.Delete] = GLFW_KEY_DELETE;
        keyMap[ImGuiKey.Backspace] = GLFW_KEY_BACKSPACE;
        keyMap[ImGuiKey.Space] = GLFW_KEY_SPACE;
        keyMap[ImGuiKey.Enter] = GLFW_KEY_ENTER;
        keyMap[ImGuiKey.Escape] = GLFW_KEY_ESCAPE;
        keyMap[ImGuiKey.KeyPadEnter] = GLFW_KEY_KP_ENTER;
        keyMap[ImGuiKey.A] = GLFW_KEY_A;
        keyMap[ImGuiKey.C] = GLFW_KEY_C;
        keyMap[ImGuiKey.V] = GLFW_KEY_V;
        keyMap[ImGuiKey.X] = GLFW_KEY_X;
        keyMap[ImGuiKey.Y] = GLFW_KEY_Y;
        keyMap[ImGuiKey.Z] = GLFW_KEY_Z;
        io.setKeyMap(keyMap);

        //unfortunately can't use a for loop here
        mouseCursors[ImGuiMouseCursor.Arrow] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.TextInput] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeAll] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNS] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeEW] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNESW] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNWSE] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.Hand] = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        mouseCursors[ImGuiMouseCursor.NotAllowed] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
    }

    @Override
    public void update(float ts) {
        newFrameGLFW();
        ImGui.newFrame();
        ImGuizmo.beginFrame();
        final ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY(), ImGuiCond.Always);
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY(), ImGuiCond.Always);

        ImGui.pushFont(OxySystem.Font.allFonts.get(0));

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

        updateImGuiContext(ts);
        ImGui.render();
        renderDrawData();
    }

    @Override
    public void onEvent(Event event) {

    }
}
