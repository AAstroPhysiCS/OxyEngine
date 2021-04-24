package OxyEngine.Scripting;

import static OxyEngine.Scene.SceneRuntime.TS;

public final class ScriptEngine {

    public static OxyProviderThread<OxyScript.EntityInfoProvider> scriptThread = new OxyProviderThread<>();
    private static final Object lock = new Object();

    static {
        scriptThread.setTarget(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (var providerF : scriptThread.getProviders()) {
                    providerF.invokeUpdate(TS);
                }
            }
        });
        scriptThread.start();
    }

    private ScriptEngine() {
    }

    public static synchronized void notifyLock() {
        if (scriptThread.getProviders().size() == 0) return;
        synchronized (lock) {
            lock.notify();
        }
    }

    public static synchronized void onUpdate() {
        if (!scriptThread.isWorking()) {
            Thread.dumpStack();
            throw new IllegalStateException("Unexpected Thread State");
        }
    }

    public static void clearProviders() {
        scriptThread.getProviders().clear();
    }

    public static synchronized void stop() {
        scriptThread.stop();
    }

    public static synchronized void restart() {
        scriptThread.restart();
    }

    public static synchronized void addProvider(OxyScript.EntityInfoProvider provider) {
        if (!scriptThread.getProviders().contains(provider)) scriptThread.addProvider(provider);
    }

    public static synchronized void removeProvider(OxyScript.EntityInfoProvider provider) {
        scriptThread.removeProvider(provider);
    }

    public static void dispose() {
        if (scriptThread != null) {
            clearProviders();
            scriptThread.shutdown();
            scriptThread = null;
        }
    }
}
