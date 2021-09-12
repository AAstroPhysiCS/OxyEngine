package OxyEngine;

import OxyEngine.Core.Layers.LayerStack;
import OxyEngine.Core.Window.Window;
import OxyEngine.System.Disposable;

public abstract class OxyApplication implements Disposable {

    protected final LayerStack layerStack; // every app should have a layer stack
    protected OxyEngine oxyEngine;
    protected Window window;

    public OxyApplication() {
        layerStack = new LayerStack();
    }

    public abstract void start();

    protected abstract void init();

    protected abstract void update(float ts);

    protected abstract Runnable run();
}
