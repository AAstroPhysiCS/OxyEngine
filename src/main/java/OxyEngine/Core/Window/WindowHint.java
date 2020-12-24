package OxyEngine.Core.Window;

import static org.lwjgl.glfw.GLFW.*;

public class WindowHint {

    private int resizable, doubleBuffered = 1;

    public WindowHint resizable(int resizable) {
        this.resizable = resizable;
        return this;
    }

    public WindowHint doubleBuffered(int doubleBuffered) {
        this.doubleBuffered = doubleBuffered;
        return this;
    }

    public WindowHint create() {
        glfwWindowHint(GLFW_RESIZABLE, resizable);
        glfwWindowHint(GLFW_DOUBLEBUFFER, doubleBuffered);
        return this;
    }
}
