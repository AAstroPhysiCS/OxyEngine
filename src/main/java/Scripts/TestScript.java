package Scripts;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

public class TestScript extends ScriptableEntity {

    public TestScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    TransformComponent t;
    boolean enableX;
    boolean enableY;
    boolean enableZ;
    float speed = 0.01f;

    @Override
    public void onCreate() {
        t = getComponent(TransformComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        if(enableX) t.rotation.x += speed;
        if(enableY) t.rotation.y += speed;
        if(enableZ) t.rotation.z += speed;
        updateData();
    }
}
