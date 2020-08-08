package OxyEngineEditor.UI.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Sandbox.Scene.Scene;

public abstract class UILayer extends Layer {

    public static final float[] bgC = new float[]{41 / 255f, 41 / 255f, 41 / 255f, 1.0f};

    public UILayer(WindowHandle windowHandle, Scene scene) {
        super(windowHandle, scene);
    }

    @Override
    public abstract void preload();

    @Override
    public abstract void renderLayer();
}
