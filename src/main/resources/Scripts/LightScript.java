package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.Scene;

public class LightScript extends ScriptableEntity {

    public LightScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    TransformComponent t;
    public float Speed;
    int dir = 1;

    @Override
    public void onCreate() {
        t = getComponent(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        float finalSpeed = Speed * ts;
        if((int) t.position.x == 100) dir *= -1;
        if((int) t.position.x == -100) dir *= -1;
        t.position.add(finalSpeed * dir, 0, 0);
        updateData();
    }
}
