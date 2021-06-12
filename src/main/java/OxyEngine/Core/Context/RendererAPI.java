package OxyEngine.Core.Context;

import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.logger;

public abstract class RendererAPI {

    static OxyRenderPass onStackRenderPass = null; //for detecting if there's any render passes bounded

    public static RendererAPI getContext(TargetPlatform API) {
        if (API == TargetPlatform.OpenGL) {
            return new OpenGLRendererAPI();
        }
        logger.severe("Vulkan not yet supported");
        System.exit(-1);
        return null;
    }

    public abstract void clearColor(OxyColor clearColor);

    public abstract void clearBuffer();

    public abstract void init(boolean debug);

    public abstract void beginRenderPass(OxyRenderPass renderPass);

    public abstract void endRenderPass();
}
