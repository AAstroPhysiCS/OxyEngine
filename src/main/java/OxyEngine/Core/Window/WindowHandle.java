package OxyEngine.Core.Window;

import OxyEngine.System.OxyDisposable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class WindowHandle implements OxyDisposable {

    private final WindowMode mode;
    private final WindowSpecs specs;
    private int width;
    private int height;
    private long pointer;
    private final String title;

    public WindowHandle(String title, int width, int height, WindowMode mode, WindowSpecs specs) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.mode = mode;
        this.specs = specs;
    }

    public WindowHandle(String title, int width, int height, WindowMode mode) {
        this(title, width, height, mode, new WindowSpecs(GLFW_TRUE, GLFW_TRUE));
    }

    public void init() {
        glfwMakeContextCurrent(pointer);
        glfwShowWindow(pointer);
    }

    @Override
    public void dispose() {
        glfwFreeCallbacks(pointer);
        glfwDestroyWindow(pointer);
    }

    public record WindowSpecs(int resizable, int doubleBuffered){
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

        glfwGetWindowSize(pointer, w, h);
        this.width = w[0];
        this.height = h[0];
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

    public WindowSpecs getSpecs() {
        return specs;
    }
}
