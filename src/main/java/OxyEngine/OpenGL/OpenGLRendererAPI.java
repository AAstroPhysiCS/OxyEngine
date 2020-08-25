package OxyEngine.OpenGL;

import OxyEngine.Core.Window.WindowHandle;

import static OxyEngine.System.Globals.Globals.normalizeColor;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class OpenGLRendererAPI {

    private OpenGLRendererAPI() {
    }

    public static void clearColor(float r, float g, float b, float a) {
        if (r > 1 || b > 1 || g > 1 || a > 1) {
            r = normalizeColor(r);
            g = normalizeColor(g);
            b = normalizeColor(b);
            a = normalizeColor(a);
        }
        glClearColor(r, g, b, a);
    }

    public static void clearBuffer() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void swapBuffer(WindowHandle handle) {
        glfwSwapBuffers(handle.getPointer());
    }

    public static void pollEvents() {
        glfwPollEvents();
    }
}
