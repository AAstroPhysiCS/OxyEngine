package Scripts;

import OxyEngine.Scripting.ScriptableEntity;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

public class PositionIteratorScript extends ScriptableEntity {

    public PositionIteratorScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    public TransformComponent c;

    @Override
    public void onCreate() {
        c = getComponent(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        c.position.add(0.01f, 0f, 0f);
        updateData();
    }
}
