package OxyEngine.Core.Window;

import OxyEngine.Func;

public final class OxyEventDispatcher {

    private static OxyEventDispatcher INSTANCE = null;

    public static OxyEventDispatcher getInstance() {
        if (INSTANCE == null) INSTANCE = new OxyEventDispatcher();
        return INSTANCE;
    }

    private OxyEventDispatcher() {
    }

    public <U extends T, T extends OxyEvent> void dispatch(Class<U> desiredEventClass, T event, Func<U> func) {
        if (desiredEventClass.equals(event.getClass()))
            func.func((U) event);
    }
}
