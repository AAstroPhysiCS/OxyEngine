package OxyEngine.Scripting.NativeScripts;

import OxyEngine.Scripting.ScriptableEntity;
import OxyEngineEditor.Components.EmittingComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

public class LightPositionScript extends ScriptableEntity {

    public LightPositionScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    TransformComponent transform;
    OxyMaterial material;
    EmittingComponent emittingComponent;

    @Override
    public void onCreate() {
        transform = getComponent(TransformComponent.class);
        material = getComponent(OxyMaterial.class);
        emittingComponent = getComponent(EmittingComponent.class);
    }

    @Override
    public void onUpdate(float ts) {
        emittingComponent.position().set(transform.position);
        emittingComponent.diffuse().set(material.albedoColor.getNumbers());
    }
}
