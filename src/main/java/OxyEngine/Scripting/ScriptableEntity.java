package OxyEngine.Scripting;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Core.Context.Scene.Entity;
import OxyEngine.Core.Context.Scene.Scene;

public abstract class ScriptableEntity {

    private final Scene scene;
    final Entity entity;

    public ScriptableEntity(Scene scene, Entity entity) {
        this.scene = scene;
        this.entity = entity;
    }

    protected <T extends EntityComponent> T getComponent(Class<T> destClass) {
        return scene.get(entity, destClass);
    }

    protected <T extends EntityComponent> boolean hasComponent(Class<T> destClass) {
        return scene.has(entity, destClass);
    }

    protected <T extends EntityComponent> Entity getEntityByName(String name){
        for(var s : scene.getEntities()){
            if(s.has(TagComponent.class)){
                if(s.get(TagComponent.class).tag().equals(name)) return s;
            }
        }
        return null;
    }

    void updateScript(float ts) {
        if (entity == null || scene == null) return;
        onUpdate(ts);
    }

    public abstract void onCreate();

    public abstract void onUpdate(float ts);
}
