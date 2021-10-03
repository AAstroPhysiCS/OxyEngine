package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.Event;
import OxyEngine.Core.Window.EventDispatcher;
import OxyEngine.System.Disposable;

public abstract class Layer implements Disposable {

    protected final EventDispatcher eventDispatcher = EventDispatcher.getInstance();

    public abstract void update(float ts);

    public abstract void onEvent(Event event);
}
