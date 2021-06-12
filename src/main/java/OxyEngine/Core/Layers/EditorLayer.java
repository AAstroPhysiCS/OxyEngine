package OxyEngine.Core.Layers;

import OxyEngine.PhysX.OxyPhysX;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.Scripting.ScriptEngine;

public class EditorLayer extends Layer {

    private static EditorLayer INSTANCE = null;

    private final OxyPhysX oxyPhysics = OxyPhysX.getInstance();

    private final SceneRenderer sceneRenderer = SceneRenderer.getInstance();

    public static EditorLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new EditorLayer();
        return INSTANCE;
    }

    private EditorLayer() {
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
    public void render() {
        sceneRenderer.renderScene();
    }
}