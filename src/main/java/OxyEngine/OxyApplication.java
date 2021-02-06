package OxyEngine;

import OxyEngine.Core.Layers.LayerStack;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.System.OxyDisposable;
import OxyEngine.Scene.Scene;

public abstract class OxyApplication implements OxyDisposable {

    protected final LayerStack layerStack; // every app should have a layer stack
    protected Scene scene;
    protected OxyEngine oxyEngine;
    protected WindowHandle windowHandle;

    public OxyApplication(){
        layerStack = new LayerStack();
    }

    public static float FPS = 0;

    public abstract void init();

    public abstract void update(float ts);

    public abstract void render(float ts);

    protected abstract Runnable run();
}
