package OxyEngine.Core.Window;

import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

public interface WindowBuilder {

    class WindowFactory implements WindowBuilder {
        @Override
        public long createOpenGLWindow(int width, int height, String title) {
            return glfwCreateWindow(width, height, title, 0, 0);
        }

        public long createWindowedFullscreenOpenGLWindow(String title) {
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if(vidMode != null) {
                glfwWindowHint(GLFW_MAXIMIZED, GL_TRUE);
                return glfwCreateWindow(vidMode.width(), vidMode.height(), title, 0, 0);
            }
            return 0;
        }

        public long createFullscreenOpenGLWindow(String title) {
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            assert vidMode != null;
            return glfwCreateWindow(vidMode.width(), vidMode.height(), title, glfwGetPrimaryMonitor(), 0);
        }

        @Override
        public WindowHint createHints() {
            return new WindowHint();
        }
    }

    long createOpenGLWindow(int width, int height, String title);

    long createWindowedFullscreenOpenGLWindow(String title);

    long createFullscreenOpenGLWindow(String title);

    WindowHint createHints();
}
