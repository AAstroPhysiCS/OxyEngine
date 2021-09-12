package OxyEngine.Core.Window;

import OxyEngine.Core.Context.Scene.SceneState;
import OxyEngine.System.Disposable;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Core.Context.Scene.SceneRuntime.sceneContext;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public final class Window implements Disposable {

    private final WindowMode mode;
    private final WindowSpecs specs;
    private int width;
    private int height;
    private final int[] x = new int[1], y = new int[1];
    private long pointer;
    private final String title;

    static final List<Event> eventPool = new ArrayList<>();
    private static final List<Event> allCreatedEvents = new ArrayList<>();

    public Window(String title, int width, int height, WindowMode mode, WindowSpecs specs) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.mode = mode;
        this.specs = specs;
    }

    public Window(String title, int width, int height, WindowMode mode) {
        this(title, width, height, mode, new WindowSpecs(GLFW_TRUE, GLFW_TRUE));
    }

    public void init() {
        glfwMakeContextCurrent(pointer);
        glfwShowWindow(pointer);

        glfwSetWindowSizeCallback(pointer, (window, width, height) -> {
            if (allCreatedEvents.stream().noneMatch(s -> s instanceof WindowEvent.WindowResizeEvent))
                allCreatedEvents.add(new WindowEvent.WindowResizeEvent());
            else {
                for (Event evt : allCreatedEvents) {
                    if (evt instanceof WindowEvent.WindowResizeEvent p) {
                        p.setWidth(width);
                        p.setHeight(height);
                        if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                    }
                }
            }
        });

        glfwSetWindowCloseCallback(pointer, window -> {
            if (allCreatedEvents.stream().noneMatch(s -> s instanceof WindowEvent.WindowCloseEvent))
                allCreatedEvents.add(new WindowEvent.WindowCloseEvent());
            else {
                for (Event evt : allCreatedEvents) {
                    if (evt instanceof WindowEvent.WindowCloseEvent p) {
                        if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                    }
                }
            }
        });

        glfwSetKeyCallback(pointer, (window, key, scancode, action, mods) -> {
            switch (action) {
                case GLFW_PRESS, GLFW_REPEAT -> {
                    if (allCreatedEvents.stream().noneMatch(s -> s instanceof KeyEvent.Press))
                        allCreatedEvents.add(new KeyEvent.Press());
                    for (Event evt : allCreatedEvents) {
                        if (evt instanceof KeyEvent.Press p) {
                            p.keyCode = KeyCode.getKeyCodeByValue(key);
                            if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                        }
                    }
                }

                case GLFW_RELEASE -> {
                    if (allCreatedEvents.stream().noneMatch(s -> s instanceof KeyEvent.Release))
                        allCreatedEvents.add(new KeyEvent.Release());
                    for (Event evt : allCreatedEvents) {
                        if (evt instanceof KeyEvent.Release p) {
                            p.keyCode = KeyCode.getKeyCodeByValue(key);
                            if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                        }
                    }
                }
            }
        });

        glfwSetMouseButtonCallback(pointer, (window, button, action, mods) -> {
            switch (action) {
                case GLFW_PRESS -> {
                    if (allCreatedEvents.stream().noneMatch(s -> s instanceof MouseEvent.Press))
                        allCreatedEvents.add(new MouseEvent.Press());
                    for (Event evt : allCreatedEvents) {
                        if (evt instanceof MouseEvent.Press p) {
                            p.mouseCode = MouseCode.getMouseCodeByValue(button);
                            if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                        }
                    }
                }

                case GLFW_RELEASE -> {
                    if (allCreatedEvents.stream().noneMatch(s -> s instanceof MouseEvent.Release))
                        allCreatedEvents.add(new MouseEvent.Release());
                    for (Event evt : allCreatedEvents) {
                        if (evt instanceof MouseEvent.Release p) {
                            p.mouseCode = MouseCode.getMouseCodeByValue(button);
                            if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                        }
                    }
                }
            }
        });

        glfwSetCharCallback(pointer, (window, codepoint) -> {
            if (allCreatedEvents.stream().noneMatch(s -> s instanceof KeyEvent.Typed))
                allCreatedEvents.add(new KeyEvent.Typed());
            for (Event evt : allCreatedEvents) {
                if (evt instanceof KeyEvent.Typed p) {
                    p.keyCode = KeyCode.getKeyCodeByValue(codepoint);
                    if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                }
            }
        });

        glfwSetCursorPosCallback(pointer, (window, xpos, ypos) -> {
            if (allCreatedEvents.stream().noneMatch(s -> s instanceof MouseEvent.Moved))
                allCreatedEvents.add(new MouseEvent.Moved());
            for (Event evt : allCreatedEvents) {
                if (evt instanceof MouseEvent.Moved p) {
                    p.x = (float) xpos;
                    p.y = (float) ypos;
                    if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                }
            }
        });

        glfwSetScrollCallback(pointer, (window, xoffset, yoffset) -> {
            if (allCreatedEvents.stream().noneMatch(s -> s instanceof MouseEvent.Scroll))
                allCreatedEvents.add(new MouseEvent.Scroll());
            for (Event evt : allCreatedEvents) {
                if (evt instanceof MouseEvent.Scroll p) {
                    p.xOffset = (float) xoffset;
                    p.yOffset = (float) yoffset;
                    if (sceneContext.getState() != SceneState.WAITING) eventPool.add(p);
                }
            }
        });
    }

    @Override
    public void dispose() {
        glfwFreeCallbacks(pointer);
        glfwDestroyWindow(pointer);
    }

    public record WindowSpecs(int resizable, int doubleBuffered) {
        public WindowSpecs() {
            this(GLFW_TRUE, GLFW_TRUE);
        }
    }

    public enum WindowMode {
        FULLSCREEN(), WINDOWEDFULLSCREEN(), WINDOWED()
    }

    public void update() {
        glfwSetWindowSizeLimits(pointer, 1366, 768, GLFW_DONT_CARE, GLFW_DONT_CARE);

        int[] w = new int[1];
        int[] h = new int[1];

        glfwGetWindowPos(pointer, x, y);
        glfwGetWindowSize(pointer, w, h);
        this.width = w[0];
        this.height = h[0];

        if (eventPool.size() != 0) eventPool.clear();
    }

    public static List<Event> getEventPool() {
        return eventPool;
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

    public int getX() {
        return x[0];
    }

    public int getY() {
        return y[0];
    }

    public String getTitle() {
        return title;
    }

    public WindowSpecs getSpecs() {
        return specs;
    }
}
