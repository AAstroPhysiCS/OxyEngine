package OxyEngine.System;

import OxyEngine.Core.Window.Input;
import OxyEngine.Core.Window.OxyWindow;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseCursor;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.io.File;
import java.util.Objects;

import static OxyEngine.System.OxySystem.gl_Version;
import static org.lwjgl.glfw.GLFW.*;

public class OxyUISystem {

    private ImGuiIO io;

    private final ImGuiImplGl3 imGuiRenderer;
    private final ImGuiImplGlfw imGuiGlfw;
    private final OxyWindow oxyWindow;

    private final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];

    public OxyUISystem(OxyWindow oxyWindow) {
        this.oxyWindow = oxyWindow;
        imGuiRenderer = new ImGuiImplGl3();
        imGuiGlfw = new ImGuiImplGlfw();
        init();
    }

    private void init() {
        ImGui.createContext();

        io = ImGui.getIO();
        ImGui.getStyle().setColors(loadStyle(OxyFileSystem.load(OxyFileSystem.getResourceByPath("/theme/oxyTheme.txt")).split("\n")));
        io.setIniFilename(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\ini\\imgui.ini").toPath().toAbsolutePath().toString());
        io.setWantSaveIniSettings(true);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setConfigViewportsNoTaskBarIcon(true);
        setKeymap();

        final long winPtr = oxyWindow.getPointer();
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

        File[] file = new File(OxyFileSystem.getResourceByPath("/fonts/")).listFiles();
        assert file != null;
        for (File f : file) {
            OxySystem.Font.load(io, f.getPath(), 16, f.getName().split("\\.")[0]);
            OxySystem.Font.load(io, f.getPath(), 19, f.getName().split("\\.")[0]);
        }

        imGuiRenderer.init(gl_Version);
        imGuiGlfw.init(oxyWindow.getPointer(), true);

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
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
        glfwGetFramebufferSize(oxyWindow.getPointer(), fbWidth, fbHeight);

        io.setDisplaySize(oxyWindow.getWidth(), oxyWindow.getHeight());
        io.setDisplayFramebufferScale((float) fbWidth[0] / oxyWindow.getWidth(), (float) fbHeight[0] / oxyWindow.getHeight());
        io.setMousePos(Input.getMouseX(), Input.getMouseY());
        io.setDeltaTime(deltaTime);

        final int imguiCursor = ImGui.getMouseCursor();
        glfwSetCursor(oxyWindow.getPointer(), mouseCursors[imguiCursor]);
        glfwSetInputMode(oxyWindow.getPointer(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
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
}
