package OxyEngine.Scripting.NativeScripts;

import OxyEngine.Scripting.ScriptableEntity;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

public class ColorIteratorScript extends ScriptableEntity {

    public ColorIteratorScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    OxyMaterial material;

    @Override
    public void onCreate() {
        material = getComponent(OxyMaterial.class);
    }

    //just the red channel
    float colorR = 0;

    @Override
    public void onUpdate(float ts) {
        material.albedoColor.setColorRGBA(new float[]{colorR, 1.0f, 1.0f, 1f});
        colorR += 0.01f;
        if (colorR >= 1.0f) colorR = 0;
    }
}
