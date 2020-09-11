package OxyEngineEditor.Scene;

import OxyEngine.System.OxyEntitySystem;

public class OxyEntitySystemRunner {

//    private static final ExecutorService executor = Executors.newSingleThreadExecutor(); Idk about it, does make a lill performance difference though

    private final Scene scene;

    OxyEntitySystemRunner(Scene scene) {
        this.scene = scene;
    }

    public void run() {
        for (OxyEntity s : scene.getEntities()) {
            if (!s.has(OxyEntitySystem.class)) continue;
            s.get(OxyEntitySystem.class).run();
        }
    }

    /*@Override
    public void dispose() {
        executor.shutdown();
    }*/
}
