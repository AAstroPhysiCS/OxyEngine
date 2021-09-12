package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.Event;
import OxyEngine.Core.Window.EventDispatcher;

public abstract class Layer {

    protected final EventDispatcher eventDispatcher = EventDispatcher.getInstance();

    public abstract void update(float ts);

    public abstract void onEvent(Event event);
}
