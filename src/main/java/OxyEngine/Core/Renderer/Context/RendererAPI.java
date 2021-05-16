package OxyEngine.Core.Renderer.Context;

import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.logger;

public abstract class RendererAPI {

    public static RendererAPI getContext(TargetPlatform API){
        if(API == TargetPlatform.OpenGL){
            return new OpenGLRendererAPI();
        }
        logger.severe("Vulkan not yet supported");
        System.exit(-1);
        return null;
    }

    public abstract void clearColor(float r, float g, float b, float a);

    public abstract void clearBuffer();

    public abstract void init(boolean debug);
}
