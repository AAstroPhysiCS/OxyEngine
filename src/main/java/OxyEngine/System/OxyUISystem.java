package OxyEngine.System;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.Events.GLFW.GLFWEventDispatcher;
import OxyEngine.Events.GLFW.GLFWEventType;
import OxyEngine.Events.OxyEventDispatcher;
import OxyEngine.Events.OxyKeyEvent;
import OxyEngine.Events.OxyMouseEvent;
import OxyEngine.OxyEngine;
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

import static OxyEngine.System.OxyEventSystem.*;
import static OxyEngine.System.OxySystem.gl_Version;
import static org.lwjgl.glfw.GLFW.*;

public class OxyUISystem {

    private ImGuiIO io;

    private final ImGuiImplGl3 imGuiRenderer;
    private final ImGuiImplGlfw imGuiGlfw;
    private final WindowHandle windowHandle;

    private final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];

    public OxyUISystem(WindowHandle windowHandle) {
        this.windowHandle = windowHandle;
        imGuiRenderer = new ImGuiImplGl3();
        imGuiGlfw = new ImGuiImplGlfw();
        dispatcher = new OxyEventDispatcher();
        init();
    }

    private void init() {
        ImGui.createContext();

        io = ImGui.getIO();
        ImGui.getStyle().setColors(OxyEngine.getLoadedTheme());
        io.setIniFilename(new File(System.getProperty("user.dir") + "\\src\\main\\resources\\ini\\imgui.ini").toPath().toAbsolutePath().toString());
        io.setWantSaveIniSettings(true);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setConfigViewportsNoTaskBarIcon(true);
        setKeymap();

        mouseButtonDispatcher = (GLFWEventDispatcher.MouseEvent) GLFWEventDispatcher.getInstance(GLFWEventType.MouseEvent, io);
        mouseCursorPosDispatcher = (GLFWEventDispatcher.MouseCursorPosEvent) GLFWEventDispatcher.getInstance(GLFWEventType.MouseCursorPosEvent, io);
        keyEventDispatcher = (GLFWEventDispatcher.KeyEvent) GLFWEventDispatcher.getInstance(GLFWEventType.KeyEvent, io);
        mouseScrollDispatcher = (GLFWEventDispatcher.MouseScrollEvent) GLFWEventDispatcher.getInstance(GLFWEventType.MouseScrollEvent, io);

        final long winPtr = windowHandle.getPointer();
        glfwSetMouseButtonCallback(winPtr, mouseButtonDispatcher);
        glfwSetKeyCallback(winPtr, keyEventDispatcher);
        glfwSetScrollCallback(winPtr, mouseScrollDispatcher);
        glfwSetCursorPosCallback(winPtr, mouseCursorPosDispatcher);

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

        File[] file = new File(OxySystem.FileSystem.getResourceByPath("/fonts/")).listFiles();
        assert file != null;
        for (File f : file) {
            OxyFontSystem.load(io, f.getPath(), 15, f.getName().split("\\.")[0]);
            OxyFontSystem.load(io, f.getPath(), 18, f.getName().split("\\.")[0]);
        }

        imGuiRenderer.init(gl_Version);
        imGuiGlfw.init(windowHandle.getPointer(), true);

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final ImGuiStyle style = ImGui.getStyle();
            style.setWindowRounding(0.0f);
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1));
        }
    }

    public void updateImGuiContext(float deltaTime) {
        int[] fbWidth = new int[1];
        int[] fbHeight = new int[1];
        glfwGetFramebufferSize(windowHandle.getPointer(), fbWidth, fbHeight);

        io.setDisplaySize(windowHandle.getWidth(), windowHandle.getHeight());
        io.setDisplayFramebufferScale((float) fbWidth[0] / windowHandle.getWidth(), (float) fbHeight[0] / windowHandle.getHeight());
        io.setMousePos((float) OxyEventSystem.mouseCursorPosDispatcher.getXPos(), (float) OxyEventSystem.mouseCursorPosDispatcher.getYPos());
        io.setDeltaTime(deltaTime);

        final int imguiCursor = ImGui.getMouseCursor();
        glfwSetCursor(windowHandle.getPointer(), mouseCursors[imguiCursor]);
        glfwSetInputMode(windowHandle.getPointer(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void dispatchNativeEvents() {
        dispatcher.dispatch(OxyKeyEvent.class);
        dispatcher.dispatch(OxyMouseEvent.class);
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
