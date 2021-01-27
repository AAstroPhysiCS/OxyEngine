package Scripts;

import OxyEngine.Components.OxyMaterialIndex;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterialPool;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

public class ColorIteratorScript extends ScriptableEntity {

    public ColorIteratorScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    OxyMaterial material;

    @Override
    public void onCreate() {
        material = OxyMaterialPool.getMaterial(getComponent(OxyMaterialIndex.class).index());
    }

    @Override
    public void onUpdate(float ts) {
        for (float r = 0; r <= 1; r += 0.01f) {
            for (float g = 0; g <= 1; g += 0.01f) {
                for (float b = 0; b <= 1; b += 0.01f) {
                    material.albedoColor.setColorRGBA(new float[]{r, g, b, 1});
                    updateData();
                }
            }
        }
    }
}
