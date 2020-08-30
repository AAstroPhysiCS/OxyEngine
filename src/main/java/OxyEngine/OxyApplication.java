package OxyEngine;

import OxyEngine.Core.Layers.LayerStack;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Scene.Scene;

public abstract class OxyApplication implements OxyDisposable {

    protected LayerStack layerStack; // every app should have a layer stack
    protected Scene scene;
    protected OxyEngine oxyEngine;

    public static int FPS = 0;

    public abstract void init();

    public abstract void update(float ts, float deltaTime);

    public abstract void render(float ts, float deltaTime);

    protected abstract Runnable run();
}
