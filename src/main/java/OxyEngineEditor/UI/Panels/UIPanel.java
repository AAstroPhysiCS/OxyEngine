package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Window.WindowHandle;

public abstract class UIPanel extends Panel {

    public static final float[] bgC = new float[]{41 / 255f, 41 / 255f, 41 / 255f, 1.0f};

    public UIPanel(WindowHandle windowHandle) {
        super(windowHandle);
    }

    @Override
    public abstract void preload();

    @Override
    public abstract void renderPanel();
}
