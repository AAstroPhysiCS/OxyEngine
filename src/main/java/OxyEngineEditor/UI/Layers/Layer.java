package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Scene;

public abstract class Layer {

    protected final WindowHandle windowHandle;
    protected final Scene scene;

    public Layer(WindowHandle windowHandle, Scene scene) {
        this.windowHandle = windowHandle;
        this.scene = scene;
    }

    public abstract void preload();

    public abstract void renderLayer();
}
