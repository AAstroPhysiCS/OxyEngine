package OxyEngine.Scripting;

import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

public abstract class ScriptableEntity {

    private final Scene scene;
    private final OxyEntity entity;

    protected ScriptableEntity(Scene scene, OxyEntity entity) {
        this.scene = scene;
        this.entity = entity;
    }

    protected <T extends EntityComponent> T getComponent(Class<T> destClass) {
        return scene.get(entity, destClass);
    }

    protected <T extends EntityComponent> boolean hasComponent(Class<T> destClass) {
        return scene.has(entity, destClass);
    }

    protected abstract void onCreate();

    protected abstract void onUpdate(float ts);
}
