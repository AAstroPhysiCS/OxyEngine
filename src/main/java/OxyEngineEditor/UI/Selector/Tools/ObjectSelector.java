package OxyEngineEditor.UI.Selector.Tools;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngineEditor.Sandbox.OxyComponents.BoundingBoxComponent;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

public interface ObjectSelector {

    Vector3f min = new Vector3f(), max = new Vector3f();
    Vector2f nearFar = new Vector2f();

    Vector3f getObjectPosRelativeToCamera(float width, float height, Vector2f mousePos, OxyCamera camera);

    //It's better to not summarize this method with the other ones...
    default OxyEntity selectObject(Set<OxyEntity> entities, Vector3f center, Vector3f direction) {
        reset();
        OxyEntity selectedEntity = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (OxyEntity entity : entities) {

            if (!entity.has(SelectedComponent.class) || !entity.has(TransformComponent.class)) continue;
            if (entity.get(SelectedComponent.class).fixedValue) continue;

            TransformComponent c = entity.get(TransformComponent.class);
            SelectedComponent selected = entity.get(SelectedComponent.class);
            BoundingBoxComponent boundingBox = entity.get(BoundingBoxComponent.class);

            selected.selected = false;

            if(boundingBox != null) {
                min.set(boundingBox.pos());
                max.set(boundingBox.pos());
            } else {
                min.set(c.position);
                max.set(c.position);
            }
            min.add(-c.scale, -c.scale, -c.scale);
            max.add(c.scale, c.scale, c.scale);
            if (Intersectionf.intersectRayAab(center, direction, min, max, nearFar) && nearFar.x < closestDistance) {
                closestDistance = nearFar.x;
                selectedEntity = entity;
                selected.selected = true;
            }
        }
        return selectedEntity;
    }

    default OxyEntity selectObject(OxyEntity entity, Vector3f center, Vector3f direction) {
        reset();
        if (center == null || direction == null || !entity.has(SelectedComponent.class) || !entity.has(TransformComponent.class))
            return null;

        OxyEntity selectedEntity = null;
        TransformComponent c = entity.get(TransformComponent.class);
        SelectedComponent selected = entity.get(SelectedComponent.class);
        BoundingBoxComponent boundingBox = entity.get(BoundingBoxComponent.class);

        selected.selected = false;
        if(boundingBox != null) {
            min.set(boundingBox.pos());
            max.set(boundingBox.pos());
        } else {
            min.set(c.position);
            max.set(c.position);
        }
        min.add(-c.scale, -c.scale, -c.scale);
        max.add(c.scale, c.scale, c.scale);
        if (Intersectionf.intersectRayAab(center, direction, min, max, nearFar)) {
            selectedEntity = entity;
            selected.selected = true;
        }
        return selectedEntity;
    }

    default void reset() {
        min.zero();
        max.zero();
        nearFar.zero();
    }
}
