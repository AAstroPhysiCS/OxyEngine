package OxyEngine.Core.Renderer.Context;

public class OxyRenderCommand {

    public static RendererContext rendererContext;
    public static RendererAPI rendererAPI;

    public static OxyRenderCommand INSTANCE = null;

    public static OxyRenderCommand getInstance(RendererContext context, RendererAPI API){
        if(INSTANCE == null) INSTANCE = new OxyRenderCommand(context, API);
        return INSTANCE;
    }

    private OxyRenderCommand(RendererContext context, RendererAPI API){
        OxyRenderCommand.rendererContext = context;
        OxyRenderCommand.rendererAPI = API;
    }

    public static void init(boolean debug){
        rendererContext.init();
        rendererAPI.init(debug);
    }
}
