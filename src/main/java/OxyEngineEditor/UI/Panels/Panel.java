package OxyEngineEditor.UI.Panels;

import OxyEngine.Core.Window.WindowHandle;

public abstract class Panel {

    protected final WindowHandle windowHandle;

    public Panel(WindowHandle windowHandle) {
        this.windowHandle = windowHandle;
    }

    public abstract void preload();

    public abstract void renderPanel();
}
