package OxyEngineEditor.UI;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Window.WindowHandle;

public abstract class UILayer implements RenderableUI {

    protected final WindowHandle windowHandle;

    protected final OxyRenderer currentRenderer;

    protected static final float[] bgC = new float[]{41 / 255f, 41 / 255f, 41 / 255f, 1.0f};

    public UILayer(WindowHandle windowHandle, OxyRenderer currentRenderer){
        this.windowHandle = windowHandle;
        this.currentRenderer = currentRenderer;
    }

    public abstract void preload();

    @Override
    public abstract void renderLayer();
}
