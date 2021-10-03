package OxyEngine.Scripting;

import static OxyEngine.Core.Renderer.Renderer.TS;

public final class ScriptEngine {

    private static ProviderThread<EntityInfoProvider> scriptThread = new ProviderThread<>();
    private static final Object runtimeLock = new Object();

    static {
        scriptThread.setTarget(() -> {
            while (!scriptThread.dispose.get()) {
                synchronized (runtimeLock) {
                    try {
                        runtimeLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (scriptThread.stop.get()) {
                    synchronized (scriptThread.stopLock) {
                        try {
                            scriptThread.stopLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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

    public static synchronized void onUpdate() {
        if (scriptThread.getProviders().size() == 0) return;
        synchronized (runtimeLock) {
            runtimeLock.notify();
        }

        if (!scriptThread.isWorking()) {
            scriptThread.dumpStack();
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

    public static synchronized void addProvider(EntityInfoProvider provider) {
        if (!scriptThread.getProviders().contains(provider)) scriptThread.addProvider(provider);
    }

    public static synchronized void removeProvider(EntityInfoProvider provider) {
        scriptThread.removeProvider(provider);
    }

    public static void dispose() {
        if (scriptThread != null) {
            //cant call locknotify method bcs it checks if there's any active script running
            //but for a clean thread shutdown, i shouldn't consider it if there's any script running (otherwise, no clean shutdown)
            synchronized (runtimeLock) {
                runtimeLock.notify();
            }
            scriptThread.shutdown();
            clearProviders();
            scriptThread = null;
        }
    }
}
