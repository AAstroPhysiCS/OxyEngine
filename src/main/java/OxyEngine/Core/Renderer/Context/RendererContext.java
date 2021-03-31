package OxyEngine.Core.Renderer.Context;

import OxyEngine.TargetPlatform;
import OxyEngine.Core.Window.WindowHandle;

import static OxyEngine.System.OxySystem.logger;

public abstract class RendererContext {

    public static TargetPlatform selectedPlatform;

    public static RendererContext getContext(TargetPlatform platform){
        selectedPlatform = platform;
        if(platform == TargetPlatform.OpenGL){
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
