package OxyEngine.Core.Renderer.Context;

import OxyEngine.Core.Renderer.OxyRendererPlatform;
import OxyEngine.Core.Window.WindowHandle;

import static OxyEngine.System.OxySystem.logger;

public abstract class RendererContext {

    public static OxyRendererPlatform selectedPlatform;

    public static RendererContext getContext(OxyRendererPlatform platform){
        selectedPlatform = platform;
        if(platform == OxyRendererPlatform.OpenGL){
            return new OpenGLContext();
        }
        logger.severe("Vulkan not yet supported");
        System.exit(-1);
        return null;
    }

    abstract void init();

    public abstract void swapBuffer(WindowHandle handle);

    public abstract void pollEvents();
}
