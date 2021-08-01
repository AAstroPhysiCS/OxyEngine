package Scripts;

import OxyEngine.Components.OxyMaterialIndex;
import OxyEngine.Scripting.ScriptableEntity;
import OxyEngine.Core.Context.Scene.OxyMaterial;
import OxyEngine.Core.Context.Scene.OxyMaterialPool;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.Core.Context.Scene.Scene;

public class ColorIteratorScript extends ScriptableEntity {

    public ColorIteratorScript(Scene scene, OxyEntity entity) {
        super(scene, entity);
    }

    OxyMaterial material;

    @Override
    public void onCreate() {
        //noinspection OptionalGetWithoutIsPresent
        material = OxyMaterialPool.getMaterial(getComponent(OxyMaterialIndex.class).index()).get();
    }

    @Override
    public void onUpdate(float ts) {
        for (float r = 0; r <= 1; r += 0.01f) {
            for (float g = 0; g <= 1; g += 0.01f) {
                for (float b = 0; b <= 1; b += 0.01f) {
                    material.albedoColor.setColorRGBA(new float[]{r, g, b, 1});
                }
            }
        }
    }
}
