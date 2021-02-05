package OxyEngineEditor.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.EntryPoint;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.SceneRuntimeControlPanel;

import static OxyEngine.Scripting.OxyScript.scriptThread;

public final class SceneRuntime {

    private static final SceneRuntimeControlPanel panel = new SceneRuntimeControlPanel();

    public static OxyCamera currentBoundedCamera;
    public static Scene ACTIVE_SCENE;

    public static float TS = 0;

    private SceneRuntime() {
    }

    public static Object loadClass(String classBinName, Scene scene, OxyEntity entity){
        try {
            return EntryPoint.class.getClassLoader().loadClass(classBinName).getDeclaredConstructor(Scene.class, OxyEntity.class).newInstance(scene, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void onCreate() {
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                c.invokeCreate();
            }
        }
    }

    public static void onUpdate(float ts) {
        TS = ts;
        if (ACTIVE_SCENE.STATE != SceneState.RUNNING) return;
        for (OxyEntity e : SceneLayer.getInstance().allModelEntities) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                scriptThread.addProvider(c.getProvider());
            }
        }
    }

    public static void stop() {
        ACTIVE_SCENE.STATE = SceneState.STOPPED;
        scriptThread.stop();
    }

    public static void resume() {
        ACTIVE_SCENE.STATE = SceneState.RUNNING;
        scriptThread.restart();
    }

    public static void dispose() {
        ACTIVE_SCENE.STATE = SceneState.TERMINATED;
        if(scriptThread != null){
            scriptThread.shutdown();
            scriptThread = null;
        }
        ACTIVE_SCENE.dispose();
    }

    public static SceneRuntimeControlPanel getPanel() {
        return panel;
    }
}