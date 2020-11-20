package OxyEngineEditor.UI.Selector.Tools;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Components.BoundingBoxComponent;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.ModelFactory;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.Scene.OxyEntity;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

public interface ObjectSelector {

    Vector3f min = new Vector3f(), max = new Vector3f();
    Vector2f nearFar = new Vector2f();

    Vector3f toClipSpace(float width, float height, Vector2f mousePos);

    Vector3f getObjectPosRelativeToCamera(float width, float height, Vector2f mousePos, OxyCamera camera);

    default OxyEntity selectObject(Set<OxyEntity> entities, Vector3f origin, Vector3f direction) {
        reset();
        OxyEntity selectedEntity = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (OxyEntity entity : entities) {
            if (entity instanceof OxyNativeObject) continue;
            if (!entity.has(ModelFactory.class)) continue;
            if (!entity.has(SelectedComponent.class) || !entity.has(TransformComponent.class)) continue;
            if (entity.get(SelectedComponent.class).fixedValue) continue;

            TransformComponent c = entity.get(TransformComponent.class);
            BoundingBoxComponent boundingBox = entity.get(BoundingBoxComponent.class);
            TagComponent tag = entity.get(TagComponent.class);

            Vector3f position = new Vector3f(c.position);

            if (tag.tag().contains("Sphere")) {
                if (c.scale.x == c.scale.y && c.scale.x == c.scale.z) {
                    if (Intersectionf.intersectRaySphere(origin, direction, position, boundingBox.max().y * boundingBox.max().y * c.scale.y * c.scale.y, nearFar) && nearFar.x < closestDistance) {
                        closestDistance = nearFar.x;
                        selectedEntity = entity;
                    }
                } else {
                    float result;
                    int ptr = 0;
                    for (int i = 0; i < entity.vertices.length / 24; ) {
                        Vector3f firstVertex = new Vector3f(entity.vertices[ptr++], entity.vertices[ptr++], entity.vertices[ptr++]);
                        ptr += 5;
                        Vector3f secondVertex = new Vector3f(entity.vertices[ptr++], entity.vertices[ptr++], entity.vertices[ptr++]);
                        ptr += 5;
                        Vector3f thirdVertex = new Vector3f(entity.vertices[ptr++], entity.vertices[ptr++], entity.vertices[ptr++]);
                        ptr += 5;
                        if ((result = Intersectionf.intersectRayTriangle(origin, direction, firstVertex, secondVertex, thirdVertex, 0.0001f)) != -1) {
                            if (result < closestDistance) {
                                closestDistance = result;
                                selectedEntity = entity;
                                break;
                            }
                        }
                        i++;
                    }
                }
            } else if (tag.tag().contains("Cube")) {
                min.set(position);
                max.set(position);
                min.add(new Vector3f(boundingBox.min()).negate().mul(c.scale));
                max.add(new Vector3f(boundingBox.max()).mul(c.scale));
                if (Intersectionf.intersectRayAab(origin, direction, min, max, nearFar) && nearFar.x < closestDistance) {
                    closestDistance = nearFar.x;
                    selectedEntity = entity;
                }
            } else {
                float result;
                int ptr = 0;
                for (int i = 0; i < entity.vertices.length / 24; ) {
                    Vector3f firstVertex = new Vector3f(entity.vertices[ptr++], entity.vertices[ptr++], entity.vertices[ptr++]);
                    ptr += 5;
                    Vector3f secondVertex = new Vector3f(entity.vertices[ptr++], entity.vertices[ptr++], entity.vertices[ptr++]);
                    ptr += 5;
                    Vector3f thirdVertex = new Vector3f(entity.vertices[ptr++], entity.vertices[ptr++], entity.vertices[ptr++]);
                    ptr += 5;
                    if ((result = Intersectionf.intersectRayTriangle(origin, direction, firstVertex, secondVertex, thirdVertex, 1E-3f)) != -1) {
                        if (result < closestDistance) {
                            closestDistance = result;
                            selectedEntity = entity;
                            break;
                        }
                    }
                    i++;
                }
            }
        }
        return selectedEntity;
    }

    //mainly for gizmo
    default OxyEntity selectObjectGizmo(Set<OxyEntity> entities, Vector3f origin, Vector3f direction) {
        reset();
        float closestDistance = Float.POSITIVE_INFINITY;
        OxyEntity selectedEntity = null;

        for (OxyEntity entity : entities) {

            if (!entity.has(SelectedComponent.class) || !entity.has(TransformComponent.class))
                return null;

            TransformComponent c = entity.get(TransformComponent.class);
            SelectedComponent selected = entity.get(SelectedComponent.class);
            BoundingBoxComponent boundingBox = entity.get(BoundingBoxComponent.class);

            Vector3f position = new Vector3f(c.position);

            selected.selected = false;
            min.set(position);
            max.set(position);
            min.add(new Vector3f(boundingBox.min()).negate().mul(c.scale));
            max.add(new Vector3f(boundingBox.max()).mul(c.scale));
            if (Intersectionf.intersectRayAab(origin, direction, min, max, nearFar) && nearFar.x < closestDistance) {
                selectedEntity = entity;
                selected.selected = true;
            }
        }
        return selectedEntity;
    }

    default void reset() {
        min.zero();
        max.zero();
        nearFar.zero();
    }
}