package OxyEngine.Core.Context;

import OxyEngine.Core.Window.WindowHandle;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL13.*;

public class OpenGLContext extends RendererContext {

    OpenGLContext() {}

    @Override
    public void init() {
        createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL); //for skybox

        logger.info("Renderer: " + glGetString(GL_RENDERER));
        logger.info("OpenGL version: " + glGetString(GL_VERSION));
        logger.info("Graphics Card Vendor: " + glGetString(GL_VENDOR));
        logger.info("OpenGL Context running on " + Thread.currentThread().getName() + " Thread");
    }

    @Override
    public void swapBuffer(WindowHandle handle) {
        glfwSwapBuffers(handle.getPointer());
    }

    @Override
    public void pollEvents() {
        glfwPollEvents();
    }
}
