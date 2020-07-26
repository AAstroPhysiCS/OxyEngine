package OxyEngineEditor.UI.Selector.Tools;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.OxyObjects.OxyEntity;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public interface ObjectSelector {

    Vector3f min = new Vector3f(), max = new Vector3f();
    Vector2f nearFar = new Vector2f();

    Vector3f getObjectPosRelativeToCamera(float width, float height, Vector2f mousePos, OxyCamera camera);

    //It's better to not summarize this method with the other ones...
    default OxyEntity selectObject(List<OxyEntity> entities, Vector3f center, Vector3f direction) {
        reset();
        OxyEntity selectedEntity = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (OxyEntity entity : entities) {
            entity.selected = false;
            min.set(entity.getPosition());
            max.set(entity.getPosition());
            min.add(-entity.getScale(), -entity.getScale(), -entity.getScale());
            max.add(entity.getScale(), entity.getScale(), entity.getScale());
            if (Intersectionf.intersectRayAab(center, direction, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                selectedEntity = entity;
                entity.selected = true;
            }
        }
        return selectedEntity;
    }

    default OxyEntity selectObject(OxyEntity entity, Vector3f center, Vector3f direction) {
        reset();
        if (center == null || direction == null) return null;
        OxyEntity selectedEntity = null;

        entity.selected = false;
        min.set(entity.getPosition());
        max.set(entity.getPosition());
        min.add(-entity.getScale(), -entity.getScale(), -entity.getScale());
        max.add(entity.getScale(), entity.getScale(), entity.getScale());
        if (Intersectionf.intersectRayAab(center, direction, min, max, nearFar)) {
            selectedEntity = entity;
            entity.selected = true;
        }
        return selectedEntity;
    }

    default void reset() {
        min.zero();
        max.zero();
        nearFar.zero();
    }
}
