package OxyEngine.Core.Context.Renderer;

import OxyEngine.Core.Context.Renderer.Texture.Color;
import OxyEngine.TargetPlatform;

import static OxyEngine.System.OxySystem.logger;

public abstract class RendererAPI {

    static RenderPass onStackRenderPass = null; //for detecting if there's any render passes bounded

    public static RendererAPI getContext(TargetPlatform API) {
        if (API == TargetPlatform.OpenGL) {
            return new OpenGLRendererAPI();
        }
        logger.severe("Vulkan not yet supported");
        System.exit(-1);
        return null;
    }

    public abstract void clearColor(Color clearColor);

    public abstract void clearBuffer();

    public abstract void init(boolean debug);

    public abstract void beginRenderPass(RenderPass renderPass);

    public abstract void endRenderPass();

    public abstract void drawArrays(int modeID, int first, int count);

    public abstract void drawElements(int modeID, int size, int type, int indices);

    public abstract void drawElementsIndexed(int modeID, int size, int type, int baseIndex, int baseVertex);

    public abstract void readBuffer(int attachment);

    public abstract void readPixels(int x, int y, int width, int height, int format, int type, int[] pixels);

    public abstract void clearTexImage(int texture, int level, int format, int type, int[] data);

    public abstract void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format);

    public abstract void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ);

    public abstract void memoryBarrier(int barriers);
}
