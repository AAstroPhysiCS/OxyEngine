package OxyEngine.Scripting;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class OxyProviderThread<T extends OxyProvider> {

    private Thread worker;
    private final ConcurrentLinkedDeque<T> providers = new ConcurrentLinkedDeque<>();
    final Object stopLock = new Object();
    final AtomicBoolean stop = new AtomicBoolean();
    final AtomicBoolean dispose = new AtomicBoolean();

    public OxyProviderThread(Runnable r, String name) {
        worker = new Thread(r, name);
    }

    public OxyProviderThread() {
    }

    public void setTarget(Runnable r) {
        worker = new Thread(r);
    }

    public void start() {
        worker.start();
    }

    public void shutdown() {
        stop.set(false);
        synchronized (stopLock) {
            stopLock.notifyAll();
        }
        dispose.set(true);
        try {
            worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        stop.set(false);
        synchronized (stopLock) {
            stopLock.notify();
        }
    }

    public void stop() {
        stop.set(true);
    }

    public ConcurrentLinkedDeque<T> getProviders() {
        return providers;
    }

    public void addProvider(T provider) {
        if (!providers.contains(provider)) providers.add(provider);
    }

    public void removeProvider(T provider) {
        providers.remove(provider);
    }

    public boolean isWorking() {
        return worker.isAlive();
    }
}