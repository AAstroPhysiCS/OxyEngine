package OxyEngine.Scripting;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.Core.Context.Scene.Scene;

public abstract class ScriptableEntity {

    private final Scene scene;
    final OxyEntity entity;

    public ScriptableEntity(Scene scene, OxyEntity entity) {
        this.scene = scene;
        this.entity = entity;
    }

    protected <T extends EntityComponent> T getComponent(Class<T> destClass) {
        return scene.get(entity, destClass);
    }

    protected <T extends EntityComponent> boolean hasComponent(Class<T> destClass) {
        return scene.has(entity, destClass);
    }

    protected <T extends EntityComponent> OxyEntity getEntityByName(String name){
        for(var s : scene.getEntities()){
            if(s.has(TagComponent.class)){
                if(s.get(TagComponent.class).tag().equals(name)) return s;
            }
        }
        return null;
    }

    protected void updateData() {
        if (entity == null) return;
        entity.transformLocally();
    }

    public abstract void onCreate();

    void updateScript(float ts) {
        if (entity == null || scene == null) return;
        onUpdate(ts);
        updateData();
    }

    public abstract void onUpdate(float ts);
}
