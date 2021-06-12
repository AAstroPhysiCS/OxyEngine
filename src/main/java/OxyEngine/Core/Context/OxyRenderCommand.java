package OxyEngine.Core.Context;

import OxyEngine.TargetPlatform;

public class OxyRenderCommand {

    private final RendererContext rendererContext;
    private final RendererAPI rendererAPI;

    private static OxyRenderCommand INSTANCE = null;

    static TargetPlatform targetPlatform;

    public static OxyRenderCommand getInstance(TargetPlatform targetPlatform){
        OxyRenderCommand.targetPlatform = targetPlatform;
        if(INSTANCE == null) {
            INSTANCE = new OxyRenderCommand(RendererContext.getContext(targetPlatform), RendererAPI.getContext(targetPlatform));
            return INSTANCE;
        }
        throw new IllegalStateException("API not supported yet!");
    }

    private OxyRenderCommand(RendererContext context, RendererAPI API){
        this.rendererContext = context;
        this.rendererAPI = API;
    }

    void init(boolean debug){
        rendererContext.init();
        rendererAPI.init(debug);
    }

    RendererAPI getRendererAPI() {
        return rendererAPI;
    }

    RendererContext getRendererContext() {
        return rendererContext;
    }
}
