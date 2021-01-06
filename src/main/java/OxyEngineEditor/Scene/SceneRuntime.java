package OxyEngineEditor.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.SceneRuntimeControlPanel;

import static OxyEngine.Scripting.OxyScript.scriptThread;

public final class SceneRuntime {

    private static final SceneRuntimeControlPanel panel = new SceneRuntimeControlPanel();

    public static OxyCamera currentBoundedCamera;
    public static Scene ACTIVE_SCENE;

    public static float TS = 0;

    public SceneRuntime(Scene scene) {
        ACTIVE_SCENE = scene;
    }

    public static void onCreate() {
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                OxyScript.EntityInfoProvider provider = c.getProvider();
                if (provider == null) continue;
                provider.invokeCreate();
            }
        }
    }

    public static void onUpdate(float ts) {
        TS = ts;
        if (ACTIVE_SCENE.STATE != SceneState.RUNNING) return;
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                OxyScript.EntityInfoProvider provider = c.getProvider();
                if (provider == null) continue;
                scriptThread.addProvider(c);
            }
        }
    }

    public static void stop() {
        ACTIVE_SCENE.STATE = SceneState.STOPPED;
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                if (c.getOxySubThread() != null) c.getOxySubThread().stop();
            }
        }
    }

    public static void resume() {
        ACTIVE_SCENE.STATE = SceneState.RUNNING;
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                if (c.getOxySubThread() != null) c.getOxySubThread().restart();
            }
        }
    }

    public static void dispose() {
        ACTIVE_SCENE.STATE = SceneState.TERMINATED;
        if(scriptThread != null){
            scriptThread.shutdown();
            scriptThread = null;
        }
        ACTIVE_SCENE.getFrameBuffer().dispose();
    }

    public static SceneRuntimeControlPanel getPanel() {
        return panel;
    }
}
