package OxyEngine.Core.Window;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.glfw.GLFW.*;

public class WindowHint {

    private int resizable, doubleBuffered = 1;
    private int redBits, greenBits, blueBits;
    private int refreshRate;

    public WindowHint resizable(int resizable) {
        this.resizable = resizable;
        return this;
    }

    public WindowHint doubleBuffered(int doubleBuffered) {
        this.doubleBuffered = doubleBuffered;
        return this;
    }

    public WindowHint setRedBits(int redBits){
        this.redBits = redBits;
        return this;
    }

    public WindowHint setGreenBits(int greenBits){
        this.greenBits = greenBits;
        return this;
    }

    public WindowHint setBlueBits(int blueBits){
        this.blueBits = blueBits;
        return this;
    }

    public WindowHint setRefreshRate(int refreshRate){
        this.refreshRate = refreshRate;
        return this;
    }

    public WindowHint colorBitsSetDefault(){
        long glfwMonitor = glfwGetPrimaryMonitor();
        var glfwMode = glfwGetVideoMode(glfwMonitor);
        assert glfwMode != null : oxyAssert("No Main Monitor!");
        this.redBits = glfwMode.redBits();
        this.greenBits = glfwMode.greenBits();
        this.blueBits = glfwMode.blueBits();
        this.refreshRate = glfwMode.refreshRate();
        return this;
    }

    public WindowHint create() {
        glfwWindowHint(GLFW_RESIZABLE, resizable);
        glfwWindowHint(GLFW_DOUBLEBUFFER, doubleBuffered);

        glfwWindowHint(GLFW_RED_BITS, redBits);
        glfwWindowHint(GLFW_GREEN_BITS, greenBits);
        glfwWindowHint(GLFW_BLUE_BITS, blueBits);
        glfwWindowHint(GLFW_REFRESH_RATE, refreshRate);
        return this;
    }
}
