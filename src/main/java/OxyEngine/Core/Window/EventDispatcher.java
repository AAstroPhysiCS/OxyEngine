package OxyEngine.Core.Window;

public final class EventDispatcher {

    private static EventDispatcher INSTANCE = null;

    public static EventDispatcher getInstance() {
        if (INSTANCE == null) INSTANCE = new EventDispatcher();
        return INSTANCE;
    }

    private EventDispatcher() {
    }

    public <U extends T, T extends Event> void dispatch(Class<U> desiredEventClass, T event, EventFunc<U> eventFunc) {
        if (desiredEventClass.equals(event.getClass()))
            eventFunc.func((U) event);
    }
}
