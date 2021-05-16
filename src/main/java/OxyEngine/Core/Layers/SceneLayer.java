package OxyEngine.Core.Layers;

import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.Scripting.ScriptEngine;

public class SceneLayer extends Layer {

    private static SceneLayer INSTANCE = null;

    private final OxyPhysX oxyPhysics = OxyPhysX.getInstance();

    private final SceneRenderer sceneRenderer = SceneRenderer.getInstance();

    public static SceneLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneLayer();
        return INSTANCE;
    }

    private SceneLayer() {
    }

    @Override
    public void build() {
        oxyPhysics.init();
        sceneRenderer.initPipelines();
        sceneRenderer.initScene();
    }

    @Override
    public void update(float ts) {
        sceneRenderer.updateScene(ts);
        ScriptEngine.run();
        oxyPhysics.simulate();
    }

    @Override
    public void render(float ts) {
        sceneRenderer.renderScene(ts);
    }
}