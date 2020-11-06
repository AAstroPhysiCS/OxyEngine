package OxyEngine.Events;

import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.SceneRuntime;

import java.util.*;

public final class OxyEventDispatcher {

    private static final Map<OxyEventListener, Set<OxyEntity>> listeners = new HashMap<>();

    public OxyEventDispatcher() {
    }

    public void addListeners(OxyEntity entity, OxyEventListener listener) {
        if (!listeners.containsKey(listener))
            listeners.put(listener, new LinkedHashSet<>());
        listeners.get(listener).add(entity);
    }

    public void dispatch() {
        for (var entrySet : listeners.entrySet()) {
            OxyEventListener listener = entrySet.getKey();
            Set<OxyEntity> entities = entrySet.getValue();
            if (listener instanceof OxyMouseListener m && SceneRuntime.currentBoundedCamera != null) {
                m.dispatch(entities);
            }
        }
    }
}
