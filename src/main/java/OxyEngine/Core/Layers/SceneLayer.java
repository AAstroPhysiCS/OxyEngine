package OxyEngine.Core.Layers;

import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.Scripting.ScriptEngine;

public class SceneLayer extends Layer {

    private static SceneLayer INSTANCE = null;

    private final OxyPhysX oxyPhysics = OxyPhysX.getInstance();

    public static SceneLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneLayer();
        return INSTANCE;
    }

    private SceneLayer() {
    }

    @Override
    public void build() {
        oxyPhysics.init();
        SceneRenderer.getInstance().initPipelines();
        SceneRenderer.getInstance().initScene();
    }

    @Override
    public void update(float ts) {
        SceneRenderer.getInstance().updateScene(ts);
        ScriptEngine.notifyLock(); //running script engine
        oxyPhysics.simulate();
    }

    @Override
    public void render(float ts) {
        SceneRenderer.getInstance().renderScene(ts);
    }
}