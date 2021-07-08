package OxyEngine;

import OxyEngine.Core.Layers.LayerStack;
import OxyEngine.Core.Window.OxyWindow;
import OxyEngine.System.OxyDisposable;

public abstract class OxyApplication implements OxyDisposable {

    protected final LayerStack layerStack; // every app should have a layer stack
    protected OxyEngine oxyEngine;
    protected OxyWindow oxyWindow;

    public OxyApplication() {
        layerStack = new LayerStack();
    }

    public abstract void start();

    protected abstract void init();

    protected abstract void update();

    protected abstract void render(float ts);

    protected abstract Runnable run();
}
