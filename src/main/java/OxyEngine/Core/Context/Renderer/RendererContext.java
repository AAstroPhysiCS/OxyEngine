package OxyEngine.Core.Context.Renderer;

import OxyEngine.Core.Window.Window;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.logger;

public abstract class RendererContext {

    public static TargetPlatform selectedPlatform;

    public static RendererContext getContext(TargetPlatform platform) {
        selectedPlatform = platform;
        if (platform == TargetPlatform.OpenGL) {
            return new OpenGLContext();
        }
        logger.severe("Vulkan not yet supported");
        System.exit(-1);
        return null;
    }

    public abstract void init();

    public abstract void swapBuffer(Window handle);

    public abstract void pollEvents();

    protected abstract void enable(int id);

    protected abstract void disable(int id);

    public abstract void depthFunc(int depthFunc);

    public abstract String getString(int id);

    public abstract void bindTextureUnit(int textureSlot, int textureID);

    public abstract void enableDepth();

    public abstract void disableDepth();
}
