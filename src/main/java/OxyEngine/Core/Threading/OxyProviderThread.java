package OxyEngine.Core.Threading;

import java.util.concurrent.ConcurrentLinkedDeque;

public class OxyProviderThread<T extends OxyProvider> {

    private Thread worker;
    private final ConcurrentLinkedDeque<T> providers = new ConcurrentLinkedDeque<>();

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

    //** FOR STOP(), SUSPEND(), RESUME() METHODS **
    //deprecated, should not be used... it is being used because you cannot directly kill a thread immediately with wait(), notify() methods.
    //with methods wait() and notify() you can stop a thread but the implementation would be horrible for long action processes (for example 3 for loops).

    @SuppressWarnings("deprecation")
    public void shutdown() {
        worker.checkAccess();
        worker.stop();
        try {
            worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("removal")
    public void restart() {
        worker.checkAccess();
        worker.resume();
    }

    @SuppressWarnings("removal")
    public void stop() {
        worker.checkAccess();
        worker.suspend();
    }

    public ConcurrentLinkedDeque<T> getProviders() {
        return providers;
    }

    public void addProvider(T provider) {
        if(!providers.contains(provider)) providers.add(provider);
    }

    public void removeProvider(T provider){
        providers.remove(provider);
    }

    public boolean isWorking() {
        return worker.isAlive();
    }
}