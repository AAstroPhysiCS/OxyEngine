package OxyEngine.Core.Window;

public final class OxyEventDispatcher {

    private static OxyEventDispatcher INSTANCE = null;

    public static OxyEventDispatcher getInstance() {
        if (INSTANCE == null) INSTANCE = new OxyEventDispatcher();
        return INSTANCE;
    }

    private OxyEventDispatcher() {
    }

    public <U extends T, T extends OxyEvent> void dispatch(Class<U> desiredEventClass, T event, EventFunc<U> eventFunc) {
        if (desiredEventClass.equals(event.getClass()))
            eventFunc.func((U) event);
    }
}
