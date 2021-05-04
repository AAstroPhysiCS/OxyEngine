package OxyEngine.Scene;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scripting.OxyScript;
import OxyEngine.Scripting.ScriptEngine;
import OxyEngineEditor.EntryPoint;
import OxyEngineEditor.UI.Panels.SceneRuntimeControlPanel;

import java.util.ArrayList;
import java.util.List;

public final class SceneRuntime {

    private static final SceneRuntimeControlPanel panel = new SceneRuntimeControlPanel();

    public static OxyCamera currentBoundedCamera;
    public static OxyNativeObject currentBoundedSkyLight;
    public static Scene ACTIVE_SCENE;

    public static float TS = 0;

    private SceneRuntime() {
    }

    public static Object loadClass(String classBinName, Scene scene, OxyEntity entity) {
        try {
            return EntryPoint.class.getClassLoader().loadClass(classBinName).getDeclaredConstructor(Scene.class, OxyEntity.class).newInstance(scene, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final List<TransformComponent> transformComponents = new ArrayList<>();

    private static void saveSceneState() {
        for (OxyEntity e : SceneRenderer.getInstance().allModelEntities) {
            transformComponents.add(new TransformComponent(e.get(TransformComponent.class)));
        }
    }

    private static void loadSceneState() {
        if (transformComponents.size() == 0) return;

        int i = 0;
        for (OxyEntity e : SceneRenderer.getInstance().allModelEntities) e.addComponent(transformComponents.get(i++));
        transformComponents.clear();
    }

    public static void onCreate() {
        saveSceneState();
        for (OxyEntity e : ACTIVE_SCENE.getEntities()) {
            if (!(e instanceof OxyModel)) continue;
            for (OxyScript c : e.getScripts()) {
                c.invokeCreate();
                ScriptEngine.addProvider(c.getProvider());
            }
        }
        System.gc();
        resume();
    }

    public static void onUpdate(float ts) {
        TS = ts;
        ScriptEngine.onUpdate();
    }

    public static void stop() {
        ACTIVE_SCENE.STATE = SceneState.STOPPED;
        ScriptEngine.stop();
        System.gc();
        loadSceneState();
        OxyPhysX.getInstance().resetSimulation();
    }

    public static void resume() {
        ACTIVE_SCENE.STATE = SceneState.RUNNING;
        ScriptEngine.restart();
        System.gc();
    }

    public static void dispose() {
        OxyPhysX.getInstance().dispose();
        ACTIVE_SCENE.STATE = SceneState.TERMINATED;
        ScriptEngine.dispose();
        ACTIVE_SCENE.dispose();
    }

    public static SceneRuntimeControlPanel getPanel() {
        return panel;
    }
}