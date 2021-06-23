package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.OxyEvent;
import OxyEngine.Core.Window.OxyEventDispatcher;

public abstract class Layer {

    protected final OxyEventDispatcher eventDispatcher = OxyEventDispatcher.getInstance();

    public abstract void build();

    public abstract void update(float ts);

    public abstract void render();

    public abstract void onEvent(OxyEvent event);
}
