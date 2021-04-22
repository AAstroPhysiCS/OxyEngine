package OxyEngine.Core.Layers;

import OxyEngine.Scene.Objects.WorldGrid;
import OxyEngine.Scene.SceneRenderer;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;

public class SceneLayer extends Layer {

    private static SceneLayer INSTANCE = null;

    public static SceneLayer getInstance() {
        if (INSTANCE == null) INSTANCE = new SceneLayer();
        return INSTANCE;
    }

    private SceneLayer() {
    }

    @Override
    public void build() {
        SceneRenderer.getInstance().initPipelines();
        SceneRenderer.getInstance().initScene();
        new WorldGrid(ACTIVE_SCENE, 10);
    }

    @Override
    public void update(float ts) {
        SceneRenderer.getInstance().updateScene(ts);
    }

    @Override
    public void render(float ts) {
        SceneRenderer.getInstance().renderScene(ts);
    }
}