package OxyEngine.Scripting;

import java.lang.reflect.Field;

import static OxyEngine.Core.Context.Scene.SceneRuntime.sceneContext;

final class EntityInfoProvider implements Provider {

    private final ScriptableEntity scriptableEntity;

    private final Field[] allFields;

    public EntityInfoProvider(ScriptableEntity scriptableEntity) {
        this.scriptableEntity = scriptableEntity;
        this.allFields = scriptableEntity.getClass().getDeclaredFields();
//            System.out.println(Arrays.toString(allFields));
//            for (Field f : allFields) f.setAccessible(true);
    }

    public Field[] getAllFields() {
        return allFields;
    }

    public ScriptableEntity getScriptableEntity() {
        return scriptableEntity;
    }

    @Override
    public void invokeCreate() {
        scriptableEntity.onCreate();
    }

    @Override
    public void invokeUpdate(float ts) {
        if (!sceneContext.isValid(scriptableEntity.entity)) return;
        scriptableEntity.updateScript(ts);
    }
}
