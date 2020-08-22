package OxyEngine.Events;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;

import java.util.*;

public final class OxyEventDispatcher {

    private static final Map<OxyEntity, List<OxyEventListener>> listeners = new HashMap<>();

    public OxyEventDispatcher() {
    }

    public void addDispatchersToThread(OxyEntity entity, OxyEventListener listener) {
        if (!listeners.containsKey(entity))
            listeners.put(entity, new ArrayList<>());
        if (!listeners.get(entity).contains(listener))
            listeners.get(entity).add(listener);
    }

    public void dispatch() {
        for (var entrySet : listeners.entrySet()) {
            OxyEntity entity = entrySet.getKey();
            List<OxyEventListener> listeners = entrySet.getValue();
            for (OxyEventListener l : listeners) {
                if (l instanceof OxyMouseListener m) {
                    if (OxyRenderer.currentBoundedCamera != null) {
                        m.dispatch(entity);
                    }
                }
            }
        }
    }
}
