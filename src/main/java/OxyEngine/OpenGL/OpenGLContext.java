package OxyEngine.OpenGL;

import OxyEngine.Core.Window.WindowHandle;
import org.lwjgl.glfw.GLFWErrorCallback;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL13.*;

public class OpenGLContext {

    private OpenGLContext() {
    }

    public static void init(WindowHandle windowHandle) {
        GLFWErrorCallback.createPrint(System.err).set();
        glfwMakeContextCurrent(windowHandle.getPointer());
        glfwShowWindow(windowHandle.getPointer());

        createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
        logger.info("Renderer: " + glGetString(GL_RENDERER));
        logger.info("OpenGL version: " + glGetString(GL_VERSION));
        logger.info("Graphics Card Vendor: " + glGetString(GL_VENDOR));
        logger.info("OpenGL Context running on " + Thread.currentThread().getName() + " Thread");
    }
}
