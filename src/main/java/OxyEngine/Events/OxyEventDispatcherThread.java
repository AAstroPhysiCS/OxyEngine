package OxyEngine.Events;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OxyEventDispatcherThread extends Thread {

    private static boolean running = false;

    private static final Map<OxyEntity, List<OxyEventListener>> listeners = new ConcurrentHashMap<>();

    public OxyEventDispatcherThread() {
        this.setName("OxyEventDispatcher - Thread");
    }

    public void addDispatchersToThread(OxyEntity entity, OxyEventListener listener) {
        if (!listeners.containsKey(entity))
            listeners.put(entity, new ArrayList<>());
        if (!listeners.get(entity).contains(listener))
            listeners.get(entity).add(listener);
    }

    public void startThread() {
        running = true;
        this.start();
    }

    public void joinThread() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
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
            try {
                Thread.sleep(1000 / 144);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
