package OxyEngine.Core.Window;

import OxyEngine.OxyEngine;

import static org.lwjgl.glfw.GLFW.*;

public class WindowHint {

    private int resizable, doubleBuffered = 1;
    private OxyEngine.Antialiasing antiAliasing;

    public WindowHint resizable(int resizable) {
        this.resizable = resizable;
        return this;
    }

    public WindowHint antiAliasing(OxyEngine.Antialiasing antialiasing) {
        this.antiAliasing = antialiasing;
        return this;
    }

    public WindowHint doubleBuffered(int doubleBuffered){
        this.doubleBuffered = doubleBuffered;
        return this;
    }

    public void create(){
        glfwWindowHint(GLFW_RESIZABLE, resizable);
        glfwWindowHint(GLFW_SAMPLES, antiAliasing.getLevel());
        glfwWindowHint(GLFW_DOUBLEBUFFER, doubleBuffered);
    }
}
