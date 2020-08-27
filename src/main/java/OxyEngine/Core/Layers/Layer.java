package OxyEngine.Core.Layers;

import OxyEngineEditor.Scene.Scene;

public abstract class Layer {

    protected final Scene scene;

    public Layer(Scene scene) {
        this.scene = scene;
    }

    public abstract void build();

    public abstract void rebuild();

    public abstract void update(float ts, float deltaTime);

    public abstract void render(float ts, float deltaTime);

    public Scene getScene() {
        return scene;
    }
}
