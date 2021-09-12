package OxyEngine.Core.Context.Renderer;

import OxyEngine.Core.Window.Window;

import static OxyEngine.System.OxySystem.logger;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public final class OpenGLContext extends RendererContext {

    OpenGLContext() {
    }

    @Override
    public void init() {
        createCapabilities();
        enable(GL_DEPTH_TEST);
        enable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        depthFunc(GL_LEQUAL); //for skybox

        logger.info("Renderer: " + getString(GL_RENDERER));
        logger.info("OpenGL version: " + getString(GL_VERSION));
        logger.info("Graphics Card Vendor: " + getString(GL_VENDOR));
        logger.info("OpenGL Context running on " + Thread.currentThread().getName() + " Thread");
    }

    @Override
    public void swapBuffer(Window handle) {
        glfwSwapBuffers(handle.getPointer());
    }

    @Override
    public void pollEvents() {
        glfwPollEvents();
    }

    @Override
    public void enable(int id) {
        glEnable(id);
    }

    @Override
    public void disable(int id){
        glDisable(id);
    }

    @Override
    public void depthFunc(int depthFunc) {
        glDepthFunc(depthFunc);
    }

    @Override
    public String getString(int id) {
        return glGetString(id);
    }

    @Override
    public void bindTextureUnit(int textureSlot, int textureID) {
        glBindTextureUnit(textureSlot, textureID);
    }

    @Override
    public void enableDepth() {
        enable(GL_DEPTH_TEST);
    }

    @Override
    public void disableDepth() {
        disable(GL_DEPTH_TEST);
    }
}
