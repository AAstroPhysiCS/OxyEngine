package OxyEngineEditor.UI;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Scene;

public abstract class UILayer implements RenderableUI {

    protected final WindowHandle windowHandle;

    protected final Scene scene;

    protected static final float[] bgC = new float[]{41 / 255f, 41 / 255f, 41 / 255f, 1.0f};

    public UILayer(WindowHandle windowHandle, Scene scene) {
        this.windowHandle = windowHandle;
        this.scene = scene;
    }

    public abstract void preload();

    @Override
    public abstract void renderLayer();
}
