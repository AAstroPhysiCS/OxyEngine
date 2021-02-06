package OxyEngine.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Threading.OxyProviderThread;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.EntryPoint;
import OxyEngineEditor.UI.Panels.SceneRuntimeControlPanel;

public final class SceneRuntime {

    private static final SceneRuntimeControlPanel panel = new SceneRuntimeControlPanel();

    public static OxyCamera currentBoundedCamera;
    public static Scene ACTIVE_SCENE;

    public static OxyProviderThread<OxyScript.EntityInfoProvider> scriptThread = new OxyProviderThread<>();

    public static float TS = 0;

    static {
        scriptThread.setTarget(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                for (var providerF : scriptThread.getProviders()) providerF.invokeUpdate(TS);
            }
        });
        scriptThread.start();
    }

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
        for (OxyEntity e : SceneLayer.getInstance().allModelEntities) {
            for (OxyScript c : e.getScripts()) {
                c.invokeCreate();
                if(!scriptThread.getProviders().contains(c.getProvider())) scriptThread.addProvider(c.getProvider());
            }
        }
        System.gc();
    }

    public static void onUpdate(float ts) {
        TS = ts;
        if(!scriptThread.isWorking()) throw new IllegalStateException("Unexpected Thread State");
    }

    public static void stop() {
        ACTIVE_SCENE.STATE = SceneState.STOPPED;
        scriptThread.stop();
        System.gc();
    }

    public static void resume() {
        ACTIVE_SCENE.STATE = SceneState.RUNNING;
        scriptThread.restart();
        System.gc();
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