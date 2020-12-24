package OxyEngine.Core.Renderer.Context;

import OxyEngine.Core.Renderer.OxyRendererPlatform;

import static OxyEngine.System.OxySystem.logger;

public abstract class RendererAPI {

    public static RendererAPI getContext(OxyRendererPlatform API){
        if(API == OxyRendererPlatform.OpenGL){
            return new OpenGLRendererAPI();
        }
        logger.severe("Vulkan not yet supported");
        System.exit(-1);
        return null;
    }

    public abstract void clearColor(float r, float g, float b, float a);

    public abstract void clearBuffer();

    abstract void init(boolean debug);
}
