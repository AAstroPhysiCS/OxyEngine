package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngine.Core.Context.Scene.Entity;
import OxyEngine.Core.Context.Scene.Scene;

public final class TestScript extends ScriptableEntity {

    public TestScript(Scene scene, Entity entity) {
        super(scene, entity);
    }

    TransformComponent t;
    public boolean enableX;
    public boolean enableY;
    public boolean enableZ;
    public float speed = 0.01f;

    @Override
    public void onCreate() {
        t = getComponent(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        if(enableX) t.rotation.x += speed * ts;
        if(enableY) t.rotation.y += speed * ts;
        if(enableZ) t.rotation.z += speed * ts;
        updateData();
    }
}
